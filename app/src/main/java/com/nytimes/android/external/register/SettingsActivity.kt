package com.nytimes.android.external.register

import android.animation.LayoutTransition
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.format.DateUtils
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.nytimes.android.external.register.di.Injector
import com.nytimes.android.external.register.di.SchedulerProvider
import com.nytimes.android.external.register.legal.LegalActivity
import com.nytimes.android.external.register.model.Repository
import io.reactivex.disposables.CompositeDisposable
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var api: GithubApi
    @Inject
    lateinit var provider: SchedulerProvider

    private lateinit var githubCardRoot: ViewGroup
    private lateinit var githubCardName: TextView
    private lateinit var githubCardCommit: TextView
    private lateinit var githubCardDesc: TextView
    private lateinit var githubCardForks: TextView
    private lateinit var githubCardStars: TextView
    private lateinit var githubImage: View
    private lateinit var githubError: View

    private lateinit var showDataTransition: Transition
    private lateinit var showErrorTransition: Transition
    private lateinit var showDefaultTransition: Transition
    private lateinit var numberFormat: NumberFormat

    private var subs = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initToolbar()
        initRoot()
        initGithub()
        initOther()
        initLicense()
    }

    private fun inject() {
        Injector.create(this).inject(this)
    }

    private fun initRoot() {
        val layout = findViewById<View>(R.id.root) as ViewGroup
        val layoutTransition = layout.layoutTransition
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun initGithub() {

        with(TransitionInflater.from(this)) {
            showDataTransition = inflateTransition(R.transition.transition_github_data)
            showErrorTransition = inflateTransition(R.transition.transition_github_error)
            showDefaultTransition = inflateTransition(R.transition.transition_github_default)
            showDefaultTransition.addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    loadGithubRepoData(true)
                }
            })
        }

        numberFormat = NumberFormat.getIntegerInstance()

        githubCardRoot = findViewById<View>(R.id.settings_github) as ViewGroup
        githubCardName = findViewById<View>(R.id.github_repo_name) as TextView
        githubCardCommit = findViewById<View>(R.id.github_repo_last_commit) as TextView
        githubCardDesc = findViewById<View>(R.id.github_repo_desc) as TextView
        githubCardForks = findViewById<View>(R.id.github_repo_forks) as TextView
        githubCardStars = findViewById<View>(R.id.github_repo_stars) as TextView
        githubError = findViewById(R.id.github_error)
        githubImage = findViewById(R.id.github_logo)
    }

    private fun initToolbar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initGeneral() {
        setText(R.id.settings_header_general, R.string.settings_general)
        initItemNested(R.id.settings_item_import,
                R.string.settings_general_import_title,
                R.string.settings_general_import_summ) {
            val root = findViewById<View>(android.R.id.content)
            Snackbar.make(root, "TODO: Change Import Location", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun initOther() {
        setText(R.id.settings_header_other, R.string.settings_other)
        initItemNested(R.id.settings_item_legal, R.string.settings_other_legal, -1) {
            val intent = Intent(this, LegalActivity::class.java)
            startActivity(intent)
        }
        initItemNested(R.id.settings_item_tos, R.string.settings_other_tos, -1) {
            startWebIntent(getString(R.string.url_tos))
        }
        initItemNested(R.id.settings_item_priv, R.string.settings_other_priv, -1) {
            startWebIntent(getString(R.string.url_priv))
        }
    }

    private fun initItemNested(@IdRes layout: Int, @StringRes titleRes: Int, @StringRes summaryRes: Int,
                               clickListener: (View) -> Unit) {
        val v = findViewById<View>(layout)

        val title = v.findViewById<View>(R.id.settings_item_title) as TextView
        title.setText(titleRes)

        val desc = v.findViewById<View>(R.id.settings_item_summary) as TextView

        if (summaryRes == -1) {
            desc.visibility = View.GONE
        } else {
            desc.setText(summaryRes)
            desc.visibility = View.VISIBLE
        }

        v.setOnClickListener(clickListener)
    }

    private fun initDevelopedBy() {
        val onClick = { _: View -> startWebIntent(getString(R.string.url_jobs)) }

        val title = setText(R.id.settings_header_dev, R.string.settings_dev_by)
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_times_dev, 0)
        title.setOnClickListener(onClick)

        val source = getString(R.string.settings_dev_by_text)
        val summ = findViewById<View>(R.id.settings_item_dev) as TextView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            summ.text = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            summ.text = Html.fromHtml(source)
        }
        summ.setOnClickListener(onClick)
    }

    private fun initLicense() {
        setText(R.id.settings_header_license, R.string.settings_license)
        val licenseView = setText(R.id.settings_item_license, R.string.settings_license_text)
        licenseView.background = null
    }

    private fun setText(@IdRes viewId: Int, @StringRes stringResId: Int): TextView {
        return (findViewById<View>(viewId) as TextView).apply {
            setText(stringResId)
        }
    }

    private fun startWebIntent(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(packageManager) == null) {
            val root = findViewById<View>(android.R.id.content)
            Snackbar.make(root, R.string.error_cannot_load_url, Snackbar.LENGTH_SHORT).show()
        } else {
            startActivity(intent)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        loadGithubRepoData(savedInstanceState != null)
    }

    internal fun loadGithubRepoData(immediate: Boolean) {
        subs.add(api.playBillingRepository
                .delay((if (immediate) 0 else 2000).toLong(), TimeUnit.MILLISECONDS)
                .observeOn(provider.getMainThread())
                .subscribe({ showData(it) }, { showError(it) }))
    }

    private fun showData(repository: Repository) {
        TransitionManager.beginDelayedTransition(githubCardRoot, showDataTransition)

        val logoSize = resources.getDimensionPixelSize(R.dimen.settings_github_logo_size)
        val layoutParams = githubImage.layoutParams as RelativeLayout.LayoutParams
        layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT)
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.github_repo_forks)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
        layoutParams.width = logoSize
        layoutParams.height = logoSize

        githubCardName.visibility = View.VISIBLE
        githubCardCommit.visibility = View.VISIBLE
        githubCardDesc.visibility = View.VISIBLE
        githubCardForks.visibility = View.VISIBLE
        githubCardStars.visibility = View.VISIBLE

        githubCardName.text = repository.fullName
        githubCardCommit.text = DateUtils.getRelativeTimeSpanString(repository.pushedAt.time,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
        githubCardDesc.text = repository.description
        githubCardForks.text = numberFormat.format(repository.forksCount.toLong())
        githubCardStars.text = numberFormat.format(repository.stargazersCount.toLong())
        githubCardRoot.setOnClickListener { startWebIntent(repository.htmlUrl) }
    }

    private fun showError(throwable: Throwable) {
        Log.e(SettingsActivity::class.java.simpleName, "Error while loading GitHub repo", throwable)
        TransitionManager.beginDelayedTransition(githubCardRoot, showErrorTransition)

        val logoAdjustY = resources.getDimensionPixelSize(R.dimen.settings_github_logo_offset)
        val layoutParams = githubImage.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = githubImage.top - githubCardRoot.paddingTop - logoAdjustY
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT)

        githubError.visibility = View.VISIBLE

        githubCardRoot.setOnClickListener { view -> showDefault() }
    }

    private fun showDefault() {
        githubCardRoot.isClickable = false
        TransitionManager.beginDelayedTransition(githubCardRoot, showDefaultTransition)

        val layoutParams = githubImage.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = 0
        layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)

        githubError.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        subs.clear()
        super.onDestroy()
    }

}
