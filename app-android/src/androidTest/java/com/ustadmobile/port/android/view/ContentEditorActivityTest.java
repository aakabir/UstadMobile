package com.ustadmobile.port.android.view;

import android.support.design.widget.BottomSheetBehavior;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.toughra.ustadmobile.R;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Test class which tests {@link ContentEditorActivity} to make sure it behaves as expected
 * under different circumstances when action performed.
 *
 * @author kileha3
 */

@RunWith(AndroidJUnit4.class)
public class ContentEditorActivityTest {

    private BottomSheetBehavior bottomSheetBehavior;

    private final static int RECYCLER_ITEM_INDEX = 0;

    @Rule
    public ActivityTestRule<ContentEditorActivity> mActivityRule = new ActivityTestRule<>(
            ContentEditorActivity.class,false,true);

    @Before
    public void beforeStartingTheTests(){
        bottomSheetBehavior = mActivityRule.getActivity().getFormattingBottomSheetBehavior();
    }
    @Test
    public void givenFormatIconIsClicked_whenBottomSheetIsCollapsed_thenShouldExpandTheBottomSheet() {
        onView(withId(R.id.content_action_format)).perform(click());
        assertTrue("The BottomSheet should be settling or expanded",
                bottomSheetBehavior.getState() <= BottomSheetBehavior.STATE_EXPANDED);
    }

    @Test
    public void givenFormatTypeButtonIsClickedOnList_whenEditingIsEnabled_thenShouldActivateAndApplyFormatting(){
        onView(withId(R.id.content_action_format)).perform(click());
        Matcher<View> matcher = allOf(withId(R.id.formats_list),isDisplayed());
        onView(matcher)
                .perform(RecyclerViewActions.actionOnItemAtPosition(RECYCLER_ITEM_INDEX, click()));
        List<ContentEditorActivity.ContentFormat> formats =
                ContentEditorActivity.mFormatting.get(RECYCLER_ITEM_INDEX);
        assertTrue("Formatting type should be applied and button should be activated",
                formats.get(RECYCLER_ITEM_INDEX).isActive());
    }

    @Test
    public void givenMultipleChoiceButtonIsClicked_whenEditingIsEnabled_thenShouldInsertMultipleChoiceTemplate(){
        onView(withId(R.id.content_editor_insert)).perform(click());

    }

    @Test
    public void givenFillInTheBlanksChoiceButtonIsClicked_whenEditingIsEnabled_thenShouldInsertMultipleChoiceTemplate(){

    }

    @Test
    public void givenMultimediaButtonIsClicked_whenEditingIsEnabled_thenShouldLetUserChooseTheSource(){

    }

    @Test
    public void givenMultimediaSourceIsSelected_whenEditingIsEnabled_thenShouldInsertMultimediaObject(){

    }



}
