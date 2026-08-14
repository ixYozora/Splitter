# ---------- build stage ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

# Wrapper and build scripts first: Docker caches this layer until the build
# files themselves change, so dependencies are not re-downloaded on every
# source edit.
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN ./gradlew --no-daemon --console=plain dependencies

COPY src src
RUN ./gradlew --no-daemon --console=plain bootJar

# ---------- runtime stage ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as an unprivileged user rather than root.
RUN useradd --system --create-home --uid 1001 spring
USER spring

# bootJar produces exactly one artifact in build/libs.
COPY --from=build /build/build/libs/*.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
