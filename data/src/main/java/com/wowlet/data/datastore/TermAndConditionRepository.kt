package com.wowlet.data.datastore

import com.wowlet.entities.local.UserSecretData

interface TermAndConditionRepository {
    suspend  fun initNewUser(): List<UserSecretData>
}