package org.p2p.wallet.swap.model.orca

import org.p2p.wallet.swap.api.OrcaInfoResponse
import org.p2p.wallet.swap.api.OrcaPoolResponse
import org.p2p.wallet.swap.api.OrcaTokensResponse
import org.p2p.wallet.swap.api.ProgramIdResponse
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

object OrcaConverter {

    fun fromNetwork(response: OrcaInfoResponse): OrcaConfigs =
        OrcaConfigs(
            tokens = response.config.tokens.mapValues { fromNetwork(it.value) } as OrcaTokens,
            pools = response.config.pools.mapValues { fromNetwork(it.value) } as OrcaPools,
            programId = fromNetwork(response.config.programIds)
        )

    private fun fromNetwork(response: OrcaTokensResponse): OrcaToken =
        OrcaToken(
            mint = response.mint,
            name = response.name,
            decimals = response.decimals,
            fetchPrice = response.fetchPrice,
        )

    private fun fromNetwork(response: OrcaPoolResponse): OrcaPool =
        OrcaPool(
            account = response.account.toPublicKey(),
            authority = response.authority.toPublicKey(),
            nonce = response.nonce,
            poolTokenMint = response.poolTokenMint.toPublicKey(),
            tokenAccountA = response.tokenAccountA.toPublicKey(),
            tokenAccountB = response.tokenAccountB.toPublicKey(),
            feeAccount = response.feeAccount.toPublicKey(),
            hostFeeAccount = response.hostFeeAccount?.toPublicKey(),
            feeNumerator = BigInteger.valueOf(response.feeNumerator),
            feeDenominator = BigInteger.valueOf(response.feeDenominator),
            ownerTradeFeeNumerator = BigInteger.valueOf(response.ownerTradeFeeNumerator),
            ownerTradeFeeDenominator = BigInteger.valueOf(response.ownerTradeFeeDenominator),
            ownerWithdrawFeeNumerator = BigInteger.valueOf(response.ownerWithdrawFeeNumerator),
            ownerWithdrawFeeDenominator = BigInteger.valueOf(response.ownerWithdrawFeeDenominator),
            hostFeeNumerator = BigInteger.valueOf(response.hostFeeNumerator),
            hostFeeDenominator = BigInteger.valueOf(response.hostFeeDenominator),
            tokenAName = response.tokenAName,
            tokenBName = response.tokenBName,
            curveType = response.curveType,
            amp = response.amp?.let { BigInteger.valueOf(it) },
            programVersion = response.programVersion,
            deprecated = response.deprecated == true,
        )

    private fun fromNetwork(response: ProgramIdResponse): OrcaProgramId =
        OrcaProgramId(
            serumTokenSwap = response.serumTokenSwap,
            tokenSwapV2 = response.tokenSwapV2,
            tokenSwap = response.tokenSwap,
            token = response.token,
            aquafarm = response.aquafarm
        )
}
