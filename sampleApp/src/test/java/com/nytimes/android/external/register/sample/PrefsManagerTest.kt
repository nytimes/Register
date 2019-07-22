package com.nytimes.android.external.register.sample

import android.content.Context
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PrefsManagerTest {

    private var testObject: PrefsManager? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testObject = PrefsManager(RuntimeEnvironment.application
                .getSharedPreferences("test", Context.MODE_PRIVATE))
    }

    @Test
    @Throws(Exception::class)
    fun testSetAndGet() {
        assertThat(testObject!!.isUsingTestGoogleServiceProvider).isFalse()
        testObject!!.setUsingGoogleServiceProvider(true)
        assertThat(testObject!!.isUsingTestGoogleServiceProvider).isTrue()
    }
}
