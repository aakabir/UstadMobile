package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ReportSelectionPresenter;
import com.ustadmobile.core.view.ReportSelectionView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * The ReportSelection activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements ReportSelectionView
 */
public class ReportSelectionActivity extends UstadBaseActivity implements ReportSelectionView {

    private Toolbar toolbar;

    private ReportSelectionPresenter mPresenter;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;

    HashMap<String, ExpandableListDataReports> expandableListDataReportsHashMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_report_selection);

        //Toolbar:
        toolbar = findViewById(R.id.activity_report_selection_toolbar);
        toolbar.setTitle(getText(R.string.select_report_type));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Call the Presenter
        mPresenter = new ReportSelectionPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        expandableListView = findViewById(R.id.activity_report_selection_expandable_report_list);


        //new:
        expandableListDataReportsHashMap = ExpandableListDataReports.getDataAll(getApplicationContext());
        expandableListTitle = new ArrayList<>(expandableListDataReportsHashMap.keySet());

        expandableListAdapter = new CustomExpandableListAdapter(this,
                expandableListDataReportsHashMap,  expandableListTitle);


        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(groupPosition -> {});

        expandableListView.setOnGroupCollapseListener(groupPosition -> {});

        //If Groups have no children, go to their link (default: expand)
        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {

            ExpandableListDataReports groupItem = expandableListDataReportsHashMap
                    .get(expandableListTitle.get(groupPosition));
            if(groupItem.children.size() == 0 && !groupItem.reportLink.isEmpty()){

                mPresenter.goToReport(groupItem.name, groupItem.desc, groupItem.reportLink,
                        groupItem.showThreshold, groupItem.showRadioGroup,
                        groupItem.showGenderDisaggregate, groupItem.showClazzes,
                        groupItem.showLocations);
            }

            return false;
        });

        //Go to child's link
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {


            mPresenter.goToReport(expandableListDataReportsHashMap
                    .get(expandableListTitle.get(groupPosition)).children
                        .get(childPosition).name,expandableListDataReportsHashMap
                            .get(expandableListTitle.get(groupPosition)).children
                            .get(childPosition).desc, expandableListDataReportsHashMap
                    .get(expandableListTitle.get(groupPosition)).children
                    .get(childPosition).reportLink,
                    expandableListDataReportsHashMap
                            .get(expandableListTitle.get(groupPosition)).children
                            .get(childPosition).showThreshold,
                    expandableListDataReportsHashMap
                            .get(expandableListTitle.get(groupPosition)).children
                            .get(childPosition).showRadioGroup,
                    expandableListDataReportsHashMap
                            .get(expandableListTitle.get(groupPosition)).children
                            .get(childPosition).showGenderDisaggregate,
                    expandableListDataReportsHashMap
                            .get(expandableListTitle.get(groupPosition)).children
                            .get(childPosition).showClazzes,
                    expandableListDataReportsHashMap
                            .get(expandableListTitle.get(groupPosition)).children
                            .get(childPosition).showLocations);
            return false;
        });

    }


}
