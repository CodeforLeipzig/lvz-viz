FROM node:16-alpine AS build-js

# see https://github.com/nodejs/docker-node/blob/master/docs/BestPractices.md#non-root-user
ENV USER node
USER ${USER}
WORKDIR /home/${USER}

COPY --chown=${USER} package*.json ./
RUN npm install --only=production

COPY --chown=${USER} Gruntfile.js ./
COPY --chown=${USER} src/main/resources/public/js ./src/main/resources/public/js
RUN npm run --silent grunt-build

FROM gradle:7.2.0-jdk11 AS build-java

ENV USER gradle
USER ${USER}
RUN mkdir -p /home/gradle/app/build/resources/main/public/js
WORKDIR /home/gradle/app

COPY --chown=${USER} build.gradle .
COPY --chown=${USER} src ./src
RUN rm -f src/main/resources/public/js/*.js
COPY --chown=${USER} --from=build-js /home/node/build/resources/main/public/js/app.min.js ./src/main/resources/public/js/

RUN gradle --info assemble

FROM eclipse-temurin:11-jdk-alpine

LABEL maintainer="Sebastian Peters <Sebastian.Peters@gmail.com>" \
      org.opencontainers.image.authors="Sebastian Peters <Sebastian.Peters@gmail.com>" \
      org.opencontainers.image.source="https://github.com/CodeforLeipzig/lvz-viz" \
      org.opencontainers.image.vendor="Open Knowledge Foundation Deutschland e.V."

# see https://github.com/adoptium/containers/blob/main/11/jdk/alpine/Dockerfile.releases.full#L22
ENV LANG='de_DE.UTF-8' LANGUAGE='de_DE:de' LC_ALL='de_DE.UTF-8'

RUN echo "Europe/Berlin" > /etc/timezone

ENV USER lvz-viz

RUN addgroup ${USER} \
  && adduser -D -G ${USER} -S ${USER}

USER ${USER}
WORKDIR /home/${USER}

COPY --chown=${USER} dewac_175m_600.crf.ser.gz .
COPY --chown=${USER} --from=build-java /home/gradle/app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XshowSettings:vm","-XX:MaxRAMPercentage=95","-jar","./app.jar"]
