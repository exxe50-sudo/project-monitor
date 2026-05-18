FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Configure Maven to use Aliyun mirror
COPY maven-settings.xml /root/.m2/settings.xml

# Copy root POM and filter to only needed modules
COPY pom.xml .
RUN sed -i '/<module>monitor-\(server\|collector\|alerter\|common\|agent\)<\/module>/d' pom.xml

# Copy needed module POM
COPY monitor-gateway/pom.xml monitor-gateway/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl monitor-gateway -am -q

# Copy source code
COPY monitor-gateway/src monitor-gateway/src/

# Build the module
RUN mvn package -pl monitor-gateway -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /build/monitor-gateway/target/*.jar app.jar

EXPOSE 8080

# Support JAVA_OPTS for memory configuration
ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD ["sh", "-c", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
