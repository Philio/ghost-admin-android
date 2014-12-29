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
package me.philio.ghost.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * A local database table to keep track of which android account data belongs to
 *
 * Created by phil on 08/12/2014.
 */
@Table(name = "blogs", id = BaseColumns._ID)
public class Blog extends Model {

    @Column(name = "url", uniqueGroups = {"account"})
    public String url;

    @Column(name = "email", uniqueGroups = {"account"})
    public String email;

}
