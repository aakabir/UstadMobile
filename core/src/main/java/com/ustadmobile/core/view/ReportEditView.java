package com.ustadmobile.core.view;


import java.util.HashMap;

/**
 * ReportEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ReportEditView extends UstadView {

    String VIEW_NAME = "ReportEdit";

    String ARG_REPORT_NAME = "ReportName";
    String ARG_REPORT_LINK = "ReportLink";
    String ARG_LOCATIONS_SET = "Locations";

    String ARG_FROM_DATE = "fromDate";
    String ARG_TO_DATE = "toDate";
    String ARG_LOCATION_LIST = "locationList";
    String ARG_CLAZZ_LIST = "clazzList";
    String ARG_GENDER_DISAGGREGATE = "genderDisaggregate";
    String ARG_STUDENT_IDENTIFIER_NUMBER = "studentIdentifierNumber";
    String ARG_STUDENT_IDENTIFIER_PERCENTAGE = "studentIdentifierPercentage";


    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Populate the view with the list of time periods. The hashmap also has every option mapped
     * to an id to handle.
     *
     * @param options   A hashmap of the id and the string option of time periods
     */
    void populateTimePeriod(HashMap<Integer, String> options);

    /**
     * Updates locations list as string on the locations text field on the view.
     *
     * @param locations The location selected string (eg: "Location1, Location2")
     */
    void updateLocationsSelected(String locations);

    /**
     * Sets if the "Disaggregate by gender" checkbox is set
     * @param byGender
     */
    void updateGenderDisaggregationSet(boolean byGender);

    /**
     * Updates the report name heading.
     *
     * @param name  The report name
     */
    void updateReportName(String name);

    /**
     * Shows the custom from/to date selector when Custom date option selected on time period spinner
     */
    void showCustomDateSelector();

    /**
     * Show attendance threshold options or remove them.
     * @param show  true if yes. false if no
     */
    void showAttendanceThresholdView(boolean show);

    /**
     * Show radio button group or not (to show student numbers and percentages(
     * @param show  true is yes, false if no
     */
    void showShowStudentNumberPercentageView(boolean show);
}