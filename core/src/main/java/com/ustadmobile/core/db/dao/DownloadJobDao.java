package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithDownloadSet;
import com.ustadmobile.lib.db.entities.DownloadJobWithTotals;

import java.util.List;

/**
 * DAO for the DownloadJob class
 */
public abstract class DownloadJobDao {

    /**
     * IInsert a new DownloadJob
     *
     * @param job DownloadJob entity to insert
     *
     * @return The Primary Key value assigned to the inserted object
     */
    public abstract long insert(DownloadJob job);

    /**
     * Mark the given DownloadJob entity as queued
     *
     * @param id The downloadJobId of the DownloadJob entity to update
     * @param status The status to mark on the DownloadJob
     * @param timeRequested The time the download job is to be marked as queued (in ms)
     */
    @UmQuery("Update DownloadJobRun SET status = :status, timeRequested = :timeRequested WHERE id = :id")
    public abstract void queueDownload(int id, int status, long timeRequested);

    /**
     * Find the next pending DownloadJob (the oldest DownloadJob that is pending)
     *
     * @return The next DownloadJob to run
     */
    @UmQuery("SELECT * FROM DownloadJobRun WHERE status > 0 AND status <= 10 ORDER BY timeRequested LIMIT 1")
    protected abstract DownloadJobWithDownloadSet findNextDownloadJob(boolean connectionMetered);

    /**
     * Update the status of the given DownloadJob
     *
     * @param id The DownloadJobId of the download job to update the status for
     * @param status The status to set on the given DownloadJob
     *
     */
    @UmQuery("UPDATE DownloadJobRun SET status = :status WHERE id = :jobId")
    public abstract void updateJobStatus(int id, int status);

    /**
     * Mark the status in bulk of DownloadJob, useful for testing purposes to cancel other downloads
     *
     * @param rangeFrom The minimum existing status of a job
     * @param rangeTo The maximum existing status of a job
     * @param setTo The status to set on a job
     */
    @UmQuery("UPDATE DownloadJobRun SET status = :setTo WHERE status BETWEEN :rangeFrom AND :rangeTo")
    @Deprecated
    public abstract void updateJobStatusByRange(int rangeFrom, int rangeTo, int setTo);


    /**
     * Update all fields on the given DownloadJob
     *
     * @param job The DownloadJob to update
     */
    public abstract void update(DownloadJob job);


    /**
     * Find a DownloadJob by the downloadJobId (primary key)
     *
     * @param id downloadJobId to search for.
     *
     * @return The DownloadJob with the given id, or null if no such DownloadJob exists
     */
    public abstract DownloadJob findById(int id);

    /**
     * Find a DownloadJobWithDownloadSet by the downloadJobId (primary key)
     *
     * @param id downloadJobId to search for.
     * @return The DownloadJobWithDownloadSet for the given id, or null if no such DownloadJob exists
     */
    public abstract DownloadJobWithDownloadSet findByIdWithDownloadSet(int id);



    /**
     * Find the corresponding downloadSetId for the given DownloadJob
     *
     * @param downloadJobId Primary key of the given DownloadJob
     *
     * @return The primary key of the related DownloadSet
     */
    public abstract int findDownloadSetId(int downloadJobId);

    /**
     * Find the next eligible DownloadJob, and if a job is remaining, set it's status to
     * NetworkTask.STATUS_STARTING
     *
     * @return The DownloadJob that has been marked as started, if any was pending
     */
    public DownloadJobWithDownloadSet findNextDownloadJobAndSetStartingStatus(boolean connectionMetered){
        DownloadJobWithDownloadSet nextJob = findNextDownloadJob(connectionMetered);
        if(nextJob != null){
            updateJobStatus(nextJob.getDownloadJobId(), NetworkTask.STATUS_STARTING);
        }

        return nextJob;
    }

    /**
     * Get a LiveData object for the given DownloadJob id.
     *
     * @param id The downloadJobId (prmiary key) of the DownloadJob to find
     *
     * @return LiveData for the given DownloadJob
     */
    @UmQuery("SELECT * From DownloadJob where id = :id")
    public abstract UmLiveData<DownloadJob> getByIdLive(int id);

    /**
     * Get a LiveData as DownloadJobWithTotals, which includes totals generated by SQL SUM functions
     * for the total number of container downloads and the total size.
     *
     * @param id DownloadJobId of the DownloadJob
     *
     * @return LiveData with DownloadJobWithTotals for the given DownloadJob
     */
    @UmQuery("SELECT DownloadSet.*, " +
            " (SELECT COUNT(*) FROM DownloadSetItem WHERE DownloadSetItem.downloadSetId = DownloadSet.id) AS numJobItems, " +
            " (SELECT SUM(DownloadSetItem.downloadLength) FROM DownloadSetItem WHERE DownloadSetItem.downloadSetId = DownloadSet.id) AS totalDownloadSize " +
            " FROM DownloadSet Where DownloadSet.id= :id")
    public abstract UmLiveData<DownloadJobWithTotals> findByIdWithTotals(int id);

    /**
     * Find the most recently created DownloadJob
     *
     * @return the most recently created DownloadJob
     */
    @UmQuery("SELECT * FROM DownloadJob ORDER BY timeCreated DESC")
    public abstract DownloadJob findLastCreatedDownloadJob();


    /**
     * Find the last download job that was requested with a given entryId as one of the entries
     *
     * @param entryId entryId to search for - can be the root entryId, or any child entry
     * @param callback callback to call when done
     */
    public void findLastDownloadJobId(String entryId, UmResultCallback<Integer> callback) {
        findLastDownloadJobIdByDownloadJobItem(entryId, (jobItemJobId) -> {
            if(jobItemJobId != null && jobItemJobId > 0) {
                callback.onDone(jobItemJobId);
            }else{
                findLastDownloadJobIdByCrawlJobItem(entryId, callback);
            }
        });
    }

    public abstract void findLastDownloadJobIdByDownloadJobItem(String entryId, UmResultCallback<Integer> callback);

    public abstract void findLastDownloadJobIdByCrawlJobItem(String entryId, UmResultCallback<Integer> callback);

    public abstract UmLiveData<Boolean> findAllowMeteredDataUsageLive(int downloadJobId);

    public abstract void updateAllowMeteredDataUsage(int downloadJobId, boolean allowMeteredDataUsage,
                                                     UmResultCallback<Void> callback);

    /**
     * Get a list of all DownloadJob items. Used for debugging purposes.
     *
     * @return A list of all DownloadJob entity objects
     */
    public abstract List<DownloadJob> findAll();

}
