plugins {
    kotlin("jvm") version "1.9.22"
    id("com.gradleup.shadow") version "9.2.2"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.4")
    implementation(kotlin("stdlib"))
}

tasks {
    shadowJar {
        archiveBaseName.set("kotlin-lambda")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}
