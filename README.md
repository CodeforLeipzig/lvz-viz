# lvz-viz

<div align="center">

![GitHub license](https://img.shields.io/github/license/CodeforLeipzig/lvz-viz.svg)
[![Java CI with Gradle](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/java_ci.yml/badge.svg)](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/java_ci.yml)
[![Node CI](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/node_ci.yml/badge.svg)](https://github.com/CodeforLeipzig/lvz-viz/actions/workflows/node_ci.yml)
[![Maintainability](https://qlty.sh/gh/CodeforLeipzig/projects/lvz-viz/maintainability.svg)](https://qlty.sh/gh/CodeforLeipzig/projects/lvz-viz)
[![Code Coverage](https://qlty.sh/gh/CodeforLeipzig/projects/lvz-viz/coverage.svg)](https://qlty.sh/gh/CodeforLeipzig/projects/lvz-viz)

</div>

## Intro

Visualization of [LVZ police ticker](https://www.lvz.de/Leipzig/Polizeiticker/Polizeiticker-Leipzig).

The official website is hosted at <https://lvz-viz.leipzig.codefor.de>
by [OK Lab Leipzig](http://codefor.de/projekte/2014-07-01-le-lvz_polizeiticker_visualisierung.html).

## Prerequisites

### Java

* `jdk 17` or higher

We recommend the usage of [The Software Development Kit Manager](https://sdkman.io/).

### Node and pnpm

* `node 22.20.0` or higher

We recommend the usage of [Node Version Manager](https://github.com/nvm-sh/nvm).

* `pnpm 10.27.0` or higher

Check out the [installation instructions](https://pnpm.io/installation).

### Angular CLI

* `@angular/cli 21.0.4` or higher

Install @angular/cli by running:

```bash
pnpm install -g @angular/cli@21
```

### Docker (when running services within docker)

* `docker 28.0.2` or higher

## Usage

Build and run the app with [pnpm](https://pnpm.io) and [Gradle](https://gradle.org).

The crawling and indexing of new articles is activated by default.
It can be delayed by setting the startup parameter `--app.initialDelay=<time in ms>` to a high value (e.g. `1800000` for 30 minutes)
or by setting an environment variable via `export APP_INITIALDELAY=<time in ms>`.

Profiles (`dev|local|prod|test`) can be set by the startup parameter `--spring.profiles.active=<profile>`
or by setting an environment variable via `export SPRING_PROFILES_ACTIVE=<profile>`.

Please use the `prod` profile for production systems with a dedicated data volume (see `docker-compose.prod.yml`).

### Read more

Check the documentation for each module.

For frontend check [lvz-viz - Frontend](./frontend/README.md).

For docker check [lvz-viz - Docker](./README_docker.md).

### Set node version

Use appropriate node and pnpm version via [nvm](https://github.com/nvm-sh/nvm#nvmrc).

```bash
nvm use
```

### Starting development

#### Gradle

For local development and testing you need to start up elasticsearch via [docker-compose](https://docs.docker.com/compose/).

```bash
docker compose up -d elasticsearch
```

You can build and test an executable jar with Gradle.

```bash
./gradlew build
```

You can run a specific test with Gradle.

```bash
./gradlew test --tests "*CrawlSchedulerTest"
```

You can build an executable jar with Gradle and run it as a separate process.

```bash
./gradlew assemble -PlocalBuild=true
java -jar build/libs/lvz-viz-*.jar --spring.profiles.active=local
```

Or you can run the project within Gradle during development.

```bash
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

### Running backend and frontend together

For development, you can use two separate terminals for starting backend and frontend separately.
For further information, please refer to the README files.

You could also use the following command in the root folder to start development in one single terminal:
Run the following command to install:

```bash
# install dependencies
pnpm install

# start both development server
pnpm start
```

## Maintenance

```bash
# display dependency updates
./gradlew dependencyUpdates
```
