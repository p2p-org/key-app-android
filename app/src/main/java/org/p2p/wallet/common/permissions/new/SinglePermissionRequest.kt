package org.p2p.wallet.common.permissions.new

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher

class SinglePermissionRequest private constructor(
    private val activityResultLauncher: ActivityResultLauncher<String>,
    private val permission: String
) {

    fun request() {
        activityResultLauncher.launch(permission)
    }

    class Builder {

        private lateinit var activityResultLauncher: ActivityResultLauncher<String>

        fun setResultLauncher(resultLauncher: ActivityResultLauncher<String>): Builder {
            this.activityResultLauncher = resultLauncher
            return this
        }

        fun build(permission: String): SinglePermissionRequest {
            return SinglePermissionRequest(activityResultLauncher, permission)
        }
    }

    class ResultCallback(private val onPermissionStatusChanged: (result: Boolean) -> Unit) :
        ActivityResultCallback<Boolean> {
        override fun onActivityResult(result: Boolean) {
            onPermissionStatusChanged.invoke(result)
        }
    }
}
