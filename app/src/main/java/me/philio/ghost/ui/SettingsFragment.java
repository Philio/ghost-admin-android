package me.philio.ghost.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v4.app.Fragment;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.github.machinarius.preferencefragment.PreferenceManagerCompat;

import me.philio.ghost.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

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
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

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

}
