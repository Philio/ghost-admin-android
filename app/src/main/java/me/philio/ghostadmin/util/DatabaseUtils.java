package me.philio.ghostadmin.util;

import com.activeandroid.query.Select;

import me.philio.ghostadmin.model.Blog;

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
        return new Select().from(Blog.class).where("url = ? AND email = ?", blogUrl, email)
                .executeSingle();
    }

}
