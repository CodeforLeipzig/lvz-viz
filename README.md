lvz-viz
=======

Visualization of [LVZ police ticker](http://www.lvz-online.de/leipzig/polizeiticker/r-polizeiticker.html).

[http://leipzig.codefor.de/lvz-viz](http://leipzig.codefor.de/lvz-viz)

# Usage

First change directory to lvz-viz and startup backend with Maven or Gradle.

    cd lvz-viz

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