import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"


java {
    sourceCompatibility = JavaVersion.VERSION_11
}

application{
    mainClass.set("com.example.module_a.ModuleAApplication")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
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

var arangodbVersion = "7.1.0"

dependencies {
    shadow("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // https://mvnrepository.com/artifact/com.arangodb/arangodb-java-driver
    implementation("com.arangodb:arangodb-java-driver:${arangodbVersion}")
    implementation("com.arangodb:core:${arangodbVersion}")
    // https://mvnrepository.com/artifact/com.arangodb/jackson-dataformat-velocypack
    implementation("com.arangodb:jackson-dataformat-velocypack:4.1.0")
}

tasks.withType<Jar> {
    enabled = true
    manifest {
        attributes["PluginId"] = "Module_A"
        attributes["pluginVersion"] = project.version
        attributes["pluginDescription"] = "This is a plugin description"
        attributes["Main-Class"] = application.mainClass
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    isEnableRelocation = true
    relocationPrefix = rootProject.name + ".shadow"
//    exclude("")
    minimize()
//    dependencies{
//        exclude(dependency("xxx.xxx"))
//    }
}
