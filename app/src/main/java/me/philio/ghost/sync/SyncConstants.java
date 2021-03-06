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

/**
 * Sync related constants
 * <p/>
 * Created by phil on 22/12/2014.
 */
public class SyncConstants {

    /**
     * Broadcast actions
     */
    public static final String ACTION_SYNC_STARTED = "me.philio.ghost.sync.action.SYNC_STARTED";
    public static final String ACTION_SYNC_FINISHED = "me.philio.ghost.sync.action.SYNC_FINISHED";

    /**
     * Broadcast extras
     */
    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_RESULT = "result";

}
