package com.wowlet.domain.interactors

import com.github.mikephil.charting.data.Entry
import com.wowlet.entities.Result
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.ChartListItem

interface DetailActivityInteractor {
    suspend fun getActivityList(publicKey: String, icon: String, tokenName: String): List<ActivityItem>
    suspend  fun blockTime(slot:Long): Result<String>
    suspend  fun getChatListByDate(tokenSymbol:String, startTime: Long, endTime: Long): Result<List<Entry>>
    suspend  fun getChatList(tokenSymbol:String): Result<List<Entry>>
}
