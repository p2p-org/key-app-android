package com.p2p.wallet.renBTC.model

enum class MintStatus(val stringValue: String) {
    DONE("done"),
    CONFIRMED("confirmed");

    companion object {
        fun parse(status: String): MintStatus = when (status) {
            DONE.stringValue -> DONE
            CONFIRMED.stringValue -> CONFIRMED
            else -> CONFIRMED // todo: add other statuses
        }
    }
}