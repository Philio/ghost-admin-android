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
package me.philio.ghost;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.activeandroid.app.Application;

/**
 * Application, nothing here at the moment, leaving for future requirements
 * <p/>
 * Created by phil on 27/11/2014.
 */
public class GhostApplication extends Application {

    /**
     * Logging tag
     */
    private static final String TAG = GhostApplication.class.getName();

    private Handler mHandler = new Handler();

    private GhostObserver mObserver;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO Temporary code for debugging any content issues, sync loops, etc
        mObserver = new GhostObserver(mHandler);
        Uri uri = Uri.parse("content://" + getString(R.string.content_authority) + "/");
        getContentResolver().registerContentObserver(uri, true, mObserver);
    }

    @Override
    public void onTerminate() {
        getContentResolver().unregisterContentObserver(mObserver);

        super.onTerminate();
    }

    class GhostObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public GhostObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "Content changed: " + uri);
        }

    }

}
