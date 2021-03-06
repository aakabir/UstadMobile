package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_CONNECTED_LOCAL;
import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_CONNECTING_LOCAL;
import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_DISCONNECTED;
import static com.ustadmobile.lib.db.entities.ConnectivityStatus.STATE_METERED;
import static com.ustadmobile.lib.db.entities.DownloadJobItemHistory.MODE_CLOUD;
import static com.ustadmobile.lib.db.entities.DownloadJobItemHistory.MODE_LOCAL;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_REQUEST;

/**
 * Class which handles all file downloading tasks, it reacts to different status as changed
 * in the Db from either UI or Network change.
 *
 * i.e Decides where to get the file based on the entry status response,
 * connecting to the peer device via BLE and WiFiP2P for the actual download
 * and Change its status based on Network status.
 *
 * @author kileha3
 */
public class DownloadJobItemRunner implements Runnable, BleMessageResponseListener{

    private NetworkManagerBle networkManager;

    private UmAppDatabase appDb;

    private DownloadJobItemWithDownloadSetItem downloadItem;

    private String endpointUrl;

    public static final String CONTENT_ENTRY_FILE_PATH = "ContentEntryFile/";

    private UmLiveData<ConnectivityStatus> statusLiveData;

    private UmObserver<ConnectivityStatus> statusObserver;

    private UmObserver<EntryStatusResponse> entryStatusObserver;

    private UmLiveData<EntryStatusResponse> entryStatusLiveData;

    private UmObserver<Integer> downloadJobItemObserver;

    private UmLiveData<Integer> downloadJobItemLiveData;

    private UmLiveData<Boolean> downloadSetConnectivityData;

    private UmObserver<Boolean> downloadSetConnectivityObserver;

    private CountDownLatch localConnectLatch = new CountDownLatch(1);

    private volatile ResumableHttpDownload httpDownload;

    private AtomicReference<ResumableHttpDownload> httpDownloadRef;

    private Timer statusCheckTimer = new Timer();

    private AtomicInteger runnerStatus = new AtomicInteger(JobStatus.NOT_QUEUED);

    private ConnectivityStatus connectivityStatus;

    private AtomicInteger meteredConnectionAllowed = new AtomicInteger(-1);

    private AtomicInteger availableLocally = new AtomicInteger(-1);

    private Object context;

    private WiFiDirectGroupBle wiFiDirectGroupBle;

    private NetworkNode currentNetworkNode;

    private EntryStatusResponse currentContentEntryFileStatus;

    private boolean isFromCloud = true;

    private static final int BAD_PEER_FAILURE_THRESHOLD = 3;

    /**
     * Timer task to keep track of the download status
     */
    private class StatusCheckTask extends TimerTask{

        @Override
        public void run() {
            ResumableHttpDownload httpDownload  = httpDownloadRef.get();
            if(httpDownload != null && runnerStatus.get() == JobStatus.RUNNING) {
                appDb.getDownloadJobItemDao().updateDownloadJobItemProgress(
                        downloadItem.getDjiUid(), httpDownload.getDownloadedSoFar(),
                        httpDownload.getCurrentDownloadSpeed());
                appDb.getDownloadJobDao().updateBytesDownloadedSoFar
                        (downloadItem.getDjiDjUid(),
                        null);
            }
        }
    }


    /**
     * Constructor to be used when creating new instance of the runner.
     * @param downloadItem Item to be downloaded
     * @param networkManager BLE network manager for network operation controls.
     * @param appDb Application database instance
     * @param endpointUrl Endpoint to get the file from.
     */
    public DownloadJobItemRunner(Object context,DownloadJobItemWithDownloadSetItem downloadItem,
                                 NetworkManagerBle networkManager, UmAppDatabase appDb,
                                 String endpointUrl) {
        this.networkManager = networkManager;
        this.downloadItem = downloadItem;
        this.appDb = appDb;
        this.endpointUrl = endpointUrl;
        this.context = context;
        this.httpDownloadRef = new AtomicReference<>();
    }

    /**
     * Handle changes triggered when connectivity status changes.
     * @param newStatus changed connectivity status
     */
    private void handleConnectivityStatusChanged(ConnectivityStatus newStatus) {
        this.connectivityStatus = newStatus;
        if(connectivityStatus != null ){
            switch(newStatus.getConnectivityState()) {
                case STATE_METERED:
                    if(meteredConnectionAllowed.get() == 0) {
                        stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                    }
                    break;

                case STATE_DISCONNECTED:
                    stopAsync(JobStatus.WAITING_FOR_CONNECTION);
                    break;

                case STATE_CONNECTING_LOCAL:
                    if(newStatus.getWifiSsid().equals(wiFiDirectGroupBle.getSsid())){
                        localConnectLatch.countDown();
                    }
                    break;

            }
        }
    }

