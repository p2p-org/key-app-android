package org.p2p.wallet.referral.repository

interface ReferralRepository {
    suspend fun setReferent(referentUsernameOrPublicKey: String)
}
