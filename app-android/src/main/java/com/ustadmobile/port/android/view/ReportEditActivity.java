package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportAttendanceGroupedByThresholdsPresenter;
import com.ustadmobile.core.controller.ReportEditPresenter;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


/**
 * The ReportEdit activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ReportEditView
 */
public class ReportEditActivity extends UstadBaseActivity implements ReportEditView,
        SelectClazzesDialogFragment.ClazzSelectDialogListener,
        SelectMultipleTreeDialogFragment.MultiSelectTreeDialogListener,
        SelectAttendanceThresholdsDialogFragment.ThresholdsSelectedDialogListener,
        SelectTwoDatesDialogFragment.CustomTimePeriodDialogListener {

    private Toolbar toolbar;

    private TextView locationsTextView;
    private Spinner timePeriodSpinner;
    private TextView heading;
    private CheckBox genderDisaggregateCheck;
    private ReportEditPresenter mPresenter;
    private TextView classesTextView;
    private TextView attendanceThresholdsTextView;

    private HashMap<String, Long> selectedClasses;
    private HashMap<String, Long> selectedLocations;
    private ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues thresholdValues;

    private RadioGroup studentNumberOrPercentageRadioGroup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_edit);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_edit_toolbar);
        toolbar.setTitle(R.string.choose_report_options);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        locationsTextView = findViewById(R.id.activity_report_edit_location_detail);
        timePeriodSpinner = findViewById(R.id.activity_report_edittime_period_spinner);

        heading = findViewById(R.id.activity_report_edit_report_title);
        genderDisaggregateCheck = findViewById(R.id.activity_report_edit_gender);
        studentNumberOrPercentageRadioGroup = findViewById(R.id.activity_report_edit_show_student_radio_options);

        classesTextView = findViewById(R.id.activity_report_classes_textview);
        attendanceThresholdsTextView =
                findViewById(R.id.activity_report_edit_attendance_threshold_selector);

        //Call the Presenter
        mPresenter = new ReportEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        studentNumberOrPercentageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.activity_report_edit_show_student_number_option) {
                mPresenter.setStudentNumbers(true);
                mPresenter.setStudentPercentages(false);
            }else if(checkedId == R.id.activity_report_edit_show_student_percentage_option) {
                mPresenter.setStudentPercentages(true);
                mPresenter.setStudentNumbers(false);
            }else{
                mPresenter.setStudentPercentages(false);
                mPresenter.setStudentNumbers(false);
            }
        });

        timePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleTimePeriodSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        classesTextView.setOnClickListener(v -> mPresenter.goToSelectClassesDialog());

        locationsTextView.setOnClickListener(v -> mPresenter.goToLocationDialog());

        genderDisaggregateCheck.setOnCheckedChangeListener(
                (buttonView, isChecked) -> mPresenter.setGenderDisaggregate(isChecked));


        attendanceThresholdsTextView.setOnClickListener(v ->
                mPresenter.goToSelectAttendanceThresholdsDialog());

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_report_edit_fab);
        fab.setOnClickListener(v -> mPresenter.handleClickPrimaryActionButton());


    }

    /**
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void populateTimePeriod(HashMap<Integer, String> options) {

        String[] timePeriodPresets = options.values().toArray(new String[options.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, timePeriodPresets);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timePeriodSpinner.setAdapter(adapter);
    }

    @Override
    public void updateLocationsSelected(String locations) {
        locationsTextView.setText(locations);
    }

    @Override
    public void updateGenderDisaggregationSet(boolean byGender) {
        genderDisaggregateCheck.setChecked(byGender);
    }

    @Override
    public void updateReportName(String name) {
        heading.setText(name);
    }

    @Override
    public void showCustomDateSelector() {

    }

    @Override
    public void updateThresholdSelected(String thresholdString) {
        attendanceThresholdsTextView.setText(thresholdString);
    }

    @Override
    public void updateClazzesSelected(String clazzes) {
        classesTextView.setText(clazzes);
    }

    @Override
    public void showAttendanceThresholdView(boolean show) {
        //TODO
        //1: Show/Hide
        //2. Update Constraint Layout
    }

    @Override
    public void showShowStudentNumberPercentageView(boolean show) {
        //TODO:
        //1. Show/Hide
        //2. Update Constraint Layout
    }


    @Override
    public void onSelectClazzesResult(HashMap<String, Long> selectedClazzes) {
        this.selectedClasses = selectedClazzes;
        String classesSelectedString = "";
        Iterator<String> selectedClazzesNameIterator = selectedClazzes.keySet().iterator();
        while(selectedClazzesNameIterator.hasNext()){
            classesSelectedString  += selectedClazzesNameIterator.next();
            if(selectedClazzesNameIterator.hasNext()){
                classesSelectedString += ", ";
            }
        }
        List<Long> selectedClasses = new ArrayList<>(selectedClazzes.values());
        mPresenter.setSelectedClasses(selectedClasses);

        updateClazzesSelected(classesSelectedString);
    }

    @Override
    public void onLocationResult(HashMap<String, Long> selectedLocations) {
        this.selectedLocations = selectedLocations;
        Iterator<String> selectedLocationsNameIterator = selectedLocations.keySet().iterator();
        String locationsSelectedString = "";
        while(selectedLocationsNameIterator.hasNext()){
            locationsSelectedString += selectedLocationsNameIterator.next();
            if(selectedLocationsNameIterator.hasNext()){
                locationsSelectedString += ", ";
            }
        }

        updateLocationsSelected(locationsSelectedString);
    }

    @Override
    public void onThresholdResult(ReportAttendanceGroupedByThresholdsPresenter.ThresholdValues values) {
        this.thresholdValues = values;
        String thresholdString = values.low + "%, " + values.med + "%, " + values.high + "%";
        mPresenter.setThresholdValues(thresholdValues);
        updateThresholdSelected(thresholdString);
    }

    @Override
    public void onCustomTimesResult(long from, long to) {
        mPresenter.setFromTime(from);
        mPresenter.setToTime(to);

        Toast.makeText(
                getApplicationContext(),
                "Custom date from : " + from + " to " + to,
                Toast.LENGTH_SHORT
        ).show();

    }
}