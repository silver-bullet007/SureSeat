# ===== Stage 1: Build =====
# Use an image with Maven + JDK 21 pre-installed, just for compiling.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only the POM first and download dependencies as a separate layer.
# Docker caches each instruction as a "layer" - as long as pom.xml hasn't
# changed, Docker reuses this cached dependency-download step on future
# builds, instead of re-downloading everything every time you rebuild.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the actual source code and build the real JAR.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Stage 2: Run =====
# A much smaller image, with just a JRE (not a full JDK) - we don't need
# compilers or build tools to just RUN an already-built application.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy ONLY the built JAR from the previous stage - none of the source
# code, Maven cache, or build tools make it into this final image.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]