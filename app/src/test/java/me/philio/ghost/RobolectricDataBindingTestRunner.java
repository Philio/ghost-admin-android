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

package me.philio.ghost;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

    public class RobolectricDataBindingTestRunner extends RobolectricTestRunner {

        private static final String BUILD_OUTPUT = "build/intermediates";

        public RobolectricDataBindingTestRunner(Class<?> klass) throws InitializationError {
            super(klass);
        }

        @Override
        protected AndroidManifest getAppManifest(Config config) {
            if (config.constants() == Void.class) {
                Logger.error("Field 'constants' not specified in @Config annotation");
                Logger.error("This is required when using RobolectricGradleTestRunner!");
                throw new RuntimeException("No 'constants' field in @Config annotation!");
            }

            final String type = getType(config);
            final String flavor = getFlavor(config);
            final String applicationId = getApplicationId(config);

            final FileFsFile res;
            if (FileFsFile.from(BUILD_OUTPUT, "res", flavor, type).exists()) {
                res = FileFsFile.from(BUILD_OUTPUT, "res", flavor, type);
            } else {
                // Use res/merged if the output directory doesn't exist for Data Binding compatibility
                res = FileFsFile.from(BUILD_OUTPUT, "res/merged", flavor, type);
            }
            final FileFsFile assets = FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);

            final FileFsFile manifest;
            if (FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
                manifest = FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
            } else {
                // Fallback to the location for library manifests
                manifest = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
            }

            Logger.debug("Robolectric assets directory: " + assets.getPath());
            Logger.debug("   Robolectric res directory: " + res.getPath());
            Logger.debug("   Robolectric manifest path: " + manifest.getPath());
            Logger.debug("    Robolectric package name: " + applicationId);
            return new AndroidManifest(manifest, res, assets, applicationId);
        }

        private String getType(Config config) {
            try {
                return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
            } catch (Throwable e) {
                return null;
            }
        }

        private String getFlavor(Config config) {
            try {
                return ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
            } catch (Throwable e) {
                return null;
            }
        }

        private String getApplicationId(Config config) {
            try {
                return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
            } catch (Throwable e) {
                return null;
            }
        }

    }