    /**
     * Handle changes triggered when Download set metered connection flag changes
     * @param meteredConnection changed metered connection flag.
     */
    private void handleDownloadSetMeteredConnectionAllowedChanged(Boolean meteredConnection){
        if(meteredConnection != null){
            meteredConnectionAllowed.set(meteredConnection ? 1 : 0);
            if(meteredConnectionAllowed.get() == 0 && connectivityStatus != null
                    && connectivityStatus.getConnectivityState() == STATE_METERED) {
                UstadMobileSystemImpl.l(UMLog.DEBUG, 699, mkLogPrefix() +
                        " : no longer allowed to run on metered network - stopping");
                stopAsync(JobStatus.WAITING_FOR_CONNECTION);
            }
        }
    }

    /**
     * Handle changes triggered when the download job item status changes
     * @param newDownloadStatus changed download job item status
     */

    private void handleDownloadJobItemStatusChanged(Integer newDownloadStatus){
        if(newDownloadStatus != null && newDownloadStatus == JobStatus.STOPPING){
            stopAsync(JobStatus.STOPPED);
        }
    }

    /**
     * Handle changes triggered when file which wasn't available locally changes
     * @param entryStatusResponse new file entry status
     */
    private void handleContentEntryFileStatus(EntryStatusResponse entryStatusResponse){
        if(entryStatusResponse != null){
            availableLocally.set(entryStatusResponse.isAvailable() ? 1:0);
            if(availableLocally.get() == 1 && !currentContentEntryFileStatus.isAvailable()){
                this.currentNetworkNode =
                        appDb.getNetworkNodeDao().findNodeById(entryStatusResponse.getErNodeId());
                startLocalConnectionHandShake();
            }
        }
    }


    /**
     * Stop download task Async
     * @param newStatus net status
     */
    private void stopAsync(int newStatus){
        runnerStatus.set(JobStatus.STOPPING);
        new Thread(() -> stop(newStatus)).start();
    }

    /**
     * Stop the download task from continuing (if not already stopped). Calling stop for a second
     * time will have no effect.
     *
     * @param newStatus new status to be set
     */
    private void stop(int newStatus) {
        if(runnerStatus.get() != JobStatus.STOPPED){
            runnerStatus.set(JobStatus.STOPPED);

            if(httpDownload != null){
                httpDownload.stop();
            }

            statusLiveData.removeObserver(statusObserver);
            downloadJobItemLiveData.removeObserver(downloadJobItemObserver);
            downloadSetConnectivityData.removeObserver(downloadSetConnectivityObserver);
            entryStatusLiveData.removeObserver(entryStatusObserver);

            statusCheckTimer.cancel();

            updateItemStatus(newStatus);
            appDb.getDownloadJobDao().updateJobStatusToCompleteIfAllItemsAreCompleted(
                    downloadItem.getDjiDjUid());
        }
    }


    @Override
    public void run() {
        runnerStatus.set(JobStatus.RUNNING);
        updateItemStatus(JobStatus.RUNNING);
        long downloadJobId = appDb.getDownloadJobDao().getLatestDownloadJobUidForDownloadSet(
                downloadItem.getDownloadSetItem().getDsiDsUid());
        appDb.getDownloadJobDao().update(downloadJobId, JobStatus.RUNNING);

        networkManager.startMonitoringAvailability(this,
                Arrays.asList(downloadItem.getDjiContentEntryFileUid()));

        statusLiveData = appDb.getConnectivityStatusDao().getStatusLive();
        downloadJobItemLiveData = appDb.getDownloadJobItemDao().getLiveStatus(downloadItem.getDjiUid());

        //get the download set
        downloadSetConnectivityData =
                appDb.getDownloadSetDao().getLiveMeteredNetworkAllowed(downloadItem
                        .getDownloadSetItem().getDsiDsUid());

        entryStatusLiveData = appDb.getEntryStatusResponseDao()
                .getLiveEntryStatus(downloadItem.getDjiContentEntryFileUid());

        downloadSetConnectivityObserver = this::handleDownloadSetMeteredConnectionAllowedChanged;
        statusObserver = this::handleConnectivityStatusChanged;
        downloadJobItemObserver = this::handleDownloadJobItemStatusChanged;
        entryStatusObserver = this::handleContentEntryFileStatus;
        statusLiveData.observeForever(statusObserver);
        downloadJobItemLiveData.observeForever(downloadJobItemObserver);
        downloadSetConnectivityData.observeForever(downloadSetConnectivityObserver);
        entryStatusLiveData.observeForever(entryStatusObserver);

        currentContentEntryFileStatus = appDb.getEntryStatusResponseDao()
                .findByContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());

