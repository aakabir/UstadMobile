package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.PeopleListView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;


/**
 * The PeopleList's Presenter - responsible for the logic behind slowing all the people regardless
 * of what class they are in. This presenter is also responsible in Adding a person handler and in
 * going to PersonDetail View to see more information about that Person.
 */
public class PeopleListPresenter
        extends CommonHandlerPresenter<PeopleListView> {

    //Provider 
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    public PeopleListPresenter(Object context, Hashtable arguments, PeopleListView view) {
        super(context, arguments, view);

    }

    /**
     * In order:
     *      1. Gets all people via the database as UmProvider and sets it to the view.
     *
     * @param savedState The saved state.
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        personWithEnrollmentUmProvider = UmAppDatabase.getInstance(context).getPersonDao()
                .findAllPeopleWithEnrollment();
        setPeopleProviderToView();

    }

    /**
     * Sets the people list provider set in the Presenter to the View.
     */
    private void setPeopleProviderToView(){
        view.setPeopleListProvider(personWithEnrollmentUmProvider);
    }

    /**
     * Handles what happens when you click Add Person button (Floating Action Button in Android).
     * This will create a new Person and persist it to the database. It will then pass this new
     * Person's Uid to PersonEdit screen to edit that. It will also add blank Custom Field Values
     * for that new Person so that those can be edited as well.
     */
    public void handleClickPrimaryActionButton() {
        //Goes to PersonEditActivity with currentClazzUid passed as argument
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Person newPerson = new Person();
        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();
        PersonCustomFieldDao personFieldDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldDao();
        PersonCustomFieldValueDao customFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();

        personDao.insertAsync(newPerson, new UmCallback<Long>() {

            @Override
            public void onSuccess(Long result) {

                //Also create null Custom Field values so it shows up in the Edit screen.
                personFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID,
                        new UmCallback<List<PersonField>>() {
                    @Override
                    public void onSuccess(List<PersonField> allCustomFields) {

                        for (PersonField everyCustomField:allCustomFields){
                            PersonCustomFieldValue cfv = new PersonCustomFieldValue();
                            cfv.setPersonCustomFieldValuePersonCustomFieldUid(
                                    everyCustomField.getPersonCustomFieldUid());
                            cfv.setPersonCustomFieldValuePersonUid(result);
                            cfv.setPersonCustomFieldValueUid(customFieldValueDao.insert(cfv));
                        }

                        Hashtable<String, Object> args = new Hashtable<>();
                        args.put(ARG_PERSON_UID, result);
                        args.put(ARG_NEW_PERSON, "true");
                        impl.go(PersonEditView.VIEW_NAME, args, view.getContext());
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });


            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }


    /**
     * The primary action handler on the people list (for every item) is to open that Person's
     * Detail (ie: PersonDetailView)
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    @Override
    public void handleCommonPressed(Object arg) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_PERSON_UID, arg);
        impl.go(PersonDetailView.VIEW_NAME, args, view.getContext());
    }

    /**
     * The secondary action handler on the people list (for every item) is nothing here.
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        //No secondary action for every item here.
    }

    /**
     * Just overriding. Does nothing.
     */
    @Override
    public void setUIStrings() {

    }
}
