package me.philio.ghostadmin.model;

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
