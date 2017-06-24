package com.nytimes.android.external.playbillingtester.legal;


import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.nytimes.android.external.playbillingtester.BuildConfig;
import com.nytimes.android.external.playbillingtester.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class, sdk = 21)
public class LegalActivityTest {

    private LegalActivity testObject;
    private ActivityController controller;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(LegalActivity.class).create();
        testObject = (LegalActivity) controller.get();
        shadowActivity = Shadow.extract(testObject);
    }

    @Test
    public void upCallsOnBackPressed() {
        controller.start();

        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(android.R.id.home);

        testObject.onOptionsItemSelected(item);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void hasCorrectData() {
        String[] names = getStringArrayResource(R.array.license_names);
        String[] values = getStringArrayResource(R.array.license_values);

        RecyclerView legalRecyclerView = (RecyclerView) testObject.findViewById(R.id.list);
        LegalAdapter adapter = (LegalAdapter) legalRecyclerView.getAdapter();

        assertThat(names.length).isEqualTo(values.length);

        for (int i = 0, size = names.length; i < size; i++) {
            Map.Entry<String, String> item = adapter.getItem(i);

            String title = names[i];
            assertThat(item.getKey()).isEqualTo(title);

            String license = values[i];
            assertThat(item.getValue()).isEqualTo(license);
        }
    }

    private String [] getStringArrayResource(int id) {
        return RuntimeEnvironment.application.getResources().getStringArray(id);
    }

}
