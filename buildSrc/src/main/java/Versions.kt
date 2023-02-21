import AppVersions.VERSION_HOTFIX
import AppVersions.VERSION_MAJOR
import AppVersions.VERSION_MINOR
import AppVersions.VERSION_PATCH
import AppVersions.VERSION_BUILD

object Versions {
    // Android versions
    const val sdkMinVersion = 26
    const val sdkTargetVersion = 33
    const val sdkCompileVersion = 33

    const val VERSION_NAME = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_HOTFIX"

    const val VERSION_CODE = 1_000_00 * (VERSION_MAJOR) +
        1_000 * (VERSION_MINOR) +
        1_0 * (VERSION_HOTFIX) +
        (VERSION_PATCH)

    const val CURRENT_APP_NAME = "key-app-$VERSION_NAME.$VERSION_BUILD"

    const val kotlinVersion = "1.7.20"
}
