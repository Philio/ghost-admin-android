Ghost Writer for Android
========================

# About

An open source offline editor for the [Ghost](https://ghost.org/) blogging software.

The app is under active development, but also being used as a test bed for new features from Android
5.0 Lollipop and Google's material design so no release date is currently set. The app is nearing
feature completeness and I am hoping to push a release to [Google Play](https://play.google.com/) in
the coming weeks.

Contributions to the project are welcome, please contact me to discuss if you're interested.

# Features and progress

This is a high level view of the main features and current progress and is very likely to change.

* Implement Ghost API [COMPLETE]
* Login to multiple blogs [COMPLETE]
* Sync data to the device for offline usage [COMPLETE]
* Sync changes from the device to the server [COMPLETE]
* Navigation and account switching [COMPLETE]
* Display list of posts and pages on a blog [COMPLETE]
* Markdown rich text editor [COMPLETE]
* HTML viewer [COMPLETE]
* Image upload [NOT STARTED]
* App settings [COMPLETE]
* Tablet layouts [NOT STARTED]
* Conflict resolution [IN PROGRESS]

# Building the project

The latest version of Android Studio (1.1) is recommended. Checked in version of code should build
and run, but is not guaranteed complete or bug free at this stage.

# Target devices

Should work on all devices from Honeycomb 3.0 upwards. Originally also supporting Gingerbread, this
has been pulled for now due to bugs.

# Third party libraries

- [Retrofit](http://square.github.io/retrofit/)
- [Picasso](http://square.github.io/picasso/)
- [ActiveAndroid](http://www.activeandroid.com/) (Custom build with modified sync to network setting)
- [Butter Knife](http://jakewharton.github.io/butterknife/)
- [Commons IO](http://commons.apache.org/proper/commons-io/)
- [PreferenceFragment-Compat](https://github.com/Machinarius/PreferenceFragment-Compat)
- [CWAC AndDown](https://github.com/commonsguy/cwac-anddown)

# License

Copyright 2014 Phil Bayfield

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

&nbsp;&nbsp;&nbsp;&nbsp;http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.