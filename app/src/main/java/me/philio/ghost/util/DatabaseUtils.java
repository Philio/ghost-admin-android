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

import com.activeandroid.query.Select;

import me.philio.ghost.model.Blog;
import me.philio.ghost.model.User;

/**
 * Utils for simplifying some common database operations
 *
 * Created by phil on 18/12/2014.
 */
public class DatabaseUtils {

    /**
     * Get the blog record for a url/email
     *
     * @param blogUrl The url of the blog
     * @param email   The user's email address
     * @return A blog record
     */
    public static Blog getBlog(String blogUrl, String email) {
        return new Select()
                .from(Blog.class)
                .where("url = ? AND email = ?", blogUrl, email)
                .executeSingle();
    }

    /**
     * Get the user record for a blog/email
     *
     * @param blog  The blog
     * @param email The user's email address
     * @return A user record
     */
    public static User getUser(Blog blog, String email) {
        return new Select()
                .from(User.class)
                .where("blog_id = ? AND email = ?", blog.getId(), email)
                .executeSingle();
    }

}
