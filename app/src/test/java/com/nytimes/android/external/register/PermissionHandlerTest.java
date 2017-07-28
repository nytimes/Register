package com.nytimes.android.external.register;

import android.app.Activity;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.nytimes.android.external.register.PermissionHandler.PERMISSION_REQ_CODE;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PermissionHandlerTest {

    @Mock
    private Activity activity;

    private Intent intent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        intent = new Intent();
        when(activity.getIntent()).thenReturn(intent);
    }

    @Test
    public void testHandlePermissionRequestStartsActivityIfGranted() {
        PermissionHandler.handlePermissionResult(PERMISSION_REQ_CODE, activity, PERMISSION_GRANTED);
        verify(activity).finish();
        verify(activity).startActivity(intent);
    }

    @Test
    public void testHandlePermissionRequestDoesNotStartActivityIfNotGranted() {
        PermissionHandler.handlePermissionResult(PERMISSION_REQ_CODE, activity, PERMISSION_DENIED);
        verify(activity, never()).finish();
        verify(activity, never()).startActivity(intent);
    }

    @Test
    public void testHandlePermissionRequestDoesNotStartActivityNoPermission() {
        PermissionHandler.handlePermissionResult(PERMISSION_REQ_CODE, activity);
        verify(activity, never()).finish();
        verify(activity, never()).startActivity(intent);
    }

    @Test
    public void testHandlePermissionRequestDoesNotStartActivityWrongRequestCode() {
        PermissionHandler.handlePermissionResult(1, activity, PERMISSION_GRANTED);
        verify(activity, never()).finish();
        verify(activity, never()).startActivity(intent);
    }
}