        checkWhereToDownloadAFileFrom();
    }

    /**
     * Decide where to get the file, on cloud or from peer devices.
     */
    private void checkWhereToDownloadAFileFrom(){
        long currentTimeStamp = System.currentTimeMillis();
        long minLastSeen = currentTimeStamp - TimeUnit.MINUTES.toMillis(1);
        long maxFailureFromTimeStamp = currentTimeStamp - TimeUnit.MINUTES.toMillis(5);

        currentNetworkNode = appDb.getNetworkNodeDao()
                .findNodeWithContentFileEntry(downloadItem.getDjiContentEntryFileUid(),
                        minLastSeen,BAD_PEER_FAILURE_THRESHOLD,maxFailureFromTimeStamp);

        if(currentContentEntryFileStatus == null || currentNetworkNode == null){
            startDownload();
        }else{
            isFromCloud = false;
            startLocalConnectionHandShake();
        }
    }


    /**
     * Start local peers connection handshake
     */
    private void startLocalConnectionHandShake(){
        BleMessage requestGroupCreation = new BleMessage(WIFI_GROUP_REQUEST,new byte[0]);
        networkManager.sendMessage(context,requestGroupCreation,
                currentNetworkNode,this);
        try {
            localConnectLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(connectivityStatus != null &&
                connectivityStatus.getConnectivityState() == STATE_CONNECTING_LOCAL){
            stopAsync(JobStatus.WAITING_FOR_CONNECTION);
        }
    }

    /**
     * Start downloading a file
     */
    private void startDownload(){
        int attemptsRemaining = 3;

        boolean downloaded = false;
        StatusCheckTask statusCheckTask = new StatusCheckTask();
        statusCheckTimer.scheduleAtFixedRate(statusCheckTask,
                0, TimeUnit.SECONDS.toMillis(1));
        DownloadJobItemHistory history = new DownloadJobItemHistory();
        history.setMode(isFromCloud ? MODE_CLOUD : MODE_LOCAL);
        history.setDownloadJobItemId(downloadItem.getDjiUid());
        history.setNetworkNode(isFromCloud ? 0L: currentNetworkNode.getNodeId());
        history.setUrl(getFileUrl());
        history.setId((int) appDb.getDownloadJobItemHistoryDao().insert(history));
        do {
            try {
                appDb.getDownloadJobItemDao().incrementNumAttempts(downloadItem.getDjiUid());
                httpDownload = new ResumableHttpDownload(getFileUrl(),
                        downloadItem.getDestinationFile());
                httpDownloadRef.set(httpDownload);
                history.setStartTime(System.currentTimeMillis());
                downloaded = httpDownload.download();
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR,699, mkLogPrefix() +
                        "Failed to download a file from "+getFileUrl(),e);
            }

            if(!downloaded) {
                //wait before retry
                try { Thread.sleep(3000); }
                catch(InterruptedException ignored) {}
            }

            history.setEndTime(System.currentTimeMillis());
            history.setSuccessful(downloaded);
            appDb.getDownloadJobItemHistoryDao().update(history);

            attemptsRemaining--;
        }while(runnerStatus.get() == JobStatus.RUNNING && !downloaded && attemptsRemaining > 0);

        //httpdownloadref usage is finished
        httpDownloadRef.set(null);

        if(downloaded){
            ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
            fileStatus.setFilePath(downloadItem.getDestinationFile());
            fileStatus.setCefsContentEntryFileUid(downloadItem.getDjiContentEntryFileUid());
            appDb.getContentEntryFileStatusDao().insert(fileStatus);
            appDb.getDownloadJobDao().updateBytesDownloadedSoFar(downloadItem.getDjiDjUid(),
                    null);
            appDb.getDownloadJobItemDao().updateDownloadJobItemStatus(downloadItem.getDjiUid(),
                    JobStatus.COMPLETE, httpDownload.getDownloadedSoFar(),
                    httpDownload.getTotalSize(),httpDownload.getCurrentDownloadSpeed());
        }

        stop(downloaded ? JobStatus.COMPLETE : JobStatus.FAILED);

    }


    /**
     * Create URL where the runner will get the file from
     * @return constructed file URL
     */
    private String getFileUrl(){
        return (isFromCloud ? this.endpointUrl :  wiFiDirectGroupBle.getEndpoint())
                + CONTENT_ENTRY_FILE_PATH + downloadItem.getDjiContentEntryFileUid();
    }

    /**
     * Update status of the currently downloading job item.
     * @param itemStatus new status to be set
     * @see JobStatus
     */
    private void updateItemStatus(int itemStatus) {
        appDb.getDownloadJobItemDao().updateStatus(downloadItem.getDjiUid(), itemStatus);
        appDb.getContentEntryStatusDao().updateDownloadStatus(
                downloadItem.getDownloadSetItem().getDsiContentEntryUid(), itemStatus);
    }


    @Override
    public void onResponseReceived(String sourceDeviceAddress, BleMessage response) {
        if(response.getRequestType() == WIFI_GROUP_CREATION_RESPONSE){
            this.wiFiDirectGroupBle = new Gson().fromJson(new String(response.getPayload()),
                            WiFiDirectGroupBle.class);
            networkManager.connectToWiFi(wiFiDirectGroupBle.getSsid(),
                    wiFiDirectGroupBle.getPassphrase());
        }
    }

    private String mkLogPrefix() {
        return "DownloadJobItem #" + downloadItem.getDjiUid() + ":";
    }
}
