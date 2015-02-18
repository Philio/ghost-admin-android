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

/**
 * Preference constants
 */
public class PreferenceConstants {

    /**
     * Editing
     */
    public static final String KEY_EDITING_MODE = "pref_editing_mode";
    public static final int EDITING_MODE_RICH = 0;
    public static final int EDITING_MODE_PLAIN = 1;
    public static final int EDITING_MODE_DEFAULT = EDITING_MODE_RICH;

    /**
     * Global sync
     */
    public static final String KEY_SYNC_DRAFTS = "pref_sync_drafts";
    public static final String KEY_SYNC_PUBLISHED = "pref_sync_published";
    public static final int SYNC_IMMEDIATELY = 0;
    public static final int SYNC_ON_EXIT = 1;
    public static final int SYNC_MANUALLY = 2;
    public static final int SYNC_DRAFT_DEFAULT = SYNC_IMMEDIATELY;
    public static final int SYNC_PUBLISHED_DEFAULT = SYNC_MANUALLY;

    /**
     * Account sync
     */
    public static final String KEY_ACCOUNT_SYNC_DRAFTS = "pref_account_sync_drafts";
    public static final String KEY_ACCOUNT_SYNC_PUBLISHED = "pref_account_sync_published";
    public static final int SYNC_USE_GLOBAL = -1;
    public static final int SYNC_ACCOUNT_DRAFT_DEFAULT = SYNC_USE_GLOBAL;
    public static final int SYNC_ACCOUNT_PUBLISHED_DEFAULT = SYNC_USE_GLOBAL;

}
