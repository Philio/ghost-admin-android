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

}
