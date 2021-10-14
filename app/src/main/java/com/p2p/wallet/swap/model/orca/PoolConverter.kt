package com.p2p.wallet.swap.model.orca

import org.p2p.solanaj.kits.Pool

object PoolConverter {

    fun fromNetwork(pool: Pool.PoolInfo): OrcaPool =
        OrcaPool(
            address = pool.address,
            tokenPool = pool.tokenPool,
            feeAccount = pool.feeAccount,
            sourceMint = pool.mintA,
            destinationMint = pool.mintB,
            tokenAccountA = pool.tokenAccountA,
            tokenAccountB = pool.tokenAccountB,
            swapProgramId = pool.swapProgramId,
            tradeFeeDenominator = pool.tradeFeeDenominator,
            tradeFeeNumerator = pool.tradeFeeNumerator
        )
}