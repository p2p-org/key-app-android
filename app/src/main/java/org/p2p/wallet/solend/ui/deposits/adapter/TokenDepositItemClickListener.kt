package org.p2p.wallet.solend.ui.deposits.adapter

import org.p2p.wallet.solend.model.SolendDepositToken

interface TokenDepositItemClickListener {
    fun onAddMoreClicked(token: SolendDepositToken.Active)
    fun onWithdrawClicked(token: SolendDepositToken.Active)
}
