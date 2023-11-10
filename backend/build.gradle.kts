plugins {
    java
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.register("prepareKotlinBuildScriptModel"){}

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/public") }
    maven { setUrl("https://maven.aliyun.com/repository/spring") }
    maven { setUrl("https://maven.aliyun.com/repository/mapr-public") }
    maven { setUrl("https://maven.aliyun.com/repository/spring-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
    mavenCentral()
}

val openAPIVersion = "1.6.11"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springdoc:springdoc-openapi-ui:${openAPIVersion}")
    implementation("cn.hutool:hutool-core:5.8.22")


    runtimeOnly("com.h2database:h2")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
