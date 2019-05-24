package com.nytimes.android.external.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nytimes.android.external.register.legal.LegalActivity
import com.nytimes.android.external.register.model.Repository
import com.nytimes.android.external.register.support.RepositoryFactory
import io.reactivex.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowPackageManager
import org.robolectric.shadows.ShadowResolveInfo
import java.text.NumberFormat

@RunWith(RobolectricTestRunner::class)
@org.robolectric.annotation.Config(
        constants = BuildConfig::class,
        sdk = [21])
class SettingsActivityTest {

    @Mock
    private lateinit var api: GithubApi

    private lateinit var testObject: SettingsActivity
    private lateinit var controller: ActivityController<*>
    private lateinit var shadowActivity: ShadowActivity
    private lateinit var shadowPackageManager: ShadowPackageManager

    private lateinit var githubCardRoot: ViewGroup
    private lateinit var githubCardName: TextView
    private lateinit var githubCardCommit: TextView
    private lateinit var githubCardDesc: TextView
    private lateinit var githubCardForks: TextView
    private lateinit var githubCardStars: TextView
    private lateinit var githubError: View

    private val numberFormat = NumberFormat.getIntegerInstance()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        shadowPackageManager = shadowOf(RuntimeEnvironment.application.packageManager)

        controller = Robolectric.buildActivity(SettingsActivity::class.java).create()
        testObject = controller.get() as SettingsActivity
        testObject.api = api
        testObject.provider = TestSchedulerProvider()

        githubCardRoot = testObject.findViewById<View>(R.id.settings_github) as ViewGroup
        githubCardName = testObject.findViewById<View>(R.id.github_repo_name) as TextView
        githubCardCommit = testObject.findViewById<View>(R.id.github_repo_last_commit) as TextView
        githubCardDesc = testObject.findViewById<View>(R.id.github_repo_desc) as TextView
        githubCardForks = testObject.findViewById<View>(R.id.github_repo_forks) as TextView
        githubCardStars = testObject.findViewById<View>(R.id.github_repo_stars) as TextView
        githubError = testObject.findViewById(R.id.github_error)

