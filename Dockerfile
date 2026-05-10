#FROM eclipse-temurin:17-jdk

# ═══════════════════════════════════════════════════════════════
# STAGE 1: BUILDER
# Purpose: download dependencies, compile code, produce app.jar
# This stage is temporary — it gets discarded after the build
# ═══════════════════════════════════════════════════════════════

# Start from an image that has both Maven AND Java 17 (JDK)
# We need Maven to download deps and compile
# We need JDK (not just JRE) because JDK can compile .java files
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set the working directory inside this temporary container
# All following commands run from /app
WORKDIR /app

# ── Dependency caching trick ───────────────────────────────────
# Copy ONLY pom.xml first (not your source code yet)
# Why? Docker builds in layers. If pom.xml hasn't changed,
# Docker skips the next RUN and uses its cached layer.
# This saves 2-3 minutes on every rebuild.
COPY pom.xml .

# Download all dependencies declared in pom.xml
# -B = batch mode (cleaner logs, no interactive prompts)
# go-offline = download everything so the next step needs no internet
RUN mvn dependency:resolve -B && \
    mvn dependency:resolve-plugins -B

# ── Build ──────────────────────────────────────────────────────
# NOW copy your source code (after dependencies are cached)
# If only your code changed (not pom.xml), Docker skips
# the dependency download above and jumps straight here
COPY src ./src

# Compile source code + bundle everything into one fat JAR
# -DskipTests = don't run tests during build (run them in CI separately)
# -B = batch mode
# Output: /app/target/student-service-0.0.1-SNAPSHOT.jar
RUN mvn clean package -DskipTests -B


# ═══════════════════════════════════════════════════════════════
# STAGE 2: RUNNER
# Purpose: run the app.jar in a clean, minimal, secure image
# This is the FINAL image — what actually gets deployed
# ═══════════════════════════════════════════════════════════════

# Fresh start — this image has NO Maven, NO JDK, NO source code
# JRE = Java Runtime Environment (can only RUN java, not compile it)
# alpine = tiny Linux distro (~5MB), keeps image small and secure
FROM eclipse-temurin:17-jre-alpine

# Set working directory inside the final container
WORKDIR /app

# ── Copy the JAR from Stage 1 ──────────────────────────────────
# --from=builder = grab this file from Stage 1 (not your machine)
# /app/target/*.jar = the fat JAR Maven produced
# app.jar = what we name it inside this final image
# This is the ONLY thing that crosses from Stage 1 to Stage 2
COPY --from=builder /app/target/*.jar app.jar

# Document that the app listens on port 8080
# This is informational — Docker Compose maps it via ports:
EXPOSE 8080

# ── Run the app ────────────────────────────────────────────────
# ENTRYPOINT = the command that runs when the container starts
# We use exec form (JSON array) not shell form ("java -jar app.jar")
# because exec form handles signals properly (clean shutdown)
#
# JVM flags explained:
# -XX:+UseContainerSupport  → tells JVM it's inside a container
#                             so it reads container memory limits
#                             not the host machine's full RAM
#
# -XX:MaxRAMPercentage=75.0 → JVM uses max 75% of container memory
#                             leaving 25% for the OS and other processes
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
