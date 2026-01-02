FROM node:22.20.0-slim AS build-frontend

RUN npm install -g pnpm@10.27.0

# see https://github.com/nodejs/docker-node/blob/master/docs/BestPractices.md#non-root-user
ENV USER=node
USER ${USER}
WORKDIR /home/${USER}

COPY --chown=${USER} frontend ./
RUN pnpm audit --audit-level critical || exit 1
RUN pnpm install --frozen-lockfile

RUN pnpm build:prod

FROM gradle:7-jdk17-alpine AS build-backend

ENV USER=gradle
USER ${USER}
RUN mkdir -p /home/gradle/app/build/resources/main/public/js
WORKDIR /home/gradle/app

COPY --chown=${USER} build.gradle .
COPY --chown=${USER} src ./src
RUN rm -f src/main/resources/public/js/*.js
COPY --chown=${USER} --from=build-frontend /home/node/dist/lvz-viz/browser ./src/main/resources/static/

RUN gradle --info assemble

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Sebastian Peters <Sebastian.Peters@gmail.com>" \
      org.opencontainers.image.authors="Sebastian Peters <Sebastian.Peters@gmail.com>" \
      org.opencontainers.image.source="https://github.com/CodeforLeipzig/lvz-viz" \
      org.opencontainers.image.vendor="Open Knowledge Foundation Deutschland e.V."

# see https://github.com/adoptium/containers/blob/main/21/jdk/alpine/3.21/Dockerfile#L26
ENV LANG='de_DE.UTF-8' LANGUAGE='de_DE:de' LC_ALL='de_DE.UTF-8'

RUN echo "Europe/Berlin" > /etc/timezone

ENV USER=lvz-viz

RUN addgroup ${USER} \
  && adduser -D -G ${USER} -S ${USER}

USER ${USER}
WORKDIR /home/${USER}

COPY --chown=${USER} dewac_175m_600.crf.ser.gz .
COPY --chown=${USER} --from=build-backend /home/gradle/app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XshowSettings:vm","-XX:MaxRAMPercentage=95","-jar","./app.jar"]
