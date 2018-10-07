package com.heaton.funnyvote;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;

import com.heaton.funnyvote.data.Injection;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import at.grabner.circleprogress.CircleProgressView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.DEFAULT)
public class MainScreenTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<MainActivity>(MainActivity.class) {

            };

//    @Before
//    public void setUp() {
//        Injection.provideFirstTimePref(mainActivityActivityTestRule.getActivity()).edit()
//                .putBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, false).commit();
//    }
//
//    @Test
//    public void firstTimeAppStart_ClickIntroductionDialog() {
//        if (Injection.provideFirstTimePref(mainActivityActivityTestRule.getActivity())
//                .getBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL,true)) {
//            onView(withId(R.id.txtTitle)).check(matches(isDisplayed()));
//            onView(withId(R.id.btnFirstOption)).perform(click());
//        }
//    }
//
//    @Test
//    public void clickDrawerUserBoxItem_openUserBoxUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        openDrawer();
//
//        // Start the screen of your activity.
//        onView(withId(R.id.navigation_view))
//                .perform(NavigationViewActions.navigateTo(R.id.navigation_item_list_my_box));
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withId(R.id.imgUserIcon)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickDrawerMainPageItem_openMainPageUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        openDrawer();
//
//        // Start the screen of your activity.
//        onView(withId(R.id.navigation_view))
//                .perform(NavigationViewActions.navigateTo(R.id.navigation_item_main));
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withId(R.id.appBarMain)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickDrawerCreateVote_openCreateVoteUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        openDrawer();
//
//        // Start the screen of your activity.
//        onView(withId(R.id.navigation_view))
//                .perform(NavigationViewActions.navigateTo(R.id.navigation_item_create_vote));
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withId(R.id.edtTitle)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickToolbarCreateVoteItem_openCreateVoteUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        onView(withId(R.id.menu_add)).perform(click());
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withId(R.id.edtTitle)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickDrawerSearch_openSearchUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        try {
//            openDrawer();
//            Thread.sleep(1000);
//            // Start the screen of your activity.
//            onView(withId(R.id.navigation_view))
//                    .perform(NavigationViewActions.navigateTo(R.id.navigation_item_search));
//
//
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withId(R.id.rySearchResult)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickDrawerAccount_openAccountUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        openDrawer();
//
//        // Start the screen of your activity.
//        onView(withId(R.id.navigation_view))
//                .perform(NavigationViewActions.navigateTo(R.id.navigation_item_account));
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withId(R.id.profile_picture)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickDrawerAbout_openAboutUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        openDrawer();
//
//        // Start the screen of your activity.
//        onView(withId(R.id.navigation_view))
//                .perform(NavigationViewActions.navigateTo(R.id.navigation_item_about));
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withId(R.id.btnShareApp)).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void clickToolbarSearchVoteItem_openSearchVoteUi() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withId(R.id.menu_search)).perform(click());
//        IdlingResource idlingResource = new LoadingFragmentIdlingResource((CircleProgressView) mainActivityActivityTestRule
//                .getActivity().findViewById(R.id.circleLoad));
//
//        IdlingRegistry.getInstance().register(idlingResource);
//
//        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("123"), pressKey(KeyEvent.KEYCODE_ENTER));
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        //onView(withId(R.id.menu_search)).perform(click());
//
//
//        onView(withId(R.id.rySearchResult)).check(matches(isDisplayed()));
//        //IdlingRegistry.getInstance().unregister(idlingResource);
//
//    }
//
//    @Test
//    public void doubleBackFromTasksScreen_ExitsApp() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        // From the tasks screen, press back should exit the app.
//
//        assertDoublePressingBackExitsApp();
//    }
//
//    @Test
//    public void backFromAboutScreenAfterMainPage_ExitsApp() {
//        firstTimeAppStart_ClickIntroductionDialog();
//        // This test checks that TasksActivity is a parent of StatisticsActivity
//
//        // Open the stats screen
//        openDrawer();
//
//        onView(withId(R.id.navigation_view))
//                .perform(NavigationViewActions.navigateTo(R.id.navigation_item_about));
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withId(R.id.btnShareApp)).check(matches(isDisplayed()));
//        pressBack();
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withId(R.id.appBarMain)).check(matches(isDisplayed()));
//
//        // Pressing back should exit app
//
//        assertDoublePressingBackExitsApp();
//    }
//
//    private void assertDoublePressingBackExitsApp() {
//        try {
//            pressBack();
//            onView(withText(R.string.wall_item_toast_double_click_to_exit))
//                    .inRoot(withDecorView(not(is(mainActivityActivityTestRule.getActivity()
//                            .getWindow().getDecorView())))).check(matches(isDisplayed()));
//            pressBack();
//            fail("Should kill the app and throw an exception");
//        } catch (NoActivityResumedException e) {
//            // Test OK
//        }
//    }
//
//    private void openDrawer() {
//        try {
//            onView(withId(R.id.drawer_layout))
//                    .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
//                    .perform(DrawerActions.open()); // Open Drawer
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    class LoadingFragmentIdlingResource implements IdlingResource {
//        private CircleProgressView circleProgressView;
//        private ResourceCallback resourceCallback;
//
//        LoadingFragmentIdlingResource(CircleProgressView circleProgressView) {
//            this.circleProgressView = circleProgressView;
//        }
//
//        @Override
//        public String getName() {
//            return getClass().getSimpleName();
//        }
//
//        @Override
//        public boolean isIdleNow() {
//            boolean idling = circleProgressView.isShown();
//            if (!idling) {
//                resourceCallback.onTransitionToIdle();
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public void registerIdleTransitionCallback(ResourceCallback callback) {
//            this.resourceCallback = callback;
//        }
//    }
//
//    @Test
//    public void clickOnAndroidHomeIcon_OpensNavigation() {
//        // Check that left drawer is closed at startup
//        onView(withId(R.id.drawer_layout))
//                .check(matches(isClosed(Gravity.LEFT))); // Left Drawer should be closed.
//
//        // Open Drawer
//        onView(withContentDescription(getToolbarNavigationContentDescription(
//                mainActivityActivityTestRule.getActivity(), R.id.toolbarMain))).perform(click());
//
//        // Check if drawer is open
//        onView(withId(R.id.drawer_layout))
//                .check(matches(isOpen(Gravity.LEFT))); // Left drawer is open open.
//    }
//
//    /**
//     * Returns the content description for the navigation button view in the toolbar.
//     */
//    public static String getToolbarNavigationContentDescription(
//            @NonNull Activity activity, @IdRes int toolbar1) {
//        Toolbar toolbar = (Toolbar) activity.findViewById(toolbar1);
//        if (toolbar != null) {
//            return (String) toolbar.getNavigationContentDescription();
//        } else {
//            throw new RuntimeException("No toolbar found.");
//        }
//    }
}
