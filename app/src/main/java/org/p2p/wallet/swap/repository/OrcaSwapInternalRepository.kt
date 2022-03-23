package org.p2p.wallet.swap.repository

import org.p2p.wallet.swap.model.orca.OrcaAquafarms
import org.p2p.wallet.swap.model.orca.OrcaPools
import org.p2p.wallet.swap.model.orca.OrcaProgramId
import org.p2p.wallet.swap.model.orca.OrcaTokens

interface OrcaSwapInternalRepository {
    suspend fun getTokens(): OrcaTokens
    suspend fun getAquafarms(): OrcaAquafarms
    suspend fun getPools(): OrcaPools
    suspend fun getProgramID(): OrcaProgramId
}
