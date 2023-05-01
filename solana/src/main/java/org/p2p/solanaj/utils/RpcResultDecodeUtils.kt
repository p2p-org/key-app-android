package org.p2p.solanaj.utils

import com.squareup.moshi.JsonAdapter
import okio.IOException

object MoshiUtils {

    fun canParse(message: String, adapter: JsonAdapter<*>): Boolean {
        return try {
            adapter.fromJson(message)
            true
        } catch (e: IOException) {
            false
        }
    }
}
