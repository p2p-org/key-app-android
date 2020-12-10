package com.wowlet.domain.interactors

import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem

interface DetailActivityInteractor {
    suspend fun getActivityList(publicKey: String, icon: String, tokenName: String): List<ActivityItem>
    suspend  fun blockTime(slot:Long): Result<String>
}