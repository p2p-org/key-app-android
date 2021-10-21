package com.p2p.wallet.swap.model.orca

import com.p2p.wallet.swap.api.OrcaAquafarmResponse
import com.p2p.wallet.swap.api.OrcaPoolResponse
import com.p2p.wallet.swap.api.OrcaTokensResponse
import com.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

object OrcaConverter {

    fun fromNetwork(response: OrcaTokensResponse): OrcaToken =
        OrcaToken(
            mint = response.mint,
            name = response.name,
            decimals = response.decimals,
            fetchPrice = response.fetchPrice,
        )

    fun fromNetwork(response: OrcaAquafarmResponse): OrcaAquafarm =
        OrcaAquafarm(
            account = response.account,
            nonce = response.nonce,
            tokenProgramId = response.tokenProgramId,
            emissionsAuthority = response.emissionsAuthority,
            removeRewardsAuthority = response.removeRewardsAuthority,
            baseTokenMint = response.baseTokenMint,
            baseTokenVault = response.baseTokenVault,
            rewardTokenMint = response.rewardTokenMint,
            rewardTokenVault = response.rewardTokenVault,
            farmTokenMint = response.farmTokenMint
        )

    fun fromNetwork(response: OrcaPoolResponse): OrcaPool =
        OrcaPool(
            account = response.account.toPublicKey(),
            authority = response.authority.toPublicKey(),
            nonce = response.nonce,
            poolTokenMint = response.poolTokenMint.toPublicKey(),
            tokenAccountA = response.tokenAccountA.toPublicKey(),
            tokenAccountB = response.tokenAccountB.toPublicKey(),
            feeAccount = response.feeAccount.toPublicKey(),
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
            deprecated = response.deprecated == true,
        )
}