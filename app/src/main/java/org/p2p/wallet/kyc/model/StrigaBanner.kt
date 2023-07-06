package org.p2p.wallet.kyc.model

import org.p2p.wallet.home.model.HomeScreenBanner

data class StrigaBanner(
    val isLoading: Boolean,
    val status: StrigaKycStatusBanner
) : HomeScreenBanner()
