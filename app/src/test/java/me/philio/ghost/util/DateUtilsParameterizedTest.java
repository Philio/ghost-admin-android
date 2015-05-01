/*
 * Copyright 2015 Phil Bayfield
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import me.philio.ghost.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", emulateSdk = 21)
public class DateUtilsParameterizedTest {

    /**
     * Common date quantities in ms
     */
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long MONTH = DAY * 30;
    private static final long YEAR = DAY * 365;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new Date(System.currentTimeMillis() - 5 * SECOND), R.plurals.post_seconds, 5},
                {new Date(System.currentTimeMillis() - MINUTE), R.plurals.post_minutes, 1},
                {new Date(System.currentTimeMillis() - 2 * MINUTE), R.plurals.post_minutes, 2},
                {new Date(System.currentTimeMillis() - 5 * MINUTE), R.plurals.post_minutes, 5},
                {new Date(System.currentTimeMillis() - HOUR), R.plurals.post_hours, 1},
                {new Date(System.currentTimeMillis() - 5 * HOUR), R.plurals.post_hours, 5},
                {new Date(System.currentTimeMillis() - DAY), R.plurals.post_days, 1},
                {new Date(System.currentTimeMillis() - 5 * DAY), R.plurals.post_days, 5},
                {new Date(System.currentTimeMillis() - MONTH), R.plurals.post_months, 1},
                {new Date(System.currentTimeMillis() - 6 * MONTH), R.plurals.post_months, 6},
                {new Date(System.currentTimeMillis() - YEAR), R.plurals.post_years, 1},
                {new Date(System.currentTimeMillis() - 5 * YEAR), R.plurals.post_years, 5},
        });
    }

    private Date input;
    private int expectedId;
    private int expectedQuantity;

    public DateUtilsParameterizedTest(Date input, int expectedId, int expectedQuantity) {
        this.input = input;
        this.expectedId = expectedId;
        this.expectedQuantity = expectedQuantity;
    }

    @Test
    public void shouldFriendlyFormatDate() {
        assertThat(DateUtils.friendlyFormat(RuntimeEnvironment.application, input),
                is(getPlural(expectedId, expectedQuantity)));
    }

    private String getPlural(int id, int quantity) {
        return RuntimeEnvironment.application.getResources()
                .getQuantityString(id, quantity, quantity);
    }

}