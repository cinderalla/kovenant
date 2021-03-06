[![CircleCI branch](https://img.shields.io/circleci/project/mplatvoet/kovenant/master.svg)](https://circleci.com/gh/mplatvoet/kovenant/tree/master) [![Maven Central](https://img.shields.io/maven-central/v/nl.komponents.kovenant/kovenant.svg)](http://search.maven.org/#browse%7C1069530195) [![DUB](https://img.shields.io/dub/l/vibe-d.svg)](https://github.com/mplatvoet/kovenant/blob/master/LICENSE)

#Kovenant
[Promises](http://en.wikipedia.org/wiki/Futures_and_promises) for [Kotlin](http://kotlinlang.org). 

The easy asynchronous library for Kotlin. With extensions for Android, LMAX Disruptor, JavaFX and much more.

```kt
async { "world" } and async { "Hello" } success {
    println("${it.second} ${it.first}!")
}
```

Please refer to the [Kovenant](http://kovenant.komponents.nl) site for API usage and more.
 
## Getting started
This version is build against `kotlin-stdlib:0.14.449`.
Source and target compatibility is `1.6`

###Gradle
```groovy
dependencies {
    compile 'nl.komponents.kovenant:kovenant:2.7.+'
}
```

###Maven
```xml
<dependency>
	<groupId>nl.komponents.kovenant</groupId>
	<artifactId>kovenant</artifactId>
	<version>[2.7.0,2.8.0)</version>
</dependency>
```

###Android Demo app
Checkout the [Android Demo App on Github](https://github.com/mplatvoet/kovenant-android-demo).

###Artifacts
Kovenant has been structured in sub projects so you can cherry pick what you need. 

|artifact            |description                                                                                        |
|--------------------|---------------------------------------------------------------------------------------------------|
|kovenant            |Container artifact that consists of `kovenant-core`, `kovenant-combine`, `kovenant-jvm` and `kovenant-functional`|
|kovenant-core       |The core of kovenant. Provides the API and default implementations                                 |
|kovenant-combine    |Adds combine functionality that keep everything strongly typed                                     |
|kovenant-jvm        |Support for converting between Executors and Dispatchers                                           |
|kovenant-ui         |Support for UI frameworks that need UI work to operate on a specific process                       |
|kovenant-android    |Extensions for Android specific needs                                                              | 
|kovenant-jfx        |Extensions for JavaFX specific needs                                                               | 
|kovenant-disruptor  |LMAX Disruptor work queues                                                                         | 
|kovenant-progress   |Progress configuration helper                                                                      | 
|kovenant-functional |Functional Programming idiomatic additions                                                         | 

##Issues 
Issues are tracked in [Youtrack](http://issues.komponents.nl/youtrack/issues?q=project%3A+Kovenant)

##Release notes
See [Changelog](http://kovenant.komponents.nl/changelog/) for release notes

## Recommended libraries:
Other libraries for Kotlin applications

* [Injekt](https://github.com/kohesive/injekt) - Crazily easy Dependency Injection for Kotlin
* [Fuel](https://github.com/kittinunf/Fuel) - The easiest HTTP networking library in Kotlin for Android.