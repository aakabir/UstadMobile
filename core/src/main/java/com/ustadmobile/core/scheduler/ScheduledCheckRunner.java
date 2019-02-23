package com.ustadmobile.core.scheduler;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.ScheduledCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScheduledCheckRunner implements Runnable{

    private ScheduledCheck scheduledCheck;

    private UmAppDatabase database;

    private UmAppDatabase dbRepository;

    public ScheduledCheckRunner(ScheduledCheck scheduledCheck, UmAppDatabase database,
                                UmAppDatabase dbRepository) {
        this.scheduledCheck = scheduledCheck;
        this.database = database;
        this.dbRepository = dbRepository;
    }

    @Override
    public void run() {
        if(scheduledCheck.getCheckType() == ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER) {
            Map<String, String> params = UMFileUtil.parseParams(scheduledCheck.getCheckParameters(),
                    ';');
            long clazzLogUid = Long.parseLong(params.get(ScheduledCheck.PARAM_CLAZZ_LOG_UID));
            ClazzLog clazzLog = dbRepository.getClazzLogDao().findByUid(clazzLogUid);
            if(!clazzLog.isDone() || clazzLog.isCanceled()) {
                String clazzName = dbRepository.getClazzDao().getClazzName(
                        clazzLog.getClazzLogClazzUid());
                List<ClazzMemberWithPerson> teachers = dbRepository.getClazzMemberDao()
                        .findClazzMemberWithPersonByRoleForClazzUidSync(
                                clazzLog.getClazzLogClazzUid(), ClazzMember.ROLE_TEACHER);
                List<FeedEntry> newFeedEntries = new ArrayList<>();
                for(ClazzMemberWithPerson teacher : teachers) {
                    long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            teacher.getClazzMemberPersonUid(), clazzLogUid,
                            ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER);
                    newFeedEntries.add(new FeedEntry(feedEntryUid, "Record attendance",
                            "Record attendance for class",
                            ClassLogDetailView.VIEW_NAME + "?" + ClazzListView.ARG_CLAZZ_UID +
                                    "=" + clazzLog.getClazzLogClazzUid(), clazzName,
                                    teacher.getClazzMemberPersonUid()));
                }
                dbRepository.getFeedEntryDao().insertList(newFeedEntries);
            }
        }

        //delete this item from database - no longer needed
        database.getScheduledCheckDao().deleteCheck(scheduledCheck);
    }


}