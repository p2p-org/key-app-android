package org.p2p.wallet.home.ui.wallet.mapper.model

import org.p2p.wallet.home.model.HomeScreenBanner

data class StrigaBanner(
    val isLoading: Boolean,
    val status: StrigaKycStatusBanner
) : HomeScreenBanner()
