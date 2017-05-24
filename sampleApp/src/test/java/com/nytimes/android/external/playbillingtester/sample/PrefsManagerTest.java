package com.nytimes.android.external.playbillingtester.sample;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PrefsManagerTest {

    private PrefsManager testObject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testObject = new PrefsManager(RuntimeEnvironment.application
                .getSharedPreferences("test", Context.MODE_PRIVATE));
    }

    @Test
    public void testSetAndGet() throws Exception {
        assertThat(testObject.isUsingTestGoogleServiceProvider()).isFalse();
        testObject.setUsingGoogleServiceProvider(true);
        assertThat(testObject.isUsingTestGoogleServiceProvider()).isTrue();
    }
}
