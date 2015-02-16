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
