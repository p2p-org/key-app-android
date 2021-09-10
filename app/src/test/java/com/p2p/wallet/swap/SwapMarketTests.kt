package com.p2p.wallet.swap

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.model.TokenVisibility
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.utils.DataInitializer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.p2p.solanaj.model.core.PublicKey
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.math.BigInteger

@RunWith(RobolectricTestRunner::class)
class SwapMarketTests {

    private val initializer = DataInitializer()

    private lateinit var serumSwapInteractor: SerumSwapInteractor

    private lateinit var userTokens: List<Token>

    private fun solNativeWallet(): Token = userTokens.first { it.tokenSymbol == "SOL" }

    private val SRM = PublicKey("SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt")
    private val USDT = PublicKey("Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB")
    private val USDC: PublicKey = PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v")

    @Before
    fun setUp() {
        initializer.initialize()
        serumSwapInteractor = initializer.getSwapInteractor()
        userTokens = initializer.getTokens()
    }

    @Test
    fun `test create vault owner`() {
        val vaultOwner = PublicKey.getVaultOwnerAndNonce(
            PublicKey("ByRys5tuUWDgL73G8JBAEfkdFf8JWBzPBDHsBVQ5vbQA")
        )

        assertEquals("GVV4ZT9pccwy9d17STafFDuiSqFbXuRTdvKQ1zJX6ttX", vaultOwner.toBase58())
    }

    @Test
    fun `test calculate exchange rate`() {
        val fair = 0.11853959222380275
        val expectedRate = BigInteger.valueOf(8375353L)
        val realRate = serumSwapInteractor.calculateExchangeRate(
            fair = fair,
            slippage = 0.005,
            toDecimal = 6
        )
        assertEquals(expectedRate, realRate)
    }

    @Test
    fun `test calculate network fees`() = runBlocking {
        val lps = BigInteger.valueOf(5000L)
        val mre = BigInteger.valueOf(2039280L)

        // from native sol to new usdt wallet
        val newUSDTWallet = Token(
            publicKey = "",
            tokenSymbol = "USDT",
            decimals = 6,
            mintAddress = USDT.toBase58(),
            tokenName = "USDT",
            logoUrl = null,
            price = BigDecimal.TEN,
            total = BigDecimal.TEN,
            color = 0,
            usdRate = BigDecimal.TEN,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = null,
            serumV3Usdt = null,
            isWrapped = false
        )

        val networkFees = serumSwapInteractor.calculateNetworkFee(
            fromWallet = solNativeWallet(),
            toWallet = newUSDTWallet,
            lamportsPerSignature = lps,
            minRentExemption = mre
        )

        assertEquals(BigInteger.valueOf(27451320L), networkFees)

        // from usdc to srm
        val usdcWallet = userTokens.first { it.mintAddress == USDC.toBase58() }

        val newSRMWallet = Token(
            publicKey = "",
            tokenSymbol = "SRM",
            decimals = 6,
            mintAddress = SRM.toBase58(),
            tokenName = "Serum",
            logoUrl = null,
            price = BigDecimal.TEN,
            total = BigDecimal.TEN,
            color = 0,
            usdRate = BigDecimal.TEN,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = null,
            serumV3Usdt = null,
            isWrapped = false
        )

        val networkFees2 = serumSwapInteractor.calculateNetworkFee(
            fromWallet = usdcWallet,
            toWallet = newSRMWallet,
            lamportsPerSignature = lps,
            minRentExemption = mre
        )

        assertEquals(BigInteger.valueOf(25407040), networkFees2)
    }
}