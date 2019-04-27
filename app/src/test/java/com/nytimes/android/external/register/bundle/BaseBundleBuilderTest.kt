package com.nytimes.android.external.register.bundle

import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BaseBundleBuilderTest {

    @Mock
    private lateinit var apiOverrides: APIOverrides

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testReturnOkWhenDefault() {
        val testObject = object : BaseBundleBuilder(apiOverrides) {
            override fun rawResponseCode(): Int {
                return APIOverrides.RESULT_DEFAULT
            }
        }
        assertThat(testObject.responseCode())
                .isEqualTo(GoogleUtil.RESULT_OK)
    }

    @Test
    fun testReturnOverrideWhenNotDefault() {
        val testObject = object : BaseBundleBuilder(apiOverrides) {
            override fun rawResponseCode(): Int {
                return GoogleUtil.RESULT_BILLING_UNAVAILABLE
            }
        }
        assertThat(testObject.responseCode())
                .isEqualTo(GoogleUtil.RESULT_BILLING_UNAVAILABLE)
    }
}
