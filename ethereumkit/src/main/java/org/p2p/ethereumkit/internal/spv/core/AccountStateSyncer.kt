package org.p2p.ethereumkit.internal.spv.core

import org.p2p.ethereumkit.internal.core.ISpvStorage
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.spv.models.AccountStateSpv
import org.p2p.ethereumkit.internal.spv.models.BlockHeader
import org.p2p.ethereumkit.internal.spv.net.handlers.AccountStateTaskHandler
import org.p2p.ethereumkit.internal.spv.net.tasks.AccountStateTask

class AccountStateSyncer(private val storage: ISpvStorage,
                         private val address: EthAddress
) : AccountStateTaskHandler.Listener {

    interface Listener {
        fun onUpdate(accountState: AccountStateSpv)
    }

    var listener: Listener? = null

    fun sync(taskPerformer: ITaskPerformer, blockHeader: BlockHeader) {
        taskPerformer.add(AccountStateTask(address, blockHeader))
    }

    override fun didReceive(accountState: AccountStateSpv, address: EthAddress, blockHeader: BlockHeader) {
        storage.saveAccountSate(accountState)
        listener?.onUpdate(accountState)
    }

}
