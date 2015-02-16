/*
 * Copyright 2014 Phil Bayfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.philio.ghost.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.philio.ghost.R;
import me.philio.ghost.sync.SyncHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    /**
     * Arguments
     */
    public static final String ARG_ACCOUNT = "account";

    /**
     * Instance of the account manager
     */
    private AccountManager mAccountManager;

    /**
     * Account to show preferences for
     */
    private Account mAccount;

    /**
     * Views
     */
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param account Account to show preferences for
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(Account account) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ACCOUNT, account);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        mAccountManager = AccountManager.get(getActivity());

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_ACCOUNT)) {
                mAccount = args.getParcelable(ARG_ACCOUNT);
            }
        }

        if (mAccount != null) {
            addPreferencesFromResource(R.xml.account_preferences);
            setupAccountPreferences();
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure toolbar as actionbar
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(mToolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set title
        // For some reason this gets changed back if called earlier
        if (mAccount != null) {
            mToolbar.setTitle(mAccount.name);
        } else {
            mToolbar.setTitle(R.string.title_activity_settings);
        }

        // Populate the account list
        PreferenceCategory catAccounts = (PreferenceCategory) findPreference("cat_accounts");
        if (catAccounts != null) {
            catAccounts.removeAll();

            Account[] accounts = mAccountManager.getAccountsByType(getString(R.string.account_type));
            for (final Account account : accounts) {
                Preference preference = new Preference(getActivity());
                preference.setTitle(account.name);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        getFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .replace(R.id.container, SettingsFragment.newInstance(account))
                                .addToBackStack(null)
                                .commit();
                        return true;
                    }
                });
                catAccounts.addPreference(preference);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            ((ListPreference) preference).setValue((String) newValue);
        }
        mAccountManager.setUserData(mAccount, preference.getKey(), (String) newValue);
        return false;
    }

    /**
     * Set up the account preferences, load the saved value for the selected account and add the
     * save listener to ensure changes are saved to account rather than shared prefs
     */
    private void setupAccountPreferences() {
        // Sync
        final CheckBoxPreference sync = (CheckBoxPreference) findPreference("pref_account_sync");
        sync.setChecked(SyncHelper.isSyncEnabled(mAccount, getString(R.string.content_authority)));
        sync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sync.setChecked((boolean) newValue);
                if ((boolean) newValue) {
                    SyncHelper.enableSync(mAccount, getString(R.string.content_authority));
                } else {
                    SyncHelper.disableSync(mAccount, getString(R.string.content_authority));
                }
                return false;
            }
        });

        // Sync drafts setting
        ListPreference syncDrafts = (ListPreference) findPreference("pref_account_sync_drafts");
        String syncDraftsValue = mAccountManager.getUserData(mAccount, "pref_account_sync_drafts");
        if (syncDraftsValue != null) {
            syncDrafts.setValue(syncDraftsValue);
        }
        syncDrafts.setOnPreferenceChangeListener(this);

        // Sync published articles setting
        ListPreference syncPublished = (ListPreference) findPreference("pref_account_sync_published");
        String syncPublishedValue = mAccountManager.getUserData(mAccount, "pref_account_sync_published");
        if (syncPublishedValue != null) {
            syncPublished.setValue(syncPublishedValue);
        }
        syncPublished.setOnPreferenceChangeListener(this);
    }

}
