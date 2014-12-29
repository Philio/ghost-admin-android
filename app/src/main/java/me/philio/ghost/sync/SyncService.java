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
package me.philio.ghost.sync;

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
