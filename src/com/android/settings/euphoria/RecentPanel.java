/*
 * Copyright 2014-2015 The Euphoria-OS Project
 * Copyright (C) 2015 SlimRoms Project
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

package com.android.settings.euphoria;

import android.app.ActivityManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Gravity;

import android.provider.SearchIndexableResource;
import com.android.settings.euphoria.SeekBarPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;

public class RecentPanel extends SettingsPreferenceFragment
            implements OnPreferenceChangeListener, Indexable  {

    private static final String SHOW_RECENTS_SEARCHBAR = "recents_show_search_bar";
    private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
    private static final String RECENTS_DISMISS_ALL = "recents_clear_all_dismiss_all";
    private static final String USE_SLIM_RECENTS = "use_slim_recents";
    private static final String RECENTS_MAX_APPS = "recents_max_apps";
    private static final String RECENT_PANEL_SCALE = "recent_panel_scale_factor";
    private static final String RECENT_PANEL_EXPANDED_MODE = "recent_panel_expanded_mode";
    private static final String RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode";

    private SwitchPreference mRecentsSearchBar;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsClearAllLocation;
    private SwitchPreference mRecentsDismissAll;
    private SwitchPreference mUseSlimRecents;
    private SeekBarPreference mMaxApps;
    private SeekBarPreference mRecentPanelScale;
    private ListPreference mRecentPanelExpandedMode;
    private SwitchPreference mRecentPanelLeftyMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.recent_panel_settings);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mRecentsSearchBar = (SwitchPreference) prefSet.findPreference(SHOW_RECENTS_SEARCHBAR);
        mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);
        mRecentsDismissAll = (SwitchPreference) prefSet.findPreference(RECENTS_DISMISS_ALL);

        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

        mUseSlimRecents = (SwitchPreference) prefSet.findPreference(USE_SLIM_RECENTS);
        mUseSlimRecents.setChecked(Settings.System.getInt(resolver,
                Settings.System.USE_SLIM_RECENTS, 0) == 1);
        mUseSlimRecents.setOnPreferenceChangeListener(this);

        mMaxApps = (SeekBarPreference) findPreference(RECENTS_MAX_APPS);
        int maxApps = Settings.System.getInt(resolver,
                Settings.System.RECENTS_MAX_APPS, ActivityManager.getMaxRecentTasksStatic());
        mMaxApps.setValue(maxApps);
        mMaxApps.setOnPreferenceChangeListener(this);

        mRecentPanelScale = (SeekBarPreference) findPreference(RECENT_PANEL_SCALE);
        int recentPanelScale = Settings.System.getInt(resolver,
                Settings.System.RECENT_PANEL_SCALE_FACTOR, 100);
        mRecentPanelScale.setValue(recentPanelScale);
        mRecentPanelScale.setOnPreferenceChangeListener(this);

        mRecentPanelExpandedMode = (ListPreference) prefSet.findPreference(RECENT_PANEL_EXPANDED_MODE);
        int recentPanelExpandedMode = Settings.System.getIntForUser(resolver,
                Settings.System.RECENT_PANEL_EXPANDED_MODE, 0, UserHandle.USER_CURRENT);
        mRecentPanelExpandedMode.setValue(String.valueOf(recentPanelExpandedMode));
        mRecentPanelExpandedMode.setSummary(mRecentPanelExpandedMode.getEntry());
        mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);

        mRecentPanelLeftyMode = (SwitchPreference) prefSet.findPreference(RECENT_PANEL_LEFTY_MODE);
        mRecentPanelLeftyMode.setChecked(Settings.System.getInt(resolver,
                Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);

        updatePreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreference();
    }

    private void updatePreference() {
        boolean slimRecent = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.USE_SLIM_RECENTS, 1) == 1;

        if (slimRecent) {
            mRecentsSearchBar.setEnabled(false);
            mRecentsClearAll.setEnabled(false);
            mRecentsClearAllLocation.setEnabled(false);
            mRecentsDismissAll.setEnabled(false);
        } else {
            mRecentsSearchBar.setEnabled(true);
            mRecentsClearAll.setEnabled(true);
            mRecentsClearAllLocation.setEnabled(true);
            mRecentsDismissAll.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) newValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        } else if (preference == mUseSlimRecents) {
            Settings.System.putInt(getContentResolver(), Settings.System.USE_SLIM_RECENTS,
                    ((Boolean) newValue) ? 1 : 0);
            updatePreference();
            return true;
        } else if (preference == mMaxApps) {
            int maxApps = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_MAX_APPS, maxApps);
            return true;
        } else if (preference == mRecentPanelScale) {
            int recentPanelScale = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, recentPanelScale);
            return true;
        } else if (preference == mRecentPanelExpandedMode) {
            int recentPanelExpandedMode = Integer.valueOf((String) newValue);
            int index = mRecentPanelExpandedMode.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENT_PANEL_EXPANDED_MODE,
                recentPanelExpandedMode, UserHandle.USER_CURRENT);
            mRecentPanelExpandedMode.setSummary(mRecentPanelExpandedMode.getEntries()[index]);
            return true;
        } else if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        }
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.recent_panel_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    return new ArrayList<String>();
                }
            };
}
