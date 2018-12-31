package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Role;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;


/**
 * The ClazzDetail Presenter - responsible for displaying the details of the Clazz who's detail we
 * want to see.
 * This is usually called first when we click on a Class from a list of Classes to get into it.
 */
public class ClazzDetailPresenter
        extends UstadBaseController<ClassDetailView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
    private ClazzDao clazzDao = repository.getClazzDao();

    private Long loggedInPersonUid = 0L;

    public ClazzDetailPresenter(Object context, Hashtable arguments, ClassDetailView view) {
        super(context, arguments, view);

        //Get Clazz Uid and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();
    }

    /**
     * In Order:
     *      1. Just set the title of the toolbar.
     *
     * @param savedState    The savedState
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update toolbar title
        updateToolbarTitle();

        //Permission check
        checkPermissions();

    }

    public void checkPermissions(){

        clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
            Role.PERMISSION_CLAZZ_UPDATE,
            new UmCallbackWithDefaultValue<>(false, new UmCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    view.setSettingsVisibility(result);
                    clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                        Role.PERMISSION_CLAZZ_VIEW_ATTENDANCE, new UmCallbackWithDefaultValue<>(false,
                        new UmCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                view.setAttendanceVisibility(true);
                                clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                                    Role.PERMISSION_CLAZZ_VIEW_SEL, new UmCallbackWithDefaultValue<>(false,
                                    new UmCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean result) {
                                            view.setSELVisibility(result);
                                            clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                                                Role.PERMISSION_CLAZZ_VIEW_ACTIVITY, new UmCallbackWithDefaultValue<>(false,
                                                new UmCallback<Boolean>() {
                                                    @Override
                                                    public void onSuccess(Boolean result) {
                                                        view.setActivityVisibility(result);
                                                        //Setup view pager after all permissions
                                                        view.setupViewPager();
                                                    }
                                                    @Override
                                                    public void onFailure(Throwable exception) {
                                                        exception.printStackTrace();
                                                    }
                                                }));
                                        }
                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    }));
                            }
                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
                            }
                        }));
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            }));

    }

    /**
     * Updates the title of the Clazz after finding it from the database.
     */
    public void updateToolbarTitle(){

        clazzDao.findByUidAsync(currentClazzUid, new UmCallback<Clazz>() {
            @Override
            public void onSuccess(Clazz result) {
                view.setToolbarTitle(result.getClazzName());
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Handles what happens when Class Edit is clicked. This takes the class to the edit page.
     */
    public void handleClickClazzEdit(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        impl.go(ClazzEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }

}
