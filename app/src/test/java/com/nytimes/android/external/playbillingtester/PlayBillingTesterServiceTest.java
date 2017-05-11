package com.nytimes.android.external.playbillingtester;

import android.content.Intent;
import android.os.IBinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class)
public class PlayBillingTesterServiceTest {

    private PlayBillingTesterService testObject;
    private ServiceController controller;

    @Before
    public void setUp() {
        controller = Robolectric.buildService(PlayBillingTesterService.class).create();
        testObject = (PlayBillingTesterService) controller.get();
    }

    @Test
    public void testServiceReturnsBinder() {
        controller.bind();
        IBinder binder = testObject.onBind(mock(Intent.class));
        assertThat(binder)
            .isEqualTo(testObject.mBinder);
    }

    static class TestPlayBillingTesterService extends PlayBillingTesterService {
        @Override
        protected void inject() {
            //intentionally blank
        }
    }
}
