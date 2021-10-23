package org.p2p.wallet.history.ui.main

import androidx.fragment.app.Fragment

interface OnHeaderClickListener {
    fun navigateToFragment(fragment: Fragment)
    fun loadDailyChartData(tokenSymbol: String, days: Int)
    fun loadHourlyChartData(tokenSymbol: String, hours: Int)
}