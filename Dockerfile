# ================================
# Stage 1: Build
# ================================
FROM eclipse-temurin:25-jdk-noble AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src/ src/

RUN ./mvnw clean package -DskipTests -B

# ================================
# Stage 2: Run
# ================================
FROM eclipse-temurin:25-jre-noble AS runtime

# Security: run as non-root user
RUN groupadd --system --gid 1001 spring && \
    useradd --system --uid 1001 --gid spring spring

WORKDIR /app

# Copy artifact from build stage
COPY --from=builder /app/target/cloudary*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

USER spring

# JVM tuning for containers
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=60 \
  -XX:InitialRAMPercentage=30 \
  -XX:MaxMetaspaceSize=384m \
  -XX:MaxDirectMemorySize=256m \
  -XX:+UseG1GC \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.backgroundpreinitializer.ignore=true"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]