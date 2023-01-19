import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import java.io.File
import java.net.URL
import java.util.zip.ZipFile

open class LokaliseAituTask : DefaultTask(){
    private val defaultLangCode = "ru"
    private val languageCodes = setOf("ru", "kk", "en")
    private val archiveUrl = "http://webhook-receiver.btsdapps.net:8000/mes/android"

    @TaskAction
    fun execute(){
        val tempDirectory =
            project.rootProject.rootDir.absolutePath + File.separator + "build" + File.separator + "lokalise" + File.separator
        val targetDirectory =
            project.rootProject.rootDir.absolutePath + File.separator + "app" + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator
        File(tempDirectory).mkdirs()
        val zipFile = File(tempDirectory + "lokalise.zip")
        zipFile.writeBytes(URL(archiveUrl).readBytes())

        val files = unzip(zipFile)
        files.forEach { file ->
            var content = file.readText(Charsets.UTF_8)
            content = Regex("#text(\\d+?)").replace(content, "%$1\\$${'s'}")
            content = Regex("#num(\\d+?)").replace(content, "%$1\\$${'d'}")
            content = content.replace("#text", "%s")
            content = content.replace("#num", "%d")
            val targetFile = File(file.absolutePath.replace(tempDirectory, targetDirectory))
            targetFile.parentFile.mkdirs()
            targetFile.writeText(content, Charsets.UTF_8)
        }
    }

    private fun unzip(
        zipFile: File
    ): List<File> {
        val zip = ZipFile(zipFile)
        val files = mutableListOf<File>()
        for (zipEntry in zip.entries()) {
            if (zipEntry.isDirectory) continue
            val folderName = File(zipFile.parentFile, zipEntry.name).parentFile.name

            val langCode = if (folderName == "values") "ru" else folderName.replace("values-", "")
            if (!languageCodes.contains(langCode)) {
                println("Skipping language '$langCode' because it is missing in languageCodes")
                continue
            }

            val targetPath = if (langCode == defaultLangCode) {
                "values" + File.separator + "strings.xml"
            } else {
                "values-$langCode" + File.separator + "strings.xml"
            }

            val fOut = File(zipFile.parentFile, targetPath)
            fOut.parentFile.mkdirs()


            fOut.outputStream().use { out -> zip.getInputStream(zipEntry).copyTo(out) }

            println("Downloaded $langCode...")
            files.add(fOut)
        }
        zip.close()
        return files
    }
}

// Create a task using the task type
tasks.register<LokaliseAituTask>("lokaliseAitu"){
    group = "btsd"
    description = "Скачивает переводы с lokalise"
}
