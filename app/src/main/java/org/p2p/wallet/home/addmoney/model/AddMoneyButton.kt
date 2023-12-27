package org.p2p.wallet.home.addmoney.model

data class AddMoneyButton(
    val type: AddMoneyButtonType,
    val isLoading: Boolean = false,
)
