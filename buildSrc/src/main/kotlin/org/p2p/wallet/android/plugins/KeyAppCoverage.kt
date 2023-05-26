package org.p2p.wallet.android.plugins

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class KeyAppCoverage : Plugin<Project> {

    abstract class KeyAppCoverageConfig {
        var reportHtml: Boolean = false
        var reportXml: Boolean = true
        var reportCsv: Boolean = false

        val excludes: MutableList<String> = arrayListOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "androidx/**/*.class",
            "**/androidx/**",
            "com/android/**/*.class",
        )

        val sourceDirs: MutableList<String> = arrayListOf(
            "src/main/java",
            "src/main/kotlin",
            "src/test/java",
            "src/test/kotlin",
            "src/androidTest/java",
            "src/androidTest/kotlin"
        )
        var builtClassesSubdir = "org.p2p.wallet"
        val enableForConfiguration: MutableList<String> = mutableListOf()

        internal var jacocoConfig: (JacocoPluginExtension.() -> Unit)? = null

        fun configureJacoco(apply: JacocoPluginExtension.() -> Unit) {
            this.jacocoConfig = apply
        }
    }

    override fun apply(target: Project) {
        val config = target.extensions.create("keyappCoverage", KeyAppCoverageConfig::class.java)

        target.pluginManager.apply("jacoco")

        target.afterEvaluate {
            if (config.jacocoConfig == null) {
                config.configureJacoco {
                    toolVersion = "0.8.8"
                    reportsDirectory.set(target.file("${target.buildDir}/reports/jacoco"))
                }
            }
            target.extensions.configure<JacocoPluginExtension>(
                JacocoPluginExtension::class.java,
                object : Action<JacocoPluginExtension> {
                    override fun execute(t: JacocoPluginExtension) {
                        t.apply(config.jacocoConfig!!)
                    }
                })
            generateTasks(target, config)
        }
    }

    private fun generateTasks(target: Project, config: KeyAppCoverageConfig) {
        config.enableForConfiguration.forEach { buildTypeName ->
            val sourceName = buildTypeName.capitalize()
            val testTaskName = "test${sourceName.capitalize()}UnitTest"

            println("Generating Jacoco coverage reports task on the $sourceName build.")
            target.tasks.register<JacocoReport>("${testTaskName}Coverage", JacocoReport::class.java) {
                group = "Verification"
                description = "Generate Jacoco coverage reports on the ${sourceName.capitalize()} build."
                dependsOn(testTaskName)

                // Set the coverage data source files
                val coverageExecutionData = target.fileTree("${project.buildDir}/jacoco")
                    .include("*.exec")
                executionData(coverageExecutionData)

                sourceDirectories.setFrom(config.sourceDirs)
                classDirectories.setFrom(
                    target.fileTree("${target.project.buildDir}/intermediates/javac/${buildTypeName}/classes/")
                        .setExcludes(config.excludes),
                    target.fileTree("${target.project.buildDir}/intermediates/classes/${buildTypeName}")
                        .setExcludes(config.excludes),
                    target.fileTree("${target.project.buildDir}/tmp/kotlin-classes/${buildTypeName}")
                        .setExcludes(config.excludes),
                )

                reports {
                    xml.required.set(config.reportXml)
                    html.required.set(config.reportHtml)
                    csv.required.set(config.reportCsv)
                }
            }
        }
    }

}
