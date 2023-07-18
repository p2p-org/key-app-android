package org.p2p.wallet.home.ui.wallet

import assertk.all
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.Constants
import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.fromLamports
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.wallet.mapper.WalletMapper
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.utils.assertThat

class WalletPresenterMapperTest {

    @Test
    fun `GIVEN non-null striga banner WHEN mapStrigaKycBanner THEN check it has correct model`() {
        val mapper = WalletMapper()
        val result = mapper.buildCellItems {
            mapStrigaKycBanner(StrigaKycStatusBanner.IDENTIFY)
        }

        result.assertThat().hasSize(1)
        result.first().assertThat()
            .isInstanceOf(AnyCellItem::class.java)
            .isInstanceOf(StrigaBanner::class.java)
            .all {
                prop(StrigaBanner::isLoading).isEqualTo(false)
                prop(StrigaBanner::status).isEqualTo(StrigaKycStatusBanner.IDENTIFY)
            }
    }

    @Test
    fun `GIVEN null striga banner WHEN mapStrigaKycBanner THEN check it did not map`() {
        val mapper = WalletMapper()
        val result = mapper.buildCellItems {
            mapStrigaKycBanner(null)
        }

        result.assertThat().hasSize(0)
    }

    @Test
    fun `GIVEN striga onramp token WHEN mapStrigaOnRampTokens THEN check is has correct model`() {
        val tokenMint = Base58String("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")
        val totalAmount = BigInteger("100") // 1 buck
        val totalFee = BigInteger("10") // 0.1 buck
        val expectedAmount = totalAmount.fromLamports(STRIGA_FIAT_DECIMALS) - totalFee.fromLamports(STRIGA_FIAT_DECIMALS)
        val onRampToken = StrigaOnRampToken(
            totalAmount = totalAmount,
            fees = StrigaOnchainWithdrawalFees(
                totalFee = totalFee,
                networkFee = BigInteger.ONE,
                ourFee = BigInteger.ONE,
                theirFee = BigInteger.ONE,
                feeCurrency = StrigaNetworkCurrency.USDC,
                gasLimit = BigInteger.ONE,
                gasPrice = BigDecimal.ONE,
            ),
            tokenDetails = Token.Active(
                publicKey = "publicKey",
                tokenSymbol = "USDC",
                decimals = 6,
                mintAddress = tokenMint.base58Value,
                tokenName = "USDC",
                iconUrl = "iconUrl",
                isWrapped = false,
                rate = BigDecimal.ONE,
                currency = Constants.USD_READABLE_SYMBOL,
                totalInUsd = BigDecimal("2.0"),
                total = BigDecimal("1.0"),
                visibility = TokenVisibility.SHOWN,
            ),
            walletId = StrigaWalletId("walletId"),
            accountId = StrigaAccountId("accountId"),
        )
        val mapper = WalletMapper()
        val result = mapper.buildCellItems {
            mapStrigaOnRampTokens(listOf(onRampToken))
        }

        result.assertThat().hasSize(1)
        result.first().assertThat()
            .isInstanceOf(StrigaOnRampCellModel::class.java)
            .all {
                prop(StrigaOnRampCellModel::amountAvailable).isEqualTo(expectedAmount)
                prop(StrigaOnRampCellModel::tokenMintAddress).isEqualTo(tokenMint)
                prop(StrigaOnRampCellModel::tokenSymbol).isEqualTo("USDC")
                prop(StrigaOnRampCellModel::tokenIcon).isEqualTo("iconUrl")
                prop(StrigaOnRampCellModel::isLoading).isEqualTo(false)
                prop(StrigaOnRampCellModel::payload).isEqualTo(onRampToken)
            }
    }
}
