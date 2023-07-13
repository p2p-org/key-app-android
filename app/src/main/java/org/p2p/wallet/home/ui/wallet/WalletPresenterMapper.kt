package org.p2p.wallet.home.ui.wallet

import org.p2p.core.crypto.toBase58Instance
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.kyc.model.StrigaKycUiBannerMapper
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken

class WalletPresenterMapper(
    private val strigaKycUiBannerMapper: StrigaKycUiBannerMapper
) {

    fun buildCellItems(mapper: WalletPresenterMapper.Builder.() -> Unit): List<AnyCellItem> {
        return Builder().apply(mapper).build()
    }

    inner class Builder internal constructor() {
        private val cellItems = mutableListOf<AnyCellItem>()

        fun mapStrigaKycBanner(banner: StrigaKycStatusBanner?): Builder {
            banner?.let {
                cellItems += strigaKycUiBannerMapper.mapToBanner(isLoading = false, status = banner)
            }
            return this
        }

        fun mapStrigaOnRampTokens(strigaOnRampTokens: List<StrigaOnRampToken>): Builder {
            cellItems += strigaOnRampTokens.map {
                val mintAddress = it.tokenDetails.mintAddress.toBase58Instance()
                StrigaOnRampCellModel(
                    amountAvailable = it.claimableAmount,
                    tokenName = it.tokenDetails.tokenName,
                    tokenMintAddress = mintAddress,
                    tokenSymbol = it.tokenDetails.tokenSymbol,
                    tokenIcon = it.tokenDetails.iconUrl.orEmpty(),
                    isLoading = false,
                    payload = it
                )
            }
            return this
        }

        fun build(): List<AnyCellItem> = cellItems
    }
}
