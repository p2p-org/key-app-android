package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

enum class ConfirmationStatus(val value: String) {
    @SerializedName("finalized")
    FINALIZED("finalized"),
    @SerializedName("confirmed")
    CONFIRMED("confirmed"),
    @SerializedName("processed")
    PROCESSED("processed");
}
