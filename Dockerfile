FROM java:8-jre-alpine
COPY build/libs/quarantyne-1.0-SNAPSHOT-all.jar /usr/local/quarantyne.jar
WORKDIR /usr/local/
  CMD java -jar /usr/local/quarantyne.jar --proxyHost 0.0.0.0 --proxyPort 8080
EXPOSE 8080