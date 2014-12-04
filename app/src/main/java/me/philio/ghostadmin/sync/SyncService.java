package me.philio.ghostadmin.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service for the sync adapter
 *
 * Created by phil on 04/12/2014.
 */
public class SyncService extends Service {

    /**
     * Adapter singleton
     */
    private static SyncAdapter sSyncAdapter = null;

    /**
     * Lock object for thread safety
     */
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        // Create singleton instance
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the binder
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
