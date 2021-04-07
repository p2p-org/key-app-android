package com.p2p.wowlet.fragment.detailsaving.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.p2p.wowlet.entities.local.ActivityItem

class DetailSavingViewModel : BaseViewModel() {

    private val listActivityData = mutableListOf<ActivityItem>()

    private val _getActivityData by lazy { MutableLiveData<MutableList<ActivityItem>>() }
    val getActivityData: LiveData<MutableList<ActivityItem>> get() = _getActivityData

    init {
        _getActivityData.value = listActivityData
    }

}