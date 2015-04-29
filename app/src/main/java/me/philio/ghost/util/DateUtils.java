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

import android.content.Context;

import java.util.Date;

import me.philio.ghost.R;

/**
 * Utils to display friendly date formats like the web admin
 */
public class DateUtils {

    /**
     * ms in a second
     */
    private static final long SECOND = 1000;

    /**
     * ms in a minute
     */
    private static final long MINUTE = SECOND * 60;

    /**
     * ms in an hour
     */
    private static final long HOUR = MINUTE * 60;

    /**
     * ms in a day
     */
    private static final long DAY = HOUR * 24;

    /**
     * ms in a month
     */
    private static final long MONTH = DAY * 30;

    /**
     * ms in a year
     */
    private static final long YEAR = DAY * 365;

    /**
     * Format a date
     *
     * @param context
     * @param date
     * @return
     */
    public static String format(Context context, Date date) {
        long difference = System.currentTimeMillis() - date.getTime();

        if (difference < MINUTE) {
            return context.getResources().getQuantityString(R.plurals.post_seconds,
                    (int) (difference / SECOND));
        } else if (difference < HOUR) {
            return context.getResources().getQuantityString(R.plurals.post_minutes,
                    (int) (difference / MINUTE), (int) (difference / MINUTE));
        } else if (difference < DAY) {
            return context.getResources().getQuantityString(R.plurals.post_hours,
                    (int) (difference / HOUR), (int) (difference / HOUR));
        } else if (difference < MONTH) {
            return context.getResources().getQuantityString(R.plurals.post_days,
                    (int) (difference / DAY), (int) (difference / DAY));
        } else if (difference < YEAR) {
            return context.getResources().getQuantityString(R.plurals.post_months,
                    (int) (difference / MONTH), (int) (difference / MONTH));
        } else if (difference > 0) {
            return context.getResources().getQuantityString(R.plurals.post_years,
                    (int) (difference / YEAR), (int) (difference / YEAR));
        }
        return null;
    }

}
