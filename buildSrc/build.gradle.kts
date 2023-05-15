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

gradlePlugin {
    plugins {
        create("keyappCoverage") {
            id = "${group}.coverage"
            implementationClass = "${group}.plugins.KeyAppCoverage"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.3.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    implementation("com.google.firebase:firebase-appdistribution-gradle:3.2.0")
    implementation("com.google.firebase:perf-plugin:1.4.2")
}
