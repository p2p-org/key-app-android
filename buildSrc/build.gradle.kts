import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.p2p.wallet.android"

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.3.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    implementation("com.google.firebase:firebase-appdistribution-gradle:3.2.0")
}
