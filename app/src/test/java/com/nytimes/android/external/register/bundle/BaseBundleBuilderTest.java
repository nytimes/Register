package com.nytimes.android.external.register.bundle;

import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.registerlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BaseBundleBuilderTest {

    private BaseBundleBuilder testObject;

    @Mock
    private APIOverrides apiOverrides;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReturnOkWhenDefault() {
        testObject = new BaseBundleBuilder(apiOverrides) {
            @Override
            protected int rawResponseCode() {
                return APIOverrides.RESULT_DEFAULT;
            }
        };
        assertThat(testObject.responseCode())
                .isEqualTo(GoogleUtil.RESULT_OK);
    }

    @Test
    public void testReturnOverrideWhenNotDefault() {
        testObject = new BaseBundleBuilder(apiOverrides) {
            @Override
            protected int rawResponseCode() {
                return GoogleUtil.RESULT_BILLING_UNAVAILABLE;
            }
        };
        assertThat(testObject.responseCode())
                .isEqualTo(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
    }
}
