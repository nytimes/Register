package com.nytimes.android.external.register

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {

    private val scenario: FragmentScenario<SettingsFragment> by lazy {
        launchFragmentInContainer<SettingsFragment>(
                themeResId = R.style.nyt_register_Theme_Register_Translucent)
    }

    @Test
    fun onCreateSetsOther() {

        scenario.onFragment {
            it.assertTextViewText(R.id.settings_header_other, R.string.nyt_register_settings_other)

            // Legal
            val legalRoot = it.requireView().findViewById<View>(R.id.settings_item_legal)
            val legalTitle = legalRoot.findViewById<View>(R.id.settings_item_title) as TextView
            Assertions.assertThat(legalTitle.text).isEqualTo(it.getStringResource(R.string.nyt_register_settings_other_legal))
            val legalSub = legalRoot.findViewById<View>(R.id.settings_item_summary) as TextView
            Assertions.assertThat(legalSub.visibility).isEqualTo(View.GONE)

            // TOS
            val tosRoot = it.requireView().findViewById<View>(R.id.settings_item_tos)
            val tosTitle = tosRoot.findViewById<View>(R.id.settings_item_title) as TextView
            Assertions.assertThat(tosTitle.text).isEqualTo(it.getStringResource(R.string.nyt_register_settings_other_tos))
            val tosSub = tosRoot.findViewById<View>(R.id.settings_item_summary) as TextView
            Assertions.assertThat(tosSub.visibility).isEqualTo(View.GONE)

            // Priv
            val privRoot = it.requireView().findViewById<View>(R.id.settings_item_priv)
            val privTitle = privRoot.findViewById<View>(R.id.settings_item_title) as TextView
            Assertions.assertThat(privTitle.text).isEqualTo(it.getStringResource(R.string.nyt_register_settings_other_priv))
            val privSub = privRoot.findViewById<View>(R.id.settings_item_summary) as TextView
            Assertions.assertThat(privSub.visibility).isEqualTo(View.GONE)
        }
    }

    @Test
    fun onCreateSetsLicense() {
        scenario.onFragment {
            it.assertTextViewText(R.id.settings_header_license, R.string.nyt_register_settings_license)
            it.assertTextViewText(R.id.settings_item_license, R.string.nyt_register_settings_license_text)
        }
    }

    private fun Fragment.assertTextViewText(@IdRes viewId: Int, @StringRes stringResId: Int) {
        val title = requireView().findViewById<View>(viewId) as TextView
        Assertions.assertThat(title.text.toString()).isEqualTo(getStringResource(stringResId))
    }

    private fun Fragment.getStringResource(@IdRes res: Int): String {
        return getString(res)
    }

}