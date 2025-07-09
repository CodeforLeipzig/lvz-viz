# LVZ Polizeiticker - backend

## Getting started

```bash
# all commands used in ./backend
cd backend
```

## Usage

### General

The crawling and indexing of new articles is activated by default.
It can be delayed by setting the startup parameter `--app.initialDelay=<time in ms>` to a high value (e.g. `1800000` for 30 minutes)
or by setting an environment variable via `export APP_INITIALDELAY=<time in ms>`.

Profiles (`dev|local|prod|test`) can be set by the startup parameter `--spring.profiles.active=<profile>`
or by setting an environment variable via `export SPRING_PROFILES_ACTIVE=<profile>`.

Please use the `prod` profile for production systems with a dedicated data volume (see `docker-compose.prod.yml`).

### Run in development mode

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
./gradlew test --tests *CrawlSchedulerTest
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

## Maintenance

```bash
-- Display dependency updates
./gradlew dependencyUpdates
```
