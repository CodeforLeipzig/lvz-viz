# lvz-viz - Docker

You can build and run the app within a Docker container.
Required version for the multi-stage build: Docker 19.03+.

## Getting started

Create an environment file for `docker` and `docker compose` and check the [configuration](#configuration).

```bash
cp default.env .env
```

## Configuration

### Table of contents

* [IMAGE](#image)
* [VERSION](#version)
* [ES_VERSION](#es_version)

### `IMAGE`

Defines the base image to use for lvz-viz.

* default: `sepe81/lvz-viz`
* type: `string`

### `VERSION`

Defines the version of the image to use for lvz-viz.

* default: `2.6.0`
* type: `string`

### `ES_VERSION`

Defines the version of elasticsearch.

* default: `6.8.23`
* type: `string`

## Usage

```bash
# build or rebuild services
docker compose build
# create and start containers
docker compose up -d
```

```bash
# build services and start containers with dev profile
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

```bash
# build services and start containers with prod profile
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

```bash
# view output from containers
docker compose logs -f
```

```bash
# stop and remove containers, networks, images, and volumes
docker compose down
```
