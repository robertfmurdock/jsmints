FROM roamingthings/kotlin-native

WORKDIR /usr/src/app

USER root
COPY build.gradle.kts gradlew /user/src/app/
COPY gradle /user/src/app/gradle
RUN ["/user/src/app/gradlew"]

COPY . /usr/src/app
RUN ["/user/src/app/gradlew", "build", "check"]

CMD [ "./gradlew", "build", "check" ]
