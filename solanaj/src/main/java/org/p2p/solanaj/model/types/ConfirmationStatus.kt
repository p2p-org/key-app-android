package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

enum class ConfirmationStatus {
    @SerializedName("finalized")
    FINALIZED,
    @SerializedName("confirmed")
    CONFIRMED,
    @SerializedName("processed")
    PROCESSED;
}
