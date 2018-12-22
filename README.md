# lvz-viz

[![Build Status](https://travis-ci.org/sepe81/lvz-viz.svg?branch=master)](https://travis-ci.org/sepe81/lvz-viz)
![GitHub license](https://img.shields.io/github/license/CodeforLeipzig/lvz-viz.svg)
[![Code Climate](https://codeclimate.com/github/CodeforLeipzig/lvz-viz/badges/gpa.svg)](https://codeclimate.com/github/CodeforLeipzig/lvz-viz)

## Intro

Visualization of [LVZ police ticker](http://www.lvz-online.de/leipzig/polizeiticker/r-polizeiticker.html).

The official website is hosted at [https://lvz-viz.leipzig.codefor.de](https://lvz-viz.leipzig.codefor.de)
by [OK Lab Leipzig](http://codefor.de/projekte/2014-07-01-le-lvz_polizeiticker_visualisierung.html).

## Usage

Build and run the app with [npm](https://www.npmjs.com), [Grunt](http://gruntjs.com/) and [Gradle](https://gradle.org).
The crawling and indexing of new articles is activated by the startup parameter `--spring.profiles.active=crawl`
or by setting the environment via `export SPRING_PROFILES_ACTIVE=crawl`.

### npm and Grunt

Download client js dependencies with npm and package them with Grunt.

```bash
npm install
npm run grunt-build
```

### Gradle

You can build an executable jar with gradle and run it as a separate process.

```bash
./gradlew build
java -jar build/libs/lvz-viz-*.jar
```

You can build an executable jar with gradle and skip all tests to speed up the build.

```bash
./gradlew build -x test
java -jar build/libs/lvz-viz-*.jar
```

Or you can simply run the project within gradle during development.

```bash
./gradlew bootRun
```

### Docker

You can build and run the app within a Docker container.

Required version for the multi-stage build: Docker 17.05+

```bash
docker-compose build
docker-compose up -d
docker-compose logs -f
docker-compose down
```
