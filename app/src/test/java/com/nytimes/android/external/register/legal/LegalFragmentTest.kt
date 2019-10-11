//package com.nytimes.android.external.register.legal
//
//import android.view.View
//import androidx.fragment.app.testing.FragmentScenario
//import androidx.fragment.app.testing.launchFragmentInContainer
//import androidx.recyclerview.widget.RecyclerView
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.nytimes.android.external.register.R
//import org.assertj.core.api.Assertions
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class LegalFragmentTest {
//
//    private val scenario: FragmentScenario<LegalFragment> by lazy {
//        launchFragmentInContainer<LegalFragment>()
//    }
//
////    @Test
////    fun upCallsOnBackPressed() {
////        controller.start()
////
////        val item = Mockito.mock(MenuItem::class.java)
////        Mockito.`when`(item.itemId).thenReturn(android.R.id.home)
////
////        testObject.onOptionsItemSelected(item)
////        Assertions.assertThat(shadowActivity.isFinishing).isTrue()
////    }
//
//    @Test
//    fun hasCorrectData() {
//        scenario.onFragment {
//            val names = it.activity!!.resources.getStringArray(R.array.license_names)
//            val values = it.activity!!.resources.getStringArray(R.array.license_values)
//
//            val legalRecyclerView = it.view!!.findViewById<View>(R.id.list) as RecyclerView
//
//            val adapter = legalRecyclerView.adapter as LegalAdapter
//
//            Assertions.assertThat(names.size).isEqualTo(values.size)
//
//            var i = 0
//            val size = names.size
//            while (i < size) {
//                val item = adapter.getItem(i)
//
//                val title = names[i]
//                Assertions.assertThat(item.first).isEqualTo(title)
//
//                val license = values[i]
//                Assertions.assertThat(item.second).isEqualTo(license)
//                i++
//            }
//        }
//
//    }
//}