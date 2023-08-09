package org.p2p.wallet.striga.signup.presetpicker

import org.p2p.wallet.auth.model.CountryCode

enum class SelectItemProviderType {
    SELECT_COUNTRY
}

sealed class SelectItemViewType(
    val selectedItemId: String?,
    val providerType: SelectItemProviderType
) {
    class SelectCountry(selectedCountry: CountryCode? = null) : SelectItemViewType(
        selectedItemId = selectedCountry?.nameCodeAlpha3,
        providerType = SelectItemProviderType.SELECT_COUNTRY
    )
}
