plugins {
    java
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

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

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
