package com.nytimes.android.external.register

import android.app.Activity
import android.content.Intent
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.PermissionHandler.Companion.PERMISSION_REQ_CODE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class PermissionHandlerTest {

    private val activity: Activity = mock()

    private lateinit var intent: Intent

    @Before
    fun setUp() {
        intent = Intent()
        whenever(activity.intent).thenReturn(intent)
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
