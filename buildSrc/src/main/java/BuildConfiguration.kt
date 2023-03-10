import java.io.File

object BuildConfiguration {

    const val FEATURE_BUILD_TESTERS_GROUP = "wallet.feature.test"
    const val RELEASE_BUILD_TESTERS_GROUP = "wallet.release.test"
    private const val CHANGELOG_FILE = "CHANGELOG.txt"

    @JvmStatic
    fun getChangelogFilePath(projectRootDir: String) : String {
        return "$projectRootDir${File.separatorChar}$CHANGELOG_FILE"
    }
}
