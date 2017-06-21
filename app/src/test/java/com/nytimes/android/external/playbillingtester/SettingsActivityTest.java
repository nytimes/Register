package com.nytimes.android.external.playbillingtester;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nytimes.android.external.playbillingtester.legal.LegalActivity;
import com.nytimes.android.external.playbillingtester.model.Repository;
import com.nytimes.android.external.playbillingtester.support.RepositoryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.shadows.ShadowResolveInfo;

import java.text.NumberFormat;

import io.reactivex.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class, sdk = 21)
public class SettingsActivityTest {

    @Mock
    private GithubApi api;

    private SettingsActivity testObject;
    private ActivityController controller;
    private ShadowActivity shadowActivity;
    private ShadowPackageManager shadowPackageManager;

    private ViewGroup githubCardRoot;
    private TextView githubCardName;
    private TextView githubCardCommit;
    private TextView githubCardDesc;
    private TextView githubCardForks;
    private TextView githubCardStars;
    private View githubError;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        shadowPackageManager = shadowOf(RuntimeEnvironment.application.getPackageManager());

        controller = Robolectric.buildActivity(SettingsActivity.class).create();
        testObject = (SettingsActivity) controller.get();
        testObject.api = api;
        testObject.provider = new TestSchedulerProvider();

        githubCardRoot = testObject.findViewById(R.id.settings_github);
        githubCardName = testObject.findViewById(R.id.github_repo_name);
        githubCardCommit = testObject.findViewById(R.id.github_repo_last_commit);
        githubCardDesc = testObject.findViewById(R.id.github_repo_desc);
        githubCardForks = testObject.findViewById(R.id.github_repo_forks);
        githubCardStars = testObject.findViewById(R.id.github_repo_stars);
        githubError = testObject.findViewById(R.id.github_error);

        shadowActivity = Shadow.extract(testObject);
    }

    @Test
    public void onCreateSetsOther() {
        assertTextViewText(R.id.settings_header_other, R.string.settings_other);

        // Legal
        View legalRoot = testObject.findViewById(R.id.settings_item_legal);
        TextView legalTitle = (TextView) legalRoot.findViewById(R.id.settings_item_title);
        assertThat(legalTitle.getText()).isEqualTo(getStringResource(R.string.settings_other_legal));
        View legalSub = (TextView) legalRoot.findViewById(R.id.settings_item_summary);
        assertThat(legalSub.getVisibility()).isEqualTo(View.GONE);

        // TOS
        View tosRoot = testObject.findViewById(R.id.settings_item_tos);
        TextView tosTitle = (TextView) tosRoot.findViewById(R.id.settings_item_title);
        assertThat(tosTitle.getText()).isEqualTo(getStringResource(R.string.settings_other_tos));
        View tosSub = (TextView) tosRoot.findViewById(R.id.settings_item_summary);
        assertThat(tosSub.getVisibility()).isEqualTo(View.GONE);

        // Priv
        View privRoot = testObject.findViewById(R.id.settings_item_priv);
        TextView privTitle = (TextView) privRoot.findViewById(R.id.settings_item_title);
        assertThat(privTitle.getText()).isEqualTo(getStringResource(R.string.settings_other_priv));
        View privSub = (TextView) privRoot.findViewById(R.id.settings_item_summary);
        assertThat(privSub.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void onClickWhenLegalOpensLegalActivity() {
        testObject.findViewById(R.id.settings_item_legal).callOnClick();
        Intent nextActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextActivity.getComponent().getClassName()).isEqualTo(
                LegalActivity.class.getName());
    }

    @Test
    public void onClickWhenTosOpensTosWeb() {
        Uri uri = Uri.parse(getStringResource(R.string.url_tos));
        shadowPackageManager.setQueryIntentImplicitly(true);
        shadowPackageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, uri),
                ShadowResolveInfo.newResolveInfo("web", "app.web", "web"));

        testObject.findViewById(R.id.settings_item_tos).callOnClick();
        Intent nextActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(nextActivity.getData()).isEqualTo(uri);
    }

    @Test
    public void onClickWhenPrivOpensPrivWeb() {
        Uri uri = Uri.parse(getStringResource(R.string.url_priv));
        shadowPackageManager.setQueryIntentImplicitly(true);
        shadowPackageManager.addResolveInfoForIntent(new Intent(Intent.ACTION_VIEW, uri),
                ShadowResolveInfo.newResolveInfo("web", "app.web", "web"));

        testObject.findViewById(R.id.settings_item_priv).callOnClick();
        Intent nextActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextActivity.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(nextActivity.getData()).isEqualTo(uri);
    }

    @Test
    public void onCreateSetsLicense() {
        assertTextViewText(R.id.settings_header_license, R.string.settings_license);
        assertTextViewText(R.id.settings_item_license, R.string.settings_license_text);
    }

    @Test
    public void onOptionsItemSelectedWhenHomeFinishes() {
        controller.start();

        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(android.R.id.home);

        testObject.onOptionsItemSelected(item);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void loadDataWhenErrorShowsErrorView() {
        when(api.getPlayBillingRepository())
                .thenReturn(Observable.error(new Throwable()));

        controller.start().postCreate(new Bundle()).resume().visible();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertThat(githubError.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void loadDataWhenSuccessShowsDataViews() {
        Repository repository = RepositoryFactory.create();
        when(api.getPlayBillingRepository())
                .thenReturn(Observable.just(repository));

        controller.start().postCreate(new Bundle()).resume().visible();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertDataSet(repository);
    }

    @Test
    public void loadDataWhenFailThenSuccessShowsDataViews() {
        Repository repository = RepositoryFactory.create();

        when(api.getPlayBillingRepository())
                .thenReturn(Observable.error(new Throwable()))
                .thenReturn(Observable.just(repository));

        // Failure request
        controller.start().postCreate(new Bundle()).resume().visible();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // Assert error
        assertThat(githubError.getVisibility()).isEqualTo(View.VISIBLE);

        // Success request
        githubCardRoot.callOnClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertDataSet(repository);
    }

    private void assertDataSet(Repository repository) {
        assertThat(githubError.getVisibility()).isEqualTo(View.GONE);
        assertThat(githubCardName.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(githubCardName.getText()).isEqualTo(repository.fullName());
        assertThat(githubCardCommit.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(githubCardCommit.getText()).isEqualTo(DateUtils.getRelativeTimeSpanString(
                repository.pushedAt().getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL));
        assertThat(githubCardDesc.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(githubCardDesc.getText()).isEqualTo(repository.description());
        assertThat(githubCardForks.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(githubCardForks.getText()).isEqualTo(numberFormat.format(repository.forksCount()));
        assertThat(githubCardStars.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(githubCardStars.getText()).isEqualTo(numberFormat.format(repository.stargazersCount()));
    }

    private void assertTextViewText(@IdRes int viewId, @StringRes int stringResId) {
        TextView title = (TextView) testObject.findViewById(viewId);
        assertThat(title.getText().toString()).isEqualTo(getStringResource(stringResId));
    }

    private String getStringResource(int id) {
        return RuntimeEnvironment.application.getResources().getString(id);
    }

}
