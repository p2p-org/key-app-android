package org.p2p.wallet.settings

import android.os.Build

object DeviceInfoHelper {

    fun getCurrentDeviceName(): String {
        return Build.MANUFACTURER + ' ' + Build.MODEL
    }
}
