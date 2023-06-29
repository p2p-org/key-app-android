apply {
    from("${project.rootDir}/.scripts/dependencies.gradle")
    from("${project.rootDir}/.scripts/changelog.gradle")
}

buildscript {

    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.google.com/")
        maven(url = "https://jitpack.io/")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.buildGradleVersion}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinGradlePluginVersion}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.6")
        classpath("com.google.gms:google-services:4.3.15")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io/")
        maven(url = "https://maven.google.com/")
        maven(url = "https://maven.lokalise.com/")
        maven(url = "https://maven.sumsub.com/repository/maven-public/")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register<Copy>("installGitHook") {
    val useGitHooks = extra["use_git_hooks"]?.toString()?.toBoolean() ?: false

    logger.quiet("(GIT_HOOKS): use_git_hooks=$useGitHooks")
    if (useGitHooks) {
        from(rootProject.file("scripts/pre-commit"))
        into(rootProject.file(".git/hooks"))
        // Set file mode to executable
        rootProject.file(".git/hooks/pre-commit").setExecutable(true)
    } else {
        rootProject.file(".git/hooks/pre-commit").delete()
    }
}

gradle.projectsEvaluated {
    tasks.withType<JavaCompile> {
        dependsOn("installGitHook")
    }
}
