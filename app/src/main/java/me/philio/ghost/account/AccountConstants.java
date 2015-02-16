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

package me.philio.ghost.account;

/**
 * Account related constants
 * <p/>
 * Created by phil on 01/12/2014.
 */
public class AccountConstants {

    /**
     * Token types
     */
    public static final String TOKEN_TYPE_ACCESS = "access_token";
    public static final String TOKEN_TYPE_REFRESH = "refresh_token";

    /**
     * Account user data keys
     */
    public static final String KEY_BLOG_URL = "blog_url";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ACCESS_TOKEN_TYPE = "access_token_type";
    public static final String KEY_ACCESS_TOKEN_EXPIRES = "expires";

}
