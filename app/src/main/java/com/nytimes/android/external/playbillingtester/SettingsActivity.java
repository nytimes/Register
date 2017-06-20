package com.nytimes.android.external.playbillingtester;


import android.animation.LayoutTransition;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nytimes.android.external.playbillingtester.di.Injector;
import com.nytimes.android.external.playbillingtester.legal.LegalActivity;
import com.nytimes.android.external.playbillingtester.model.Repository;

import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class SettingsActivity extends AppCompatActivity {

    @Inject
    GithubApi api;

    private ViewGroup githubCardRoot;
    private TextView githubCardName;
    private TextView githubCardCommit;
    private TextView githubCardDesc;
    private TextView githubCardForks;
    private TextView githubCardStars;
    private View githubImage;
    private View githubError;

    private Transition showDataTransition;
    private Transition showErrorTransition;
    private Transition showDefaultTransition;
    private NumberFormat numberFormat;

    private CompositeDisposable subs = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initToolbar();
        initRoot();
        initGithub();
        initOther();
        initDevelopedBy();
        initLicense();
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initRoot() {
        ViewGroup layout = findViewById(R.id.root);
        LayoutTransition layoutTransition = layout.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
    }

    private void initGithub() {
        TransitionInflater inflater = TransitionInflater.from(this);
        showDataTransition = inflater.inflateTransition(R.transition.transition_github_data);
        showErrorTransition = inflater.inflateTransition(R.transition.transition_github_error);
        showDefaultTransition = inflater.inflateTransition(R.transition.transition_github_default);
        showDefaultTransition.addListener(new Transition.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                loadGithubRepoData(true);
            }
        });

        numberFormat = NumberFormat.getIntegerInstance();

        githubCardRoot = findViewById(R.id.settings_github);
        githubCardName = findViewById(R.id.github_repo_name);
        githubCardCommit = findViewById(R.id.github_repo_last_commit);
        githubCardDesc = findViewById(R.id.github_repo_desc);
        githubCardForks = findViewById(R.id.github_repo_forks);
        githubCardStars = findViewById(R.id.github_repo_stars);
        githubError = findViewById(R.id.github_error);
        githubImage = findViewById(R.id.github_logo);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initGeneral() {
        setText(R.id.settings_header_general, R.string.settings_general);
        initItemNested(R.id.settings_item_import,
                R.string.settings_general_import_title,
                R.string.settings_general_import_summ,
                v -> {
                    View root = findViewById(android.R.id.content);
                    Snackbar.make(root, "TODO: Change Import Location", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void initOther() {
        setText(R.id.settings_header_other, R.string.settings_other);
        initItemNested(R.id.settings_item_legal, R.string.settings_other_legal, -1,
                v -> {
                    Intent intent = new Intent(this, LegalActivity.class);
                    startActivity(intent);
                });
        initItemNested(R.id.settings_item_tos, R.string.settings_other_tos, -1,
                v -> startWebIntent(getString(R.string.url_tos)));
        initItemNested(R.id.settings_item_priv, R.string.settings_other_priv, -1,
                v -> startWebIntent(getString(R.string.url_priv)));
    }

    private void initItemNested(@IdRes int layout, @StringRes int titleRes, @StringRes int summaryRes, View.OnClickListener clickListener) {
        View v = findViewById(layout);

        TextView title = v.findViewById(R.id.settings_item_title);
        title.setText(titleRes);

        TextView desc = v.findViewById(R.id.settings_item_summary);
        if (summaryRes > -1) {
            desc.setText(summaryRes);
            desc.setVisibility(View.VISIBLE);
        } else {
            desc.setVisibility(View.GONE);
        }

        v.setOnClickListener(clickListener);
    }

    private void initDevelopedBy() {
        View.OnClickListener onClick = view -> startWebIntent(getString(R.string.url_jobs));

        TextView title = setText(R.id.settings_header_dev, R.string.settings_dev_by);
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_times_dev, 0);
        title.setOnClickListener(onClick);

        String source = getString(R.string.settings_dev_by_text);
        TextView summ = findViewById(R.id.settings_item_dev);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            summ.setText(Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY));
        } else {
            summ.setText(Html.fromHtml(source));
        }
        summ.setOnClickListener(onClick);
    }

    private void initLicense() {
        setText(R.id.settings_header_license, R.string.settings_license);
        View licenseView = setText(R.id.settings_item_license, R.string.settings_license_text);
        licenseView.setBackground(null);
    }

    private TextView setText(@IdRes int viewId, @StringRes int stringResId) {
        TextView v = findViewById(viewId);
        v.setText(stringResId);
        return v;
    }

    private void startWebIntent(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            View root = findViewById(android.R.id.content);
            Snackbar.make(root, R.string.error_cannot_load_url, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadGithubRepoData(false);
    }

    private void loadGithubRepoData(boolean immediate) {
        subs.add(api.getPlayBillingRepository()
                .delay(immediate ? 0 : 2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showData, this::showError));
    }

    private void showData(Repository repository) {
        TransitionManager.beginDelayedTransition(githubCardRoot, showDataTransition);

        githubCardName.setText(repository.fullName());
        githubCardCommit.setText(DateUtils.getRelativeTimeSpanString (repository.pushedAt().getTime(),
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        githubCardDesc.setText(repository.description());
        githubCardForks.setText(numberFormat.format(repository.forksCount()));
        githubCardStars.setText(numberFormat.format(repository.stargazersCount()));
        githubCardRoot.setOnClickListener(view ->
                startWebIntent(repository.htmlUrl()));

        int logoSize = getResources()
                .getDimensionPixelSize(R.dimen.settings_github_logo_size);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                githubImage.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.github_repo_forks);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        layoutParams.width = logoSize;
        layoutParams.height = logoSize;

        githubCardName.setVisibility(View.VISIBLE);
        githubCardCommit.setVisibility(View.VISIBLE);
        githubCardDesc.setVisibility(View.VISIBLE);
        githubCardForks.setVisibility(View.VISIBLE);
        githubCardStars.setVisibility(View.VISIBLE);
    }

    private void showError(Throwable throwable) {
        Log.e(SettingsActivity.class.getSimpleName(), "Error while loading GitHub repo", throwable);
        TransitionManager.beginDelayedTransition(githubCardRoot, showErrorTransition);

        int logoAdjustY = getResources()
                .getDimensionPixelSize(R.dimen.settings_github_logo_offset);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                githubImage.getLayoutParams();
        layoutParams.topMargin = githubImage.getTop() - githubCardRoot.getPaddingTop() - logoAdjustY;
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);

        githubError.setVisibility(View.VISIBLE);

        githubCardRoot.setOnClickListener(view -> showDefault());
    }

    private void showDefault() {
        githubCardRoot.setClickable(false);
        TransitionManager.beginDelayedTransition(githubCardRoot, showDefaultTransition);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                githubImage.getLayoutParams();
        layoutParams.topMargin = 0;
        layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        githubError.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (subs != null) {
            subs.clear();
            subs = null;
        }
        super.onDestroy();
    }
}
