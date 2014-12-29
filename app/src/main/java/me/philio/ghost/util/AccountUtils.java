package me.philio.ghost.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;

import me.philio.ghost.R;

/**
 * Created by phil on 23/12/2014.
 */
public class AccountUtils {

    /**
     * Get an account name
     *
     * @param blogUrl
     * @param email
     * @return
     */
    public static String getName(String blogUrl, String email) {
        Uri uri = Uri.parse(blogUrl);
        return email + " (" + uri.getHost() + ")";
    }

    /**
     * Check if an account already exists
     *
     * @param context
     * @param blogUrl
     * @param email
     * @return
     */
    public static boolean accountExists(Context context, String blogUrl, String email) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string.account_type));
        String name = getName(blogUrl, email);
        for (Account account : accounts) {
            if (account.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
