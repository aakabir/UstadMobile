package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SELQuestionEditPresenter;
import com.ustadmobile.core.view.SELQuestionEditView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;


/**
 * The SELQuestionEdit activity. This Activity extends UstadBaseActivity and implements
 * SELQuestionEditView. This activity is responsible for editing and viewing an SEL question.
 */
public class SELQuestionEditActivity extends UstadBaseActivity implements SELQuestionEditView {

    private SELQuestionEditPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sel_question_edit);

        //Toolbar:
        Toolbar toolbar = findViewById(R.id.activity_sel_question_edit_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Recycler View:
        RecyclerView mRecyclerView = findViewById(
                R.id.activity_sel_question_edit_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        //Call the Presenter
        mPresenter = new SELQuestionEditPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_done, menu);
        return true;
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is Handling
     * Action Bar button click done. - which will persist the opened / editing SEL question
     *
     * @param item  The item selected (from Menu Item)
     * @return  true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == R.id.menu_catalog_entry_presenter_share) {//If this activity started from other activity
            TextInputLayout newQuestion = findViewById(R.id.activity_sel_question_edit_question);
            CheckBox assignToAllClasses =
                    findViewById(R.id.activity_sel_question_edit_assign_to_all_classes);
            CheckBox allowMultipleNominations =
                    findViewById(R.id.activity_sel_question_edit_allow_multiple_nominations);
            mPresenter.handleClickDone(Objects.requireNonNull(newQuestion.getEditText()).getText().toString(),
                    assignToAllClasses.isChecked(), allowMultipleNominations.isChecked());

            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
