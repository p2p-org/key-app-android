package com.p2p.wallet.swap.model

import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance
import java.math.BigInteger

data class SwapRequest(
    val pool: Pool.PoolInfo,
    val slippage: Double,
    val amount: BigInteger,
    val balanceA: TokenAccountBalance,
    val balanceB: TokenAccountBalance
)