package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonCustomFieldWithPersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.PersonField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON_TYPE;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;
import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_HEADER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_ADDRESS;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_ATTENDANCE;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_BIRTHDAY;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_CLASSES;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_FULL_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_LAST_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NUMBER;

/**
 * PersonEditPresenter : This is responsible for generating the Edit data along with its Custom
 * Fields. It is also responsible for updating the data and checking for changes and handling
 * Done with Save or Discard.
 *
 */
public class PersonEditPresenter extends UstadBaseController<PersonEditView> {


    private UmLiveData<Person> personLiveData;

    //Headers and Fields
    private List<PersonDetailPresenterField> headersAndFields;

    private long personUid;

    private Person mUpdatedPerson;

    //OG person before Done/Save/Discard clicked.
    private Person mOriginalValuePerson;

    private UmProvider<ClazzWithNumStudents> assignedClazzes;

    //The custom fields' values
    private Map<Long, PersonCustomFieldWithPersonCustomFieldValue> customFieldWithFieldValueMap;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    private PersonDao personDao = repository.getPersonDao();

    private String newPersonString = "";

    private List<PersonCustomFieldValue> customFieldsToUpdate;

    private PersonCustomFieldValueDao personCustomFieldValueDao =
            repository.getPersonCustomFieldValueDao();

    private int currentRole = -1;

    private long enrollToClazz = -1L;

    /**
     * Presenter's constructor where we are getting arguments and setting the newly/editable
     * personUid
     *
     * @param context Android context
     * @param arguments Arguments from the Activity passed here.
     * @param view  The view that called this presenter (PersonEditView->PersonEditActivity)
     */
    public PersonEditPresenter(Object context, Hashtable arguments, PersonEditView view) {
        super(context, arguments, view);

        if (arguments.containsKey(ARG_PERSON_UID)) {
            personUid = Long.parseLong(arguments.get(ARG_PERSON_UID).toString());
        }

        if(arguments.containsKey(ARG_NEW_PERSON)){
            newPersonString = arguments.get(ARG_NEW_PERSON).toString();
        }

        if(arguments.containsKey(ARG_NEW_PERSON_TYPE)){
            currentRole = (int) arguments.get(ARG_NEW_PERSON_TYPE);
        }

        if(arguments.containsKey(ARG_CLAZZ_UID)){
            enrollToClazz = (Long) arguments.get(ARG_CLAZZ_UID);
        }

        customFieldsToUpdate = new ArrayList<>();

    }

    /**
     * Presenter's Overridden onCreate that: Gets the mPerson LiveData and observe it.
     * @param savedState    The saved state
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        PersonCustomFieldValueDao personCustomFieldValueDao = repository.getPersonCustomFieldValueDao();
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                repository.getPersonDetailPresenterFieldDao();
        PersonCustomFieldDao personCustomFieldDao = repository.getPersonCustomFieldDao();


        if(newPersonString.equals("true")){
            view.updateToolbarTitle(impl.getString(MessageID.new_person, context));
        }

        //Get all the currently set headers and fields:
        //personDetailPresenterFieldDao.findAllPersonDetailPresenterFields(
        personDetailPresenterFieldDao.findAllPersonDetailPresenterFieldsEditMode(
                new UmCallback<List<PersonDetailPresenterField>>() {
            @Override
            public void onSuccess(List<PersonDetailPresenterField> result) {

                headersAndFields = result;

                //Get all custom fields (if any)
                personCustomFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID, new UmCallback<List<PersonField>>() {
                    @Override
                    public void onSuccess(List<PersonField> customFields) {
                        //Create a list of every custom fields supposed to be and fill them with
                        //blank values that will be used to display empty fields. If those fields
                        //exists, then they will get replaced in the next Dao call.
                        customFieldWithFieldValueMap = new HashMap<>();
                        for(PersonField customField:customFields){

                            //the blank custom field value.
                            PersonCustomFieldValue blankCustomValue = new PersonCustomFieldValue();
                            blankCustomValue.setFieldValue("");

                            //Create a (custom field + custom value) map object
                            PersonCustomFieldWithPersonCustomFieldValue blankCustomMap =
                                    new PersonCustomFieldWithPersonCustomFieldValue();
                            blankCustomMap.setFieldName(customField.getFieldName());
                            blankCustomMap.setLabelMessageId(customField.getLabelMessageId());
                            blankCustomMap.setFieldIcon(customField.getFieldIcon());
                            blankCustomMap.setCustomFieldValue(blankCustomValue);

                            //Set the custom field and the field+value object to the map.
                            customFieldWithFieldValueMap.put(customField.getPersonCustomFieldUid(),
                                    blankCustomMap);
                        }

                        //Get all the custom fields and their values for this person (if applicable)
                        personCustomFieldValueDao.findByPersonUidAsync2(personUid,
                            new UmCallback<List<PersonCustomFieldWithPersonCustomFieldValue>>() {
                                @Override
                                public void onSuccess(List<PersonCustomFieldWithPersonCustomFieldValue> result) {

                                    //Store the values and fields in this Map

                                    for (PersonCustomFieldWithPersonCustomFieldValue fieldWithFieldValue : result) {
                                        customFieldWithFieldValueMap.put(
                                                fieldWithFieldValue.getPersonCustomFieldUid(), fieldWithFieldValue);
                                    }

                                    //Get person live data and observe
                                    personLiveData = personDao.findByUidLive(personUid);
                                    //Observe the live data
                                    personLiveData.observe(PersonEditPresenter.this,
                                            PersonEditPresenter.this::handlePersonValueChanged);
                                }

                                @Override
                                public void onFailure(Throwable exception) {

                                }
                        });

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });


            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    /**
     * Updates the pic of the person after taken to the Person object directly
     *
     * @param picPath    The whole path of the picture.
     */
    public void updatePersonPic(String picPath){
        personDao.findByUidAsync(personUid, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person personWithPic) {
                personWithPic.setImagePath(picPath);
                personDao.updateAsync(personWithPic, new UmCallback<Integer>(){

                    @Override
                    public void onSuccess(Integer result) {
                        System.out.println("Success updating person with Pic..");
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Generates live data for Clazz list to be assigned to the current Person being edited.
     */
    public void generateAssignedClazzesLiveData(){
        ClazzDao clazzDao = repository.getClazzDao();
        assignedClazzes = clazzDao.findAllClazzesByPersonUid(personUid);
        updateClazzListProviderToView();
    }

    /**
     * Updates the Clazz List provider of type ClazzWithNumStudents that is set on this Presenter to
     * the View.
     */
    private void updateClazzListProviderToView(){
        view.setClazzListProvider(assignedClazzes);
    }


    /**
     * Common method to set edit fields up for the current Person Editing.
     *
     * @param thisPerson The person being edited
     * @param allFields The Fields
     * @param thisView  The View
     * @param valueMap  The Custom fields value map
     */
    private void setFieldsOnView(Person thisPerson, List<PersonDetailPresenterField> allFields,
                                 PersonEditView thisView,
                                 Map<Long, PersonCustomFieldWithPersonCustomFieldValue> valueMap){

        Locale currnetLocale = Locale.getDefault();

        if(thisPerson.getImagePath() != null){
            view.updateImageOnView(thisPerson.getImagePath());
        }

        //Clear all view before setting fields ?
        view.clearAllFields();

        for(PersonDetailPresenterField field : allFields) {

            String thisValue = "";

            if(field.getFieldType() == FIELD_TYPE_HEADER) {
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(FIELD_TYPE_HEADER,
                        field.getHeaderMessageId(), null), field.getHeaderMessageId());
                continue;
            }

            if (field.getFieldUid() == PERSON_FIELD_UID_FULL_NAME){
                thisValue = thisPerson.getFirstNames() + " " + thisPerson.getLastName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            }else if (field.getFieldUid() == PERSON_FIELD_UID_FIRST_NAMES) {
                thisValue =  thisPerson.getFirstNames();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_LAST_NAME) {
                thisValue = thisPerson.getLastName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_ATTENDANCE) {
                //TODO: Check if we are still using attendance

                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_CLASSES) {
                thisValue = "Class Name ...";
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER) {
                thisValue = thisPerson.getFatherName() + " (" + thisPerson.getFatherNumber() +")";
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);

            } else if(field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER){
                thisValue = thisPerson.getMotherName() + " (" + thisPerson.getMotherNum() + ")";
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NAME) {
                thisValue = thisPerson.getFatherName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NAME) {
                thisValue = thisPerson.getMotherName();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_FATHER_NUMBER) {
                thisValue = thisPerson.getFatherNumber();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if (field.getFieldUid() == PERSON_FIELD_UID_MOTHER_NUMBER) {
                thisValue = thisPerson.getMotherNum();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }

            else if (field.getFieldUid() == PERSON_FIELD_UID_ADDRESS) {
                thisValue = thisPerson.getAddress();
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }
            else if(field.getFieldUid() == PERSON_FIELD_UID_BIRTHDAY){
                thisValue = UMCalendarUtil.getPrettyDateFromLong(
                        thisPerson.getDateOfBirth(), currnetLocale);
                thisView.setField(field.getFieldIndex(), field.getFieldUid(),
                        new PersonDetailViewField(field.getFieldType(),
                        field.getLabelMessageId(),field.getFieldIcon()), thisValue);
            }else  {//this is actually a custom field
                int messageLabel = 0;
                String iconName = null;
                String fieldValue = null;
                if(valueMap.get(field.getFieldUid()) != null){
                    if(valueMap.get(field.getFieldUid()).getLabelMessageId() != 0){
                        messageLabel = valueMap.get(field.getFieldUid()).getLabelMessageId();
                    }
                    if(valueMap.get(field.getFieldUid()).getFieldIcon() != null){
                        iconName = valueMap.get(field.getFieldUid()).getFieldIcon();
                    }
                    if(valueMap.get(field.getFieldUid()).getCustomFieldValue().getFieldValue() != null){
                        fieldValue = valueMap.get(field.getFieldUid())
                                .getCustomFieldValue().getFieldValue();
                    }
                }
                thisView.setField(
                        field.getFieldIndex(),
                        field.getFieldUid(),
                        new PersonDetailViewField(
                                field.getFieldType(),
                                messageLabel,
                                iconName
                        ), fieldValue

                );
            }
        }
    }

    /**
     * Updates fields of Person w.r.t field Id given and the value. This method does NOT persist
     * the data.
     *
     * @param updateThisPerson the Person object to update values for.
     * @param fieldcode The field Uid that needs to get updated
     * @param value The value to update the Person's field.
     * @return  The updated Person with the updated field.
     */
    private Person updateSansPersistPersonField(Person updateThisPerson,
                                                long fieldcode, Object value){

        //Update Core fields
        if (fieldcode == PERSON_FIELD_UID_FIRST_NAMES) {
            updateThisPerson.setFirstNames((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_LAST_NAME) {
            updateThisPerson.setLastName((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NAME) {
            updateThisPerson.setFatherName((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_FATHER_NUMBER) {
            updateThisPerson.setFatherNumber((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NAME) {
            updateThisPerson.setMotherName((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_MOTHER_NUMBER) {
            updateThisPerson.setMotherNum((String) value);

        } else if (fieldcode == PERSON_FIELD_UID_BIRTHDAY) {
            System.out.println(" ");
            //TODO: Change and work this
            //updateThisPerson.setDateOfBirth((Long) value);

        } else if (fieldcode == PERSON_FIELD_UID_ADDRESS) {
            updateThisPerson.setAddress((String) value);

        } else {
            //This is actually a custom field.

            personCustomFieldValueDao.findCustomFieldByFieldAndPersonAsync(fieldcode,
                    updateThisPerson.getPersonUid(), new UmCallback<PersonCustomFieldValue>() {
                @Override
                public void onSuccess(PersonCustomFieldValue result) {
                    if(result != null) {
                        result.setFieldValue(value.toString());
                        customFieldsToUpdate.add(result);
                    }else{
                        //Create the custom field
                        PersonCustomFieldValue newCustomValue = new PersonCustomFieldValue();
                        newCustomValue.setPersonCustomFieldValuePersonUid(updateThisPerson.getPersonUid());
                        newCustomValue.setPersonCustomFieldValuePersonCustomFieldUid(fieldcode);
                        personCustomFieldValueDao.insert(newCustomValue);
                        newCustomValue.setFieldValue(value.toString());
                        customFieldsToUpdate.add(newCustomValue);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }

        return updateThisPerson;

    }

    /**
     * This method tells the View what to show. It will set every field item to the view.
     * The Live Data handler calls this method when the data (via Live data) is updated.
     *
     * @param person The person that needs to be displayed.
     */
    private void handlePersonValueChanged(Person person) {
        //set the og person value
        if(mOriginalValuePerson == null)
            mOriginalValuePerson = person;

        if(mUpdatedPerson == null || !mUpdatedPerson.equals(person)) {
            //set fields on the view as they change and arrive.
            setFieldsOnView(person, headersAndFields, view,
                    customFieldWithFieldValueMap);

            mUpdatedPerson = person;
        }

    }

    /**
     * Handles every field Edit (focus changed).
     *
     * @param fieldCode The field code that needs editing
     * @param value The new value of the field from the view
     */
    public void handleFieldEdited(long fieldCode, Object value) {
        mUpdatedPerson = updateSansPersistPersonField(mUpdatedPerson, fieldCode, value);
    }

//    /**
//     * Handle discarding the edits done so far when leaving the activity / clicking discard.
//     */
//    public void handleClickDiscardChanges(){
//        //TODO:  Make use of this method?
//        //Update dao with mOriginalValuePerson
//        personDao.insert(mOriginalValuePerson);
//
//    }

    /**
     * Click handler when Add new Class clicked on Classes section
     */
    public void handleClickAddNewClazz(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_PERSON_UID, personUid);
        impl.go(PersonDetailEnrollClazzView.VIEW_NAME, args, context);
    }

    /**
     * Done click handler on the Edit / Enrollment page: Clicking done will persist and save it and
     * end the activity.
     *
     */
    public void handleClickDone(){
        mUpdatedPerson.setActive(true);
        personDao.updateAsync(mUpdatedPerson, new UmCallback<Integer>(){

            @Override
            public void onSuccess(Integer result) {

                //Update the custom fields
                personCustomFieldValueDao.updateListAsync(customFieldsToUpdate,
                        new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        //Close the activity.
                        view.finish();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Overridden. Does'nt do anything.
     */
    @Override
    public void setUIStrings() {

    }

}
