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
npm install --no-progress
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

Required version for the multi-stage build: Docker 19.03+

```bash
-- Build or rebuild services
docker-compose build
-- Create and start containers
docker-compose up -d
```

```bash
-- Build services and start containers with dev profile
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

```bash
-- View output from containers
docker-compose logs -f
```

```bash
-- Stop and remove containers, networks, images, and volumes
docker-compose down
```
