FROM develar/java

MAINTAINER Sebastian Peters <Sebastian.Peters@gmail.com>

ENV APP_NAME lvz-viz
ENV APP_VERSION 1.2.0-SNAPSHOT
ENV USER_NAME $APP_NAME
ENV USER_HOME /home/$USER_NAME

RUN adduser -S $USER_NAME

WORKDIR $USER_HOME

COPY dewac_175m_600.crf.ser.gz .
COPY build/libs/$APP_NAME-$APP_VERSION.jar ./app.jar

RUN chown -R $USER_NAME $USER_HOME

USER $USER_NAME

VOLUME $USER_HOME/data
VOLUME /tmp

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","./app.jar"]
