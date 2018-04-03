FROM node:carbon AS build-js

RUN npm install -g grunt-cli

ENV USER app
RUN adduser --disabled-password -gecos '' $USER

USER $USER
WORKDIR /home/$USER

COPY --chown=app Gruntfile.js package*.json ./
COPY --chown=app src/main/resources/public/js ./src/main/resources/public/js

RUN npm install

FROM gradle:4.6.0-jdk8 AS build-java

USER gradle
RUN mkdir -p /home/gradle/app/build/resources/main/public/js
WORKDIR /home/gradle/app

COPY --chown=gradle build.gradle .
COPY --chown=gradle src ./src
COPY --chown=gradle --from=build-js /home/app/build/resources/main/public/js ./build/resources/main/public/js

RUN gradle --no-daemon build -x test

FROM anapsix/alpine-java:8_server-jre_unlimited

LABEL maintainer="Sebastian Peters <Sebastian.Peters@gmail.com>"

ARG APP_JAR=app-1.2.0-SNAPSHOT.jar
ENV USER lvz-viz

RUN addgroup $USER \
  && adduser -D -G $USER -S $USER

USER $USER
WORKDIR /home/$USER

COPY --chown=lvz-viz dewac_175m_600.crf.ser.gz .
COPY --chown=lvz-viz --from=build-java /home/gradle/app/build/libs/$APP_JAR ./app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]
