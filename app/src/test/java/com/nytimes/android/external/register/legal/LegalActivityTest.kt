package com.nytimes.android.external.register.legal

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.nytimes.android.external.register.R
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
class LegalActivityTest {

    private lateinit var testObject: LegalActivity
    private lateinit var controller: ActivityController<*>
    private lateinit var shadowActivity: ShadowActivity

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        controller = Robolectric.buildActivity(LegalActivity::class.java).create()
        testObject = controller.get() as com.nytimes.android.external.register.legal.LegalActivity
        shadowActivity = Shadow.extract<ShadowActivity>(testObject)
    }

    @Test
    fun upCallsOnBackPressed() {
        controller.start()

        val item = mock(MenuItem::class.java)
        `when`(item.itemId).thenReturn(android.R.id.home)

        testObject.onOptionsItemSelected(item)
        assertThat(shadowActivity.isFinishing).isTrue()
    }

    @Test
    fun hasCorrectData() {
        val names = getStringArrayResource(R.array.license_names)
        val values = getStringArrayResource(R.array.license_values)

        val legalRecyclerView = testObject.findViewById<View>(R.id.list) as RecyclerView
        val adapter = legalRecyclerView.adapter as LegalAdapter

        assertThat(names.size).isEqualTo(values.size)

        var i = 0
        val size = names.size
        while (i < size) {
            val item = adapter.getItem(i)

            val title = names[i]
            assertThat(item.first).isEqualTo(title)

            val license = values[i]
            assertThat(item.second).isEqualTo(license)
            i++
        }
    }

    private fun getStringArrayResource(id: Int): Array<String> {
        return RuntimeEnvironment.application.resources.getStringArray(id)
    }

}
