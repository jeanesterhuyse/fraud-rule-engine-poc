FROM public.ecr.aws/amazoncorretto/amazoncorretto:21

ARG JAR_FILE=fraud-rule-engine-api-1.0.0-SNAPSHOT.jar
ENV JAR_FILE=$JAR_FILE

COPY target/$JAR_FILE /

USER 1000
EXPOSE 8080
ENV TZ="Africa/Johannesburg"

ENTRYPOINT exec java $JAVA_OPTS -jar /$JAR_FILE
