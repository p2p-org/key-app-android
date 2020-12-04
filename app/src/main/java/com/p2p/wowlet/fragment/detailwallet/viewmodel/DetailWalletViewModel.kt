package com.p2p.wowlet.fragment.detailwallet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.DetailActivityInteractor
import com.wowlet.entities.local.ActivityItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailWalletViewModel(val detailActivityInteractor: DetailActivityInteractor) :
    BaseViewModel() {

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

    private val _getActivityData by lazy { MutableLiveData<List<ActivityItem>>() }
    val getActivityData: LiveData<List<ActivityItem>> get() = _getActivityData
    private val _getChartData by lazy { MutableLiveData<List<Entry>>() }
    val getChartData: LiveData<List<Entry>> get() = _getChartData

    init {
        _getChartData.value = chartList
    }

    fun getActivityList(publicKey: String, icon: String, tokenName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val detailList = detailActivityInteractor.getActivityList(publicKey,icon,tokenName)
            withContext(Dispatchers.Main){
                _getActivityData.value=detailList
            }
        }
    }

    fun navigateUp() {
        _command.value =
            Command.NavigateUpViewCommand(R.id.action_navigation_receive_to_navigation_dashboard)
    }

    fun goToQrScanner() {
        _command.value =
            Command.NavigateScannerViewCommand(R.id.action_navigation_detail_wallet_to_navigation_scanner)
    }
    fun openTransactionDialog(itemActivity:ActivityItem) {
        _command.value =
            Command.OpenTransactionDialogViewCommand(itemActivity)
    }

}