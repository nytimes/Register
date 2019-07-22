package com.nytimes.android.external.register

import android.content.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController

@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.Config(constants = BuildConfig::class, sdk = [21])
class RegisterServiceTest {

    private lateinit var testObject: RegisterService
    private lateinit var controller: ServiceController<*>

    @Before
    fun setUp() {
        controller = Robolectric.buildService(RegisterService::class.java).create()
        testObject = controller.get() as RegisterService
    }

    @Test
    fun testServiceReturnsBinder() {
        controller.bind()
        val binder = testObject.onBind(mock(Intent::class.java))
        assertThat(binder).isEqualTo(testObject.mBinder)
    }

    internal class TestRegisterService : RegisterService() {
        override fun inject() {
            //intentionally blank
        }
    }
}
