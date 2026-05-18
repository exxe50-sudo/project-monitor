FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Configure Maven to use Aliyun mirror
COPY maven-settings.xml /root/.m2/settings.xml

# Copy root POM and filter to only needed modules
COPY pom.xml .
RUN sed -i '/<module>monitor-\(gateway\|server\|collector\|alerter\|common\)<\/module>/d' pom.xml

# Copy needed module POM
COPY monitor-agent/pom.xml monitor-agent/

# Copy source code
COPY monitor-agent/src monitor-agent/src/

# Build the module (protobuf code will be generated during build)
RUN cd monitor-agent && mvn clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /build/monitor-agent/target/*.jar app.jar

# Support JAVA_OPTS for memory configuration in K8s
ENV JAVA_OPTS="-Xms64m -Xmx128m"
ENV AGENT_COLLECTOR_HOST="localhost"
ENV AGENT_COLLECTOR_PORT="9090"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD ["sh", "-c", "ps aux | grep app.jar | grep -v grep || exit 1"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
