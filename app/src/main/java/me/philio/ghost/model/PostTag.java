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
