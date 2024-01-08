package org.p2p.wallet.settings.model

import com.google.gson.annotations.SerializedName
import org.p2p.core.utils.emptyString

class UserCountrySettingsEntity(
    @SerializedName("name")
    val name: String,
    @SerializedName("code_alpha_2")
    val codeAlpha2: String,
    @SerializedName("code_alpha_3")
    val codeAlpha3: String,
    @SerializedName("phone_code")
    val phoneCode: String,
    @SerializedName("phone_mask")
    var phoneMask: String = emptyString(),
    @SerializedName("flag_emoji")
    val flagEmoji: String,
)
