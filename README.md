# autoparse-json

Autoparse JSON is a java library built specifically for Android that uses code generation to parse JSON into custom objects in your project.

Learn how to use Autoparse JSON in the [wiki](https://github.com/workday/autoparse-json/wiki)!

**Latest Version:**  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.workday/autoparse-json/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.workday/autoparse-json)

**Build Status:** [![Circle CI](https://circleci.com/gh/Workday/autoparse-json.svg?style=svg)](https://circleci.com/gh/Workday/autoparse-json)

## Installation

Add the following lines to your `build.gradle` file, replacing `$autoparse_json_version` with latest version from Maven Central.

```
repositories {
    mavenCentral()
}

dependencies {
    compile "com.workday:autoparse-json:$autoparse_json_version"
    compile "com.workday:autoparse-json-processor:$autoparse_json_version"
}
```

Note that if you use the [android-apt plugin](https://bitbucket.org/hvisser/android-apt) or the [kotlin-android plugin](https://kotlinlang.org/docs/reference/using-gradle.html), you may use `apt` or `kapt` respectively instead of `compile` for `autoparse-json-processor`, e.g.

```
apt "com.workday:autoparse-json-processor:$autoparse_json_version"
```
In fact, it is highly recommended that you use `apt` or `kapt` as this will get rid of some "invalid package" and related warnings.
