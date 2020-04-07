package ch.epfl.sdp

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginNavFragmentTest {

    companion object {
        private const val FAKE_NAME = "Fake Girl"
        private const val FAKE_EMAIL = "fake@fake.com"
        private const val FAKE_PROFILE_IMAGE_URL = "https://fakeimg.pl/80x80/"
    }

    private lateinit var mUiDevice: UiDevice

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = grant(ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION)

    @get:Rule
    val mActivityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun before() {
        mUiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        Auth.logout()
    }

    private fun getGSO(): GoogleSignInOptions {
        return GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(MainApplication.applicationContext().getString(R.string.google_signin_key))
                .requestEmail()
                .build()
    }

    @Test
    fun clickSignInShouldLaunchIntent() {
        Auth.logout()
        onView(withId(R.id.drawer_layout))
                .check(matches(DrawerMatchers.isClosed(Gravity.LEFT))) // Check that drawer is closed to begin with
                .perform(DrawerActions.open())

        onView(withId(R.id.nav_signin_button)).check(matches(isClickable())).perform(click())

        val mGoogleSignInClient = GoogleSignIn.getClient(MainApplication.applicationContext(), getGSO())
        Intents.intended(IntentMatchers.filterEquals(mGoogleSignInClient.signInIntent))
        mUiDevice.pressBack()
    }

    @Test
    fun whenAuthValuesAreUpdatedInterfaceShouldBeUpdated() {
        Auth.logout()
        onView(withId(R.id.drawer_layout))
                .check(matches(DrawerMatchers.isClosed(Gravity.LEFT))) // Check that drawer is closed to begin with
                .perform(DrawerActions.open())

        Auth.email.postValue(FAKE_EMAIL)
        Auth.name.postValue(FAKE_NAME)
        Auth.profileImageURL.postValue(FAKE_PROFILE_IMAGE_URL)
        Auth.loggedIn.postValue(true)

        onView(withId(R.id.nav_username)).check(matches(withText(FAKE_NAME)))
        onView(withId(R.id.nav_user_email)).check(matches(withText(FAKE_EMAIL)))
        onView(withId(R.id.nav_user_image)).check(matches(isDisplayed()))

        onView(withId(R.id.nav_signout_button)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.nav_signin_button)).check(matches(isDisplayed()))

        mUiDevice.pressBack()
    }

}