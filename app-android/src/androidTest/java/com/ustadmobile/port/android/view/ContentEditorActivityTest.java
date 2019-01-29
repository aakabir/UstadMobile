package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.web.matcher.DomMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ContentEditorView;
import com.ustadmobile.port.android.umeditor.UmFormat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.assertion.WebViewAssertions.webContent;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_BOLD;
import static com.ustadmobile.core.view.ContentEditorView.TEXT_FORMAT_TYPE_ITALIC;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Test class which tests {@link ContentEditorActivity} to make sure it behaves as expected
 * under different circumstances when editing mode is ON
 *
 * @author kileha3
 */

@RunWith(AndroidJUnit4.class)
public class ContentEditorActivityTest {

    private BottomSheetBehavior contentOptionBottomSheet;

    private BottomSheetBehavior multimediaSourceBottomSheet;

    private static final int MAX_WAIT_TIME = 2;

    @Rule
    public ActivityTestRule<ContentEditorActivity> mActivityRule =
            new ActivityTestRule<ContentEditorActivity>(ContentEditorActivity.class,false,true){
        @Override
        protected Intent getActivityIntent() {
            Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent args = new Intent(mContext, ContentEditorActivity.class);
            args.putExtra(ContentEditorView.CONTENT_ENTRY_FILE_UID,"1");
            return args;
        }

    };

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    );

    @Before
    public void setUp() throws InterruptedException {
       contentOptionBottomSheet = mActivityRule.getActivity().getContentOptionsBottomSheetBehavior();
        multimediaSourceBottomSheet = mActivityRule.getActivity().getMediaSourceBottomSheetBehavior();

        //Wait for proper initialization
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));
    }

    @Test
    public void givenButtonToSwitchEditingModeOn_whenClicked_thenShouldTurnEditingModeOn()
            throws InterruptedException {

        //switch on editing mode
        onView(withId(R.id.btn_start_editing)).perform(click());

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        assertTrue("Editing mode was switched ON successfully",
                mActivityRule.getActivity().isEditorInitialized());
    }


    @Test
    public void givenInsertContentIconIsClicked_whenEditingIsEnabledAndBottomSheetIsCollapsed_thenShouldExpandContentOptionBottomSheet()
            throws InterruptedException {

        //switch on editing mode
        onView(withId(R.id.btn_start_editing)).perform(click());

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));
        onView(withId(R.id.content_action_insert)).perform(click());

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        assertTrue("Editing mode was enabled",
                mActivityRule.getActivity().isEditorInitialized());

        assertEquals("Content option BottomSheet was expanded",
                contentOptionBottomSheet.getState(), BottomSheetBehavior.STATE_EXPANDED);
    }


    @Test
    public void givenFormatTypeMenuIsClicked_whenEditingIsEnabled_thenShouldActivateAndApplyFormatting()
            throws InterruptedException {

        //switch on editing mode
        onView(withId(R.id.btn_start_editing)).perform(click());

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));
        //add content to the editor
        mActivityRule.getActivity().insertTestContent("Dummy Text on Editor");

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        //Select all added content
        mActivityRule.getActivity().selectAllTestContent();

        //apply formatting (Bold & Italic)
        onView(withId(R.id.content_action_bold)).perform(click());
        onView(withId(R.id.content_action_italic)).perform(click());

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        //Get formatting type reference
        ContentEditorActivity.UmFormatHelper umFormatHelper =
                mActivityRule.getActivity().getUmFormatHelper();
        UmFormat boldFormat = umFormatHelper.getFormatByCommand(TEXT_FORMAT_TYPE_BOLD);
        UmFormat italicFormat = umFormatHelper.getFormatByCommand(TEXT_FORMAT_TYPE_ITALIC);


        assertTrue("Editing mode was enabled",
                mActivityRule.getActivity().isEditorInitialized());

        assertTrue("Bold formatting was applied to the text",boldFormat.isActive());

        assertTrue("Italic formatting was applied to the text",italicFormat.isActive());
    }

    @Test
    public void givenMultipleChoiceButtonIsClicked_whenEditingIsEnabled_thenShouldInsertMultipleChoiceTemplate()
            throws InterruptedException {

        //switch on editing mode
        onView(withId(R.id.btn_start_editing)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        //insert multiple-choice question
        onView(withId(R.id.content_option_multiplechoice)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        assertTrue("Editing mode was enabled",
                mActivityRule.getActivity().isEditorInitialized());


        onWebView().withTimeout(10000, TimeUnit.MILLISECONDS)
                .check(webContent(DomMatchers.hasElementWithXpath("//div[contains(@data-um-widget, 'multi-choice')]")));
    }

    @Test
    public void givenFillInTheBlanksChoiceButtonIsClicked_whenEditingIsEnabled_thenShouldInsertFillInTheBlanksTemplate()
            throws InterruptedException {

        //switch on editing mode
        onView(withId(R.id.btn_start_editing)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        //insert fill in the blanks question
        onView(withId(R.id.content_option_filltheblanks)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        assertTrue("Editing mode was enabled",
                mActivityRule.getActivity().isEditorInitialized());

        onWebView().withTimeout(10000, TimeUnit.MILLISECONDS)
                .check(webContent(DomMatchers.hasElementWithXpath("//div[contains(@data-um-widget, 'fill-the-blanks')]")));
    }

    @Test
    public void givenMultimediaChoiceIsClicked_whenEditingIsEnabled_thenShouldLetUserChooseTheSource()
            throws InterruptedException {

        //switch on editing mode
        onView(withId(R.id.btn_start_editing)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        //open content chooser
        onView(withId(R.id.content_action_insert)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        //insert multiple-choice question
        onView(withId(R.id.content_option_multimedia)).perform(click());
        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_WAIT_TIME));

        assertTrue("Editing mode was enabled",
                mActivityRule.getActivity().isEditorInitialized());

        assertEquals("Multimedia source bottom sheen was expanded",
                multimediaSourceBottomSheet.getState(), BottomSheetBehavior.STATE_EXPANDED);

    }

}
