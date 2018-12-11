package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class ClazzLogDao implements SyncableDao<ClazzLog, ClazzLogDao> {

    public static class NumberOfDaysClazzesOpen{
        long date;
        int number;

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }

    @UmInsert
    public abstract long insert(ClazzLog entity);

    @UmInsert
    public abstract void insertAsync(ClazzLog entity, UmCallback<Long> resultObject);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    public abstract ClazzLog findByUid(long uid);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzClazzUid = :clazzid AND logDate = :date")
    public abstract ClazzLog findByClazzIdAndDate(long clazzid, long date);

    @UmQuery("SELECT * FROM ClazzLog WHERE clazzClazzUid = :clazzid and logDate = :date")
    public abstract void findByClazzIdAndDateAsync(long clazzid, long date,
                                                   UmCallback<ClazzLog> resultObject);

    @UmQuery("SELECT * FROM ClazzLog")
    public abstract List<ClazzLog> findAll();

    @UmQuery("UPDATE ClazzLog SET done = 1 where clazzLogUid = :clazzLogUid ")
    public abstract void updateDoneForClazzLogAsync(long clazzLogUid, UmCallback<Integer> callback);

    @UmQuery("SELECT * FROM ClazzLog where clazzClazzUid = :clazzUid")
    public abstract UmProvider<ClazzLog> findByClazzUid(long clazzUid);

    @UmQuery("SELECT * FROM ClazzLog where clazzClazzUid = :clazzUid AND done = 1")
    public abstract UmProvider<ClazzLog> findByClazzUidThatAreDone(long clazzUid);

    @UmQuery("UPDATE ClazzLog SET numPresent = :numPresent,  numAbsent = :numAbsent, " +
            "numPartial = :numPartial WHERE clazzLogUid = :clazzLogUid")
    public abstract void updateClazzAttendanceNumbersAsync(long clazzLogUid, int numPresent,
                                                           int numAbsent, int numPartial,
                                                           UmCallback<Void> callback);

   

    @UmQuery ("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            " GROUP BY ClazzLog.logDate")
    public abstract void getNumberOfClassesOpenForDateLocationClazzes(long fromDate, long toDate,
            UmCallback<List<NumberOfDaysClazzesOpen>> resultList);

    public void createClazzLogForDate(long currentClazzUid, long currentLogDate,
                                      UmCallback<Long> callback){

        findByClazzIdAndDateAsync(currentClazzUid, currentLogDate, new UmCallback<ClazzLog>() {
            @Override
            public void onSuccess(ClazzLog result) {
                if(result != null){
                    callback.onSuccess(result.getClazzClazzUid());
                }else{
                    //Create one
                    ClazzLog newClazzLog = new ClazzLog();
                    newClazzLog.setLogDate(currentLogDate);
                    newClazzLog.setTimeRecorded(System.currentTimeMillis());
                    newClazzLog.setDone(false);
                    newClazzLog.setClazzClazzUid(currentClazzUid);
                    insertAsync(newClazzLog, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            newClazzLog.setClazzLogUid(result);
                            callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

}