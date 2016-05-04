lvz-viz
=======

[![Code Climate](https://codeclimate.com/github/CodeforLeipzig/lvz-viz/badges/gpa.svg)](https://codeclimate.com/github/CodeforLeipzig/lvz-viz)

Visualization of [LVZ police ticker](http://www.lvz-online.de/leipzig/polizeiticker/r-polizeiticker.html).

The official website is hosted at [https://lvz-viz.leipzig.codefor.de](https://lvz-viz.leipzig.codefor.de) by [OK Lab Leipzig](http://codefor.de/projekte/2014-07-01-le-lvz_polizeiticker_visualisierung).


# Usage

Run the app with Maven or Gradle.

Apache Maven

    # build and run project as jar
    mvn package
    java -jar target/lvz-viz-*.jar

    # run project within maven
    mvn spring-boot:run

Gradle

    # build and run project as jar
    ./gradlew build
    java -jar build/libs/lvz-viz-*.jar

    # run project within gradle
    ./gradlew bootRun
