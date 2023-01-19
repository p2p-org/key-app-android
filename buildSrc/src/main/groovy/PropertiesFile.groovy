import org.gradle.api.logging.Logger
import org.gradle.api.GradleScriptException

class PropertiesFile {
    private Properties properties = new Properties()
    def String propertiesFileName = "-"
    private Logger logger
    def Boolean isFileExists

    PropertiesFile(File propertiesFile, Logger logger) {
        this.logger = logger
        propertiesFileName = propertiesFile.name
        isFileExists = propertiesFile.canRead()
        if (propertiesFile.canRead()) {
            properties.load(new FileInputStream(propertiesFile))
            logger.quiet("[${propertiesFile.name}] File ${propertiesFile.name} found, using file")
        } else {
            logger.quiet("[${propertiesFile.name}] No file ${propertiesFile.name} found, doing nothing")
        }
    }

    String getOrDefault(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            properties.getProperty(key)
        } else {
            logger.quiet("[$propertiesFileName] No $key found, using default: $defaultValue")
            defaultValue
        }
    }

    String getOrThrow(String key) {
        if (properties.containsKey(key)) {
            properties.getProperty(key)
        } else {
            throw new GradleScriptException(
                    "[$propertiesFileName] No $key found, but it's needed! Available keys: ${properties.keySet()}",
                    null
            )
        }
    }

}