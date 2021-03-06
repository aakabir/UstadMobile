package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.android.netwokmanager.EmbeddedHttpdService;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.util.RunnableQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mike on 2/15/18.
 */

public abstract class ZippedContentActivity extends UstadBaseActivity {

    private AtomicReference<EmbeddedHTTPD> httpdRef = new AtomicReference<>();

    private AtomicBoolean httpdBound = new AtomicBoolean(false);

    private RunnableQueue runWhenConnectedQueue = new RunnableQueue();

    private ServiceConnection httpdServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            httpdRef.set(((EmbeddedHttpdService.LocalServiceBinder)service).getHttpd());
            httpdBound.set(true);
            runWhenConnectedQueue.setReady(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            runWhenConnectedQueue.setReady(false);
            httpdBound.set(false);
        }
    };

    private static class MountZipAsyncTask extends AsyncTask<String, Void, String> {

        private UmCallback<String> callback;

        private EmbeddedHTTPD httpd;

        protected MountZipAsyncTask(UmCallback callback, EmbeddedHTTPD httpd) {
            this.callback = callback;
            this.httpd = httpd;
        }

        @Override
        protected String doInBackground(String... strings) {
            String mountedUri = httpd.mountZipOnHttp(strings[0], null, false, null);
            return UMFileUtil.joinPaths(httpd.getLocalHttpUrl(),
                    mountedUri);
        }

        @Override
        protected void onPostExecute(String mountedPath) {
            callback.onSuccess(mountedPath);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent httpdServiceIntent = new Intent(this, EmbeddedHttpdService.class);
        bindService(httpdServiceIntent, httpdServiceConnection,
                Context.BIND_AUTO_CREATE|Context.BIND_ADJUST_WITH_ACTIVITY);
    }


    @Override
    public void onDestroy() {
        if(httpdBound.get()) {
            unbindService(httpdServiceConnection);
        }

        super.onDestroy();
    }

    public void mountZip(String zipUri, UmCallback<String> callback) {
        runWhenConnectedQueue.runWhenReady(() -> {
            new MountZipAsyncTask(callback, httpdRef.get()).doInBackground(zipUri);
        });
    }

    public void unmountZipFromHttp(String mountedPath){
        httpdRef.get().unmountZip(mountedPath);
    }

    protected void runWhenHttpdReady(Runnable runnable) {
        runWhenConnectedQueue.runWhenReady(runnable);
    }



}
