package org.p2p.wallet.android

import androidx.activity.result.ActivityResultLauncher

data class PermissionContract(
    val name: String,
    val resultLauncher: ActivityResultLauncher<String>
)
