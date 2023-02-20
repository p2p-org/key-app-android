// example './gradlew setVersionBuild -Pversion=10'
tasks.register("setVersionBuild", Exec::class) {
    doLast {
        val version = project.findProperty("version") as Long?

        if (version == null) {
            logger.error("Invalid version argument, usage: ./gradlew setVersionBuild -Pversion=10")
            return@doLast
        }

        val appVersionsClass = File("${rootProject.projectDir}/buildSrc/src/main/java/AppVersions.kt")
        if (!appVersionsClass.exists()) {
            logger.error("AppVersions.kt not found in buildSrc/src/main/java/")
            return@doLast
        }

        appVersionsClass.forEachLine { line ->
            if (line.contains("const val VERSION_BUILD")) {
                val newLine = "    const val VERSION_BUILD = $version\n"
                appVersionsClass.writeText(appVersionsClass.readText().replace(line, newLine))
                logger.quiet("VERSION_BUILD updated to $version")
            }
        }
    }
}
