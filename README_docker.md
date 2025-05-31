# LVZ Polizeiticker - docker

## Getting started

You can build and run the app within a Docker container.

Required version for the multi-stage build: Docker 19.03+

```bash
-- Build or rebuild services
dockercompose build
-- Create and start containers
docker compose up -d
```

```bash
-- Build services and start containers with dev profile
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

```bash
-- Build services and start containers with prod profile
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

```bash
-- View output from containers
docker compose logs -f
```

```bash
-- Stop and remove containers, networks, images, and volumes
docker compose down
```