        shadowActivity = Shadow.extract<ShadowActivity>(testObject)
    }

    @Test
    fun onCreateSetsOther() {
        assertTextViewText(R.id.settings_header_other, R.string.settings_other)

        // Legal
        val legalRoot = testObject.findViewById<View>(R.id.settings_item_legal)
        val legalTitle = legalRoot.findViewById<View>(R.id.settings_item_title) as TextView
        assertThat(legalTitle.text).isEqualTo(getStringResource(R.string.settings_other_legal))
        val legalSub = legalRoot.findViewById<View>(R.id.settings_item_summary) as TextView
        assertThat(legalSub.visibility).isEqualTo(View.GONE)

        // TOS
        val tosRoot = testObject.findViewById<View>(R.id.settings_item_tos)
        val tosTitle = tosRoot.findViewById<View>(R.id.settings_item_title) as TextView
        assertThat(tosTitle.text).isEqualTo(getStringResource(R.string.settings_other_tos))
        val tosSub = tosRoot.findViewById<View>(R.id.settings_item_summary) as TextView
        assertThat(tosSub.visibility).isEqualTo(View.GONE)

        // Priv
        val privRoot = testObject.findViewById<View>(R.id.settings_item_priv)
        val privTitle = privRoot.findViewById<View>(R.id.settings_item_title) as TextView
        assertThat(privTitle.text).isEqualTo(getStringResource(R.string.settings_other_priv))
        val privSub = privRoot.findViewById<View>(R.id.settings_item_summary) as TextView
        assertThat(privSub.visibility).isEqualTo(View.GONE)
    }

    @Test
    fun onClickWhenLegalOpensLegalActivity() {
        testObject.findViewById<View>(R.id.settings_item_legal).callOnClick()
        val nextActivity = shadowActivity.nextStartedActivity
        assertThat(nextActivity.component.className).isEqualTo(
                LegalActivity::class.java.name)
    }

    @Test
    fun onClickWhenTosOpensTosWeb() {
        val uri = Uri.parse(getStringResource(R.string.url_tos))
        shadowPackageManager.isQueryIntentImplicitly = true
        shadowPackageManager.addResolveInfoForIntent(Intent(Intent.ACTION_VIEW, uri),
                ShadowResolveInfo.newResolveInfo("web", "app.web", "web"))

        testObject.findViewById<View>(R.id.settings_item_tos).callOnClick()
        val nextActivity = shadowActivity.nextStartedActivity
        assertThat(nextActivity.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(nextActivity.data).isEqualTo(uri)
    }

    @Test
    fun onClickWhenPrivOpensPrivWeb() {
        val uri = Uri.parse(getStringResource(R.string.url_priv))
        shadowPackageManager.isQueryIntentImplicitly = true
        shadowPackageManager.addResolveInfoForIntent(Intent(Intent.ACTION_VIEW, uri),
                ShadowResolveInfo.newResolveInfo("web", "app.web", "web"))

        testObject.findViewById<View>(R.id.settings_item_priv).callOnClick()
        val nextActivity = shadowActivity.nextStartedActivity
        assertThat(nextActivity.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(nextActivity.data).isEqualTo(uri)
    }

    @Test
    fun onCreateSetsLicense() {
        assertTextViewText(R.id.settings_header_license, R.string.settings_license)
        assertTextViewText(R.id.settings_item_license, R.string.settings_license_text)
    }

    @Test
    fun onOptionsItemSelectedWhenHomeFinishes() {
        controller.start()

        val item = mock(MenuItem::class.java)
        `when`(item.itemId).thenReturn(android.R.id.home)

        testObject.onOptionsItemSelected(item)
        assertThat(shadowActivity.isFinishing).isTrue()
    }

    @Test
    fun loadDataWhenErrorShowsErrorView() {
        `when`(api.playBillingRepository)
                .thenReturn(Observable.error(Throwable()))

        controller.start().postCreate(Bundle()).resume().visible()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertThat(githubError.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun loadDataWhenSuccessShowsDataViews() {
        val repository = RepositoryFactory.create()
        `when`(api.playBillingRepository)
                .thenReturn(Observable.just(repository))

        controller.start().postCreate(Bundle()).resume().visible()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertDataSet(repository)
    }

    @Test
    fun loadDataWhenFailThenSuccessShowsDataViews() {
        val repository = RepositoryFactory.create()

        `when`(api.playBillingRepository)
                .thenReturn(Observable.error(Throwable()))
                .thenReturn(Observable.just(repository))

        // Failure request
        controller.start().postCreate(Bundle()).resume().visible()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Assert error
        assertThat(githubError.visibility).isEqualTo(View.VISIBLE)

        // Success request
        githubCardRoot.callOnClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertDataSet(repository)
    }

    private fun assertDataSet(repository: Repository) {
        assertThat(githubError.visibility).isEqualTo(View.GONE)
        assertThat(githubCardName.visibility).isEqualTo(View.VISIBLE)
        assertThat(githubCardName.text).isEqualTo(repository.fullName)
        assertThat(githubCardCommit.visibility).isEqualTo(View.VISIBLE)
        assertThat(githubCardCommit.text).isEqualTo(DateUtils.getRelativeTimeSpanString(
                repository.pushedAt.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL))
        assertThat(githubCardDesc.visibility).isEqualTo(View.VISIBLE)
        assertThat(githubCardDesc.text).isEqualTo(repository.description)
        assertThat(githubCardForks.visibility).isEqualTo(View.VISIBLE)
        assertThat(githubCardForks.text).isEqualTo(numberFormat.format(repository.forksCount.toLong()))
        assertThat(githubCardStars.visibility).isEqualTo(View.VISIBLE)
        assertThat(githubCardStars.text).isEqualTo(numberFormat.format(repository.stargazersCount.toLong()))
    }

    private fun assertTextViewText(@IdRes viewId: Int, @StringRes stringResId: Int) {
        val title = testObject.findViewById<View>(viewId) as TextView
        assertThat(title.text.toString()).isEqualTo(getStringResource(stringResId))
    }

    private fun getStringResource(id: Int): String {
        return RuntimeEnvironment.application.getString(id)
    }

}
