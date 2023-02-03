package org.p2p.wallet.common.permissions.new

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher

class MultiplePermissionRequest private constructor(
    private val activityResultLauncher: ActivityResultLauncher<Array<String>>,
    private val permissions: Array<String>
) {

    fun request() {
        activityResultLauncher.launch(permissions)
    }

    class Builder {

        private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>

        fun setResultLauncher(resultLauncher: ActivityResultLauncher<Array<String>>): Builder {
            this.activityResultLauncher = resultLauncher
            return this
        }

        fun build(permissions: Array<String>): MultiplePermissionRequest {
            return MultiplePermissionRequest(activityResultLauncher, permissions)
        }
    }

    class ResultCallback(private val onPermissionStatusChanged: (result: Map<String, Boolean>) -> Unit) :
        ActivityResultCallback<Map<String, Boolean>> {
        override fun onActivityResult(result: Map<String, Boolean>) {
            onPermissionStatusChanged.invoke(result)
        }
    }
}
