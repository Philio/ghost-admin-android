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
public class DateUtilsTest {

    @ParameterizedRobolectricTestRunner.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new Date(System.currentTimeMillis() - 5 * 1000), R.plurals.post_seconds, 5},
                {new Date(System.currentTimeMillis() - 60000), R.plurals.post_minutes, 1},
                {new Date(System.currentTimeMillis() - 2 * 60000), R.plurals.post_minutes, 2},
                {new Date(System.currentTimeMillis() - 5 * 60000), R.plurals.post_minutes, 5},
                {new Date(System.currentTimeMillis() - 3600000), R.plurals.post_hours, 1},
                {new Date(System.currentTimeMillis() - 5 * 3600000), R.plurals.post_hours, 5},
                {new Date(System.currentTimeMillis() - 86400000), R.plurals.post_days, 1},
                {new Date(System.currentTimeMillis() - 5 * 86400000), R.plurals.post_days, 5},
                {new Date(System.currentTimeMillis() - 30l * 86400000l), R.plurals.post_months, 1},
                {new Date(System.currentTimeMillis() - 5l * 30l * 86400000l), R.plurals.post_months, 5},
                {new Date(System.currentTimeMillis() - 365l * 86400000l), R.plurals.post_years, 1},
                {new Date(System.currentTimeMillis() - 5l * 365l * 86400000l), R.plurals.post_years, 5},
        });
    }

    private Date input;
    private int expectedId;
    private int expectedQuantity;

    public DateUtilsTest(Date input, int expectedId, int expectedQuantity) {
        this.input = input;
        this.expectedId = expectedId;
        this.expectedQuantity = expectedQuantity;
    }

    @Test
    public void testFormat() {
        assertThat(DateUtils.format(RuntimeEnvironment.application, input),
                is(getPlural(expectedId, expectedQuantity)));
    }

    private String getPlural(int id, int quantity) {
        return RuntimeEnvironment.application.getResources().getQuantityString(id, quantity, quantity);
    }

}