# lvz-viz

[![Build Status](https://travis-ci.org/sepe81/lvz-viz.svg?branch=master)](https://travis-ci.org/sepe81/lvz-viz)
![GitHub license](https://img.shields.io/github/license/CodeforLeipzig/lvz-viz.svg)
[![Code Climate](https://codeclimate.com/github/CodeforLeipzig/lvz-viz/badges/gpa.svg)](https://codeclimate.com/github/CodeforLeipzig/lvz-viz)

[![Java CI with Gradle](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/java_ci.yml/badge.svg)](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/java_ci.yml)

## Intro

Visualization of [LVZ police ticker](https://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig).

The official website is hosted at <https://lvz-viz.leipzig.codefor.de>
by [OK Lab Leipzig](http://codefor.de/projekte/2014-07-01-le-lvz_polizeiticker_visualisierung.html).

## Usage

Build and run the app with [npm](https://www.npmjs.com), [Grunt](http://gruntjs.com/) and [Gradle](https://gradle.org).

The crawling and indexing of new articles is activated by default.
It can be delayed by setting the startup parameter `--app.initialDelay=<time in ms>` to a high value (e.g. `1800000` for 30 minutes)
or by setting an environment variable via `export APP_INITIALDELAY=<time in ms>`.

Profiles (`dev|local|prod|test`) can be set by the startup parameter `--spring.profiles.active=<profile>`
or by setting an environment variable via `export SPRING_PROFILES_ACTIVE=<profile>`.

Please use the `prod` profile for production systems with a dedicated data volume (see `docker-compose.prod.yml`).

### npm and Grunt

Use appropriate node and npm version via [nvm](https://github.com/nvm-sh/nvm#nvmrc).

```bash
nvm use
```

Download client js dependencies with npm and package them with Grunt.

```bash
npm install --no-progress
npm run grunt-build
```

### Gradle

For local development and testing you need to startup elasticsearch via [docker-compose](https://docs.docker.com/compose/).

```bash
docker-compose up -d elasticsearch
```

You can build and test an executable jar with gradle.

```bash
./gradlew build
```

You can run a specific test with gradle.

```bash
./gradlew test --tests "*CrawlSchedulerTest"
```

You can build an executable jar with gradle and run it as a separate process.

```bash
./gradlew assemble
java -jar build/libs/lvz-viz-*.jar --spring.profiles.active=local
```

Or you can simply run the project within gradle during development.

```bash
export SPRING_PROFILES_ACTIVE=local
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
-- Build services and start containers with prod profile
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

```bash
-- View output from containers
docker-compose logs -f
```

```bash
-- Stop and remove containers, networks, images, and volumes
docker-compose down
```

## Maintenance

```bash
-- Display dependency updates
./gradlew dependencyUpdates
```
