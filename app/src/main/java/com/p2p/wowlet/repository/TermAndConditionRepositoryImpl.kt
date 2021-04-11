package com.p2p.wowlet.repository

import com.p2p.wowlet.datastore.TermAndConditionRepository
import com.p2p.wowlet.entities.local.UserSecretData

class TermAndConditionRepositoryImpl() : TermAndConditionRepository {
    override suspend fun initNewUser(): List<UserSecretData> {
        TODO("Not yet implemented")
    }
}