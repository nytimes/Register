package com.nytimes.android.external.register

import android.app.Activity
import android.content.Intent
import android.support.v4.content.PermissionChecker.PERMISSION_DENIED
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import com.nytimes.android.external.register.PermissionHandler.Companion.PERMISSION_REQ_CODE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionHandlerTest {

    @Mock
    private lateinit var activity: Activity

    private lateinit var intent: Intent

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        intent = Intent()
        `when`(activity.intent).thenReturn(intent)
    }

    @Test
    fun testHandlePermissionRequestStartsActivityIfGranted() {
        PermissionHandler.handlePermissionResult(PERMISSION_REQ_CODE, activity, PERMISSION_GRANTED)
        verify<Activity>(activity).finish()
        verify<Activity>(activity).startActivity(intent)
    }

    @Test
    fun testHandlePermissionRequestDoesNotStartActivityIfNotGranted() {
        PermissionHandler.handlePermissionResult(PERMISSION_REQ_CODE, activity, PERMISSION_DENIED)
        verify<Activity>(activity, never()).finish()
        verify<Activity>(activity, never()).startActivity(intent)
    }

    @Test
    fun testHandlePermissionRequestDoesNotStartActivityNoPermission() {
        PermissionHandler.handlePermissionResult(PERMISSION_REQ_CODE, activity)
        verify<Activity>(activity, never()).finish()
        verify<Activity>(activity, never()).startActivity(intent)
    }

    @Test
    fun testHandlePermissionRequestDoesNotStartActivityWrongRequestCode() {
        PermissionHandler.handlePermissionResult(1, activity, PERMISSION_GRANTED)
        verify<Activity>(activity, never()).finish()
        verify<Activity>(activity, never()).startActivity(intent)
    }
}
