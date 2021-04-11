package com.p2p.wowlet.datastore

import com.p2p.wowlet.entities.local.UserSecretData

interface TermAndConditionRepository {
    suspend fun initNewUser(): List<UserSecretData>
}