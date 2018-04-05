FROM java:8-jre
MAINTAINER Saagie <kevin@saagie.com>

ENTRYPOINT ["/usr/bin/java", "-Djava.security.egd=file:/dev/./urandom","-jar", "/usr/share/croissants-kotlin.jar"]

# Add the service itself
ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/croissants-kotlin.jar