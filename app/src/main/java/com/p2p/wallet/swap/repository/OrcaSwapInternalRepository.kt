package com.p2p.wallet.swap.repository

import com.p2p.wallet.swap.model.orca.OrcaAquafarms
import com.p2p.wallet.swap.model.orca.OrcaPools
import com.p2p.wallet.swap.model.orca.OrcaProgramId
import com.p2p.wallet.swap.model.orca.OrcaTokens

interface OrcaSwapInternalRepository {
    suspend fun getTokens(): OrcaTokens
    suspend fun getAquafarms(): OrcaAquafarms
    suspend fun getPools(): OrcaPools
    suspend fun getProgramID(): OrcaProgramId
}