FROM java:8-jre
MAINTAINER Saagie <pierre@saagie.com>

ENTRYPOINT ["/usr/bin/java", "-Djava.security.egd=file:/dev/./urandom","-jar", "/usr/share/aston-parking.jar"]

# Add the service itself
ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/aston-parking.jar