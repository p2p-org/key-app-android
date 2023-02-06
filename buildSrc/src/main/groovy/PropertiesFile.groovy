import org.gradle.api.logging.Logger
import org.gradle.api.GradleScriptException

class PropertiesFile {
    private Properties properties = new Properties()
    private String propertiesFileName = "-"
    private Logger logger

    PropertiesFile(File propertiesFile, Logger logger) {
        this.logger = logger
        propertiesFileName = propertiesFile.name
        if (propertiesFile.canRead()) {
            properties.load(new FileInputStream(propertiesFile))
            logger.quiet("[${propertiesFile.name}] File ${propertiesFile.name} found, using file")
        } else {
            logger.quiet("[${propertiesFile.name}] No file ${propertiesFile.name} found, doing nothing")
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
