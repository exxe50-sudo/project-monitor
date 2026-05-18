FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Configure Maven to use Aliyun mirror
COPY maven-settings.xml /root/.m2/settings.xml

# Copy root POM and filter to only needed modules
COPY pom.xml .
RUN sed -i '/<module>monitor-\(gateway\|collector\|alerter\|agent\)<\/module>/d' pom.xml

# Copy needed module POMs
COPY monitor-common/pom.xml monitor-common/
COPY monitor-server/pom.xml monitor-server/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl monitor-server -am -q

# Copy source code
COPY monitor-common/src monitor-common/src/
COPY monitor-server/src monitor-server/src/

# Build the module
RUN mvn package -pl monitor-server -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /build/monitor-server/target/*.jar app.jar

EXPOSE 8081

# Support JAVA_OPTS for memory configuration
ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD ["sh", "-c", "wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
