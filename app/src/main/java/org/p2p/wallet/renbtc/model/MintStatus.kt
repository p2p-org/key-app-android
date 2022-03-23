package org.p2p.wallet.renbtc.model

enum class MintStatus(val stringValue: String) {
    DONE("done"),
    EXECUTING("executing"),
    CONFIRMED("confirmed");

    companion object {
        fun parse(status: String): MintStatus = when (status) {
            DONE.stringValue -> DONE
            EXECUTING.stringValue -> EXECUTING
            CONFIRMED.stringValue -> CONFIRMED
            else -> CONFIRMED // todo: add other statuses
        }
    }
}
