package com.amaze.laputacloudsapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class PhoneFileTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun navigatePhoneFiles() {
        onView(withContentDescription("Open navigation drawer"))
            .perform(click())
        onView(withText(R.string.app_folder)).perform(click())
        onView(withId(R.id.swipeRefreshLayout))
            .check(matches(isDisplayed()))
        onView(withText("Music"))
            .perform(click())
        onView(withId(R.id.swipeRefreshLayout))
            .check(matches(isDisplayed()))
        onView(withText(".."))
            .perform(click())
        onView(withId(R.id.swipeRefreshLayout))
            .check(matches(isDisplayed()))
        onView(withText("Podcasts"))
            .perform(click())
        onView(withId(R.id.swipeRefreshLayout))
            .check(matches(isDisplayed()))
        onView(withContentDescription("Navigate up"))
            .perform(click())
        onView(withContentDescription("Open navigation drawer"))
            .perform(click())
        onView(withId(R.id.text_home))
            .check(matches(isDisplayed()))
    }
}