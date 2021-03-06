/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.android.settings.core.BasePreferenceController.UNSUPPORTED_ON_DEVICE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.FeatureFlagUtils;

import com.android.settings.core.FeatureFlags;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class MainlineModuleVersionPreferenceControllerTest {

    @Mock
    private PackageManager mPackageManager;

    private Context mContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(RuntimeEnvironment.application);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);

        FeatureFlagUtils.setEnabled(mContext, FeatureFlags.MAINLINE_MODULE, true);
    }

    @Test
    public void getAvailabilityStatus_featureDisabled_unavailable() {
        FeatureFlagUtils.setEnabled(mContext, FeatureFlags.MAINLINE_MODULE, false);

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void getAvailabilityStatus_noMainlineModuleProvider_unavailable() {
        when(mContext.getString(
                com.android.internal.R.string.config_defaultModuleMetadataProvider)).thenReturn(
                null);

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void getAvailabilityStatus_noMainlineModulePackageInfo_unavailable() throws Exception {

        final String provider = "test.provider";
        when(mContext.getString(
                com.android.internal.R.string.config_defaultModuleMetadataProvider))
                .thenReturn(provider);
        when(mPackageManager.getPackageInfo(eq(provider), anyInt()))
                .thenThrow(new PackageManager.NameNotFoundException());

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void getAvailabilityStatus_hasMainlineModulePackageInfo_available() throws Exception {
        final String provider = "test.provider";
        final String version = "test version 123";
        final PackageInfo info = new PackageInfo();
        info.versionName = version;
        when(mContext.getString(
                com.android.internal.R.string.config_defaultModuleMetadataProvider))
                .thenReturn(provider);
        when(mPackageManager.getPackageInfo(eq(provider), anyInt())).thenReturn(info);

        final MainlineModuleVersionPreferenceController controller =
                new MainlineModuleVersionPreferenceController(mContext, "key");

        assertThat(controller.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

}
