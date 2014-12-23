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
package me.philio.ghostadmin.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

/**
 * Helper methods for sync
 *
 * Created by phil on 15/12/2014.
 */
public class SyncHelper {

    /**
     * Request sync, checks that sync is not already running or pending
     *
     * @param account   The account to sync
     * @param authority The authority to sync
     */
    public static void requestSync(Account account, String authority) {
        if (ContentResolver.isSyncActive(account, authority) ||
                ContentResolver.isSyncPending(account, authority)) {
            return;
        }
        ContentResolver.requestSync(account, authority, Bundle.EMPTY);
    }

    public static boolean isSyncActivePending(Account account, String authority) {
        return ContentResolver.isSyncActive(account, authority) ||
                ContentResolver.isSyncPending(account, authority);
    }

}
