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

import java.util.Date;

/**
 * A temporary table to store a copy of a remote post that is conflicted with the local version
 */
@Table(name = "posts_conflict", id = BaseColumns._ID)
public class PostConflict extends Model {

    @Column(name = "blog_id", notNull = true)
    public Blog blog;

    @Column(name = "post_id", notNull = true, unique = true,
            onUniqueConflict = Column.ConflictAction.REPLACE,
            onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Post post;;

    @Column(name = "title")
    public String title;

    @Column(name = "markdown")
    public String markdown;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Column(name = "updated_by")
    public int updatedBy;

}
