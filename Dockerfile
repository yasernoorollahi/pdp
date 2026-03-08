# اضافه شد: Multi-stage Dockerfile برای production
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# کپی فایل‌های Maven wrapper و pom.xml اول (برای cache بهتر)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# دانلود dependencies (این layer cache میشه اگه pom.xml تغییر نکنه)
RUN ./mvnw dependency:go-offline -q

# کپی source code
COPY src ./src

# Build (بدون test چون در CI جداگانه run میشه)
RUN ./mvnw package -DskipTests -q

# Extract layers برای بهتر شدن startup time
RUN java -Djarmode=layertools -jar target/*.jar extract

# =============================================
# Stage 2: Runtime - image خیلی کوچک‌تر
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: اجرا نکردن با root
RUN addgroup -S pdp && adduser -S pdp -G pdp
USER pdp

WORKDIR /app

# کپی layers از مرحله قبل (ترتیب مهمه - کمتر تغییر میکنه اول)
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# اضافه شد: Graceful shutdown + JVM flags بهینه برای container
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
