package com.p2p.wallet.detailsaving.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wallet.deprecated.viewmodel.BaseViewModel
import com.p2p.wallet.dashboard.model.local.ActivityItem

class DetailSavingViewModel : BaseViewModel() {

    private val listActivityData = mutableListOf<ActivityItem>()

    private val _getActivityData by lazy { MutableLiveData<MutableList<ActivityItem>>() }
    val getActivityData: LiveData<MutableList<ActivityItem>> get() = _getActivityData

    init {
        _getActivityData.value = listActivityData
    }
}