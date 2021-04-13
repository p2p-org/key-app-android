package com.p2p.wallet.common.crypto

import android.util.Base64

object Base64Utils {
    fun encode(data: String) = String(Base64.encode(data.toByteArray(), Base64.DEFAULT or Base64.NO_WRAP))
    fun decode(data: String) = String(Base64.decode(data, Base64.DEFAULT or Base64.NO_WRAP))
}