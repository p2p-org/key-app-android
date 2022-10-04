package org.p2p.wallet.solend.ui.deposits.adapter

import org.p2p.wallet.solend.model.SolendDepositToken

interface TokenDepositItemClickListener {
    fun onAddMoreClicked(token: SolendDepositToken)
    fun onWithdrawClicked(token: SolendDepositToken)
}
