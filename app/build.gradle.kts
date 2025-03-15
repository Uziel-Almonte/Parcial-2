/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
    
    // Add Javalin dependencies
    implementation("io.javalin:javalin:6.4.0")
    implementation("io.javalin:javalin-bundle:6.4.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    
    // Add H2 Database
    implementation("com.h2database:h2:2.2.224")
    
    // JPA and Hibernate
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    
    // Add Hibernate (ORM)
    implementation("org.hibernate:hibernate-core:6.4.4.Final")

    implementation("org.mindrot:jbcrypt:0.4")
    
    // Add JSON support
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    
    // Add template engine
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")

    // Add javax.servlet dependency
    implementation("javax.servlet:javax.servlet-api:4.0.1")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("parcial.App")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
