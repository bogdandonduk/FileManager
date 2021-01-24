package pro.filemanager

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pro.filemanager.home.HomeActivity

@RunWith(AndroidJUnit4::class)
class HomeActivityInstrumentedTests {

    @Rule val homeActivityTestRule: ActivityScenario<HomeActivity> = ActivityScenario.launch(HomeActivity::class.java)

    @Test
    fun testHomeFragment() {
        val scenario = launchFragmentInContainer<HomeFragment>()

        // Assert
        val expectedInternalBtnTitle = "Internal Storage"

        // Act
        // ..

        // Assert
        onView(withId(R.id.fragmentHomeInternalBtnTitle)).check(matches(withText(expectedInternalBtnTitle)))
    }
}