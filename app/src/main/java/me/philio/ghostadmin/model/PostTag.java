package me.philio.ghostadmin.model;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * A link table for posts and tags
 *
 * Created by phil on 04/12/2014.
 */
@Table(name = "posts_tags", id = BaseColumns._ID)
public class PostTag extends Model {

    @Column(name = "post_id", uniqueGroups = {"posts_tags"},
            onUniqueConflicts = {Column.ConflictAction.REPLACE},
            onDelete = Column.ForeignKeyAction.CASCADE,
            onUpdate = Column.ForeignKeyAction.CASCADE)
    public Post post;

    @Column(name = "tag_id", uniqueGroups = {"posts_tags"},
            onUniqueConflicts = {Column.ConflictAction.REPLACE},
            onDelete = Column.ForeignKeyAction.CASCADE,
            onUpdate = Column.ForeignKeyAction.CASCADE)
    public Tag tag;

}
