package com.p2p.wowlet.fragment.dashboard.dialog.backupingkey.viewmodel

import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.entities.local.SecretKeyItem

class BackingUpKeyViewModel : BaseViewModel() {

    private val listSortData = mutableListOf(
        SecretKeyItem(1, "1.phrase", false),
        SecretKeyItem(2, "2.phrase", false),
        SecretKeyItem(3, "3.phrase", false),
        SecretKeyItem(4, "4.phrase", false),
        SecretKeyItem(5, "5.phrase", false),
        SecretKeyItem(6, "6.phrase", false)
    )

    private val listRandomData = mutableListOf(
        SecretKeyItem(7, "7.phrase", false),
        SecretKeyItem(12, "12.phrase", false),
        SecretKeyItem(8, "8.phrase", false),
        SecretKeyItem(10, "10.phrase", false),
        SecretKeyItem(9, "9.phrase", false),
        SecretKeyItem(11, "11.phrase", false)
    )
}