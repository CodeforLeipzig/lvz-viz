# lvz-viz

[![Code Climate](https://codeclimate.com/github/CodeforLeipzig/lvz-viz/badges/gpa.svg)](https://codeclimate.com/github/CodeforLeipzig/lvz-viz)

## Intro

Visualization of [LVZ police ticker](http://www.lvz-online.de/leipzig/polizeiticker/r-polizeiticker.html).

The official website is hosted at [https://lvz-viz.leipzig.codefor.de](https://lvz-viz.leipzig.codefor.de)
by [OK Lab Leipzig](http://codefor.de/projekte/2014-07-01-le-lvz_polizeiticker_visualisierung).

## Usage

Build and run the app with [npm](https://www.npmjs.com), [Grunt](http://gruntjs.com/) and [Maven](https://maven.apache.org/) or [Gradle](https://gradle.org). The crawling and indexing of new articles is activated by the startup parameter `--spring.profiles.active=crawl`.

### npm and Grunt

Download client js dependencies with npm and package them with Grunt during postinstall phase.

    npm install

### Apache Maven

You can build an executable jar with maven and run it as a separate process.

    mvn package
    java -jar target/lvz-viz-*.jar

Or you can simply run the project within maven during development.

    mvn spring-boot:run

### Gradle

You can build an executable jar with gradle and run it as a separate process.

    ./gradlew build
    java -jar build/libs/lvz-viz-*.jar

You can build an executable jar with gradle daemon and skip all tests to speed up the build.

    ./gradlew build -x test --daemon
    java -jar build/libs/lvz-viz-*.jar

Or you can simply run the project within gradle during development.

    ./gradlew bootRun
