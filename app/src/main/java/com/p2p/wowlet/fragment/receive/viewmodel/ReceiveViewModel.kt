package com.p2p.wowlet.fragment.receive.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.entities.local.ActivityItem
import java.util.ArrayList

class ReceiveViewModel : BaseViewModel() {

    val chartList = mutableListOf(
        Entry(0f, 0f),
        Entry(1f, 10f),
        Entry(2f, 7f),
        Entry(3f, 13f),
        Entry(5f, 11f),
        Entry(5f, 20f),
        Entry(6f, 18f),
        Entry(7f, 21f),
        Entry(8f, 18f),
        Entry(9f, 21f),
        Entry(10f, 30f),
        Entry(11f, 17f),
        Entry(12f, 25f),
        Entry(13f, 27f)
    )

    private val listActivityData = mutableListOf(
        ActivityItem("Receive Tokens", "11 oct 2020", "+ 44,51 US$", "0,00344 Tkns"),
        ActivityItem("Send Tokens", "11 oct 2020", "- 44,51 US$", "0,00344 Tkns"),
        ActivityItem("Receive Tokens", "11 oct 2020", "+ 44,51 US$", "0,00344 Tkns"),
        ActivityItem("Receive Tokens", "11 oct 2020", "+ 44,51 US$", "0,00344 Tkns"),
        ActivityItem("Send Tokens", "11 oct 2020", "- 44,51 US$", "0,00344 Tkns"),
        ActivityItem("Receive Tokens", "11 oct 2020", "+ 44,51 US$", "0,00344 Tkns"),
        ActivityItem("Receive Tokens", "11 oct 2020", "+ 44,51 US$", "0,00344 Tkns")
    )

    private val _getActivityData by lazy { MutableLiveData<MutableList<ActivityItem>>() }
    val getActivityData: LiveData<MutableList<ActivityItem>> get() = _getActivityData
    private val _getChartData by lazy { MutableLiveData<List<Entry>>() }
    val getChartData: LiveData<List<Entry>> get() = _getChartData

    init {
        _getActivityData.value = listActivityData
        _getChartData.value = chartList
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_receive_to_navigation_dashboard)
    }
}