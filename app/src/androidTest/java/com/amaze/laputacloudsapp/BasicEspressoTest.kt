package com.amaze.laputacloudslib

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.amaze.laputacloudsapp.MainActivity
import com.amaze.laputacloudsapp.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class BasicEspressoTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun checkAppList() {

        onView(withContentDescription("Open navigation drawer")).perform(click())
        onView(withText(R.string.app_folder)).perform(click())
        onView(withId(R.id.swipeRefreshLayout)).check(matches(isDisplayed()))
    }
}
