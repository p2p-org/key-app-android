package org.p2p.wallet.sdk

import androidx.annotation.Keep

typealias jboolean = Boolean
typealias JChar = Char
typealias JByte = Byte
typealias JShort = Short
typealias JInt = Int
typealias jlong = Long
typealias JFloat = Float
typealias JDouble = Double
typealias JString = String

typealias u64 = ULong
typealias u32 = UInt

@Keep
class SolendSdk {
    companion object {
        init {
            System.loadLibrary("p2p_sdk_android")
        }
    }

    external fun createSolendDepositTransactions(
        solana_rpc_url: String,
        relay_program_id: JString,
        amount: u64,
        symbol: JString,
        ownerAddres: JString,
        environment: JString,
        lendng_market_address: JString,
        blockhash: JString,
        free_transactions_count: u32,
        need_to_use_relay: jboolean,
        pay_fee_in_token: JString,
        fee_payer_address: JString,
    ): JString

    external fun createSolendWithdrawTransactions(
        solana_rpc_url: JString,
        relay_program_id: JString,
        amount: u64,
        symbol: JString,
        owner_address: JString,
        environment: JString,
        lendng_market_address: JString,
        blockhash: JString,
        free_transactions_count: u32,
        need_to_use_relay: jboolean,
        pay_fee_in_token: JString,
        fee_payer_address: JString
    ): JString

    external fun getSolendCollateralAccounts(
        rpc_url: JString,
        owner: JString,
    ): JString

    external fun getSolendConfig(
        environment: JString
    ): JString

    external fun getSolendDepositFees(
        rpc_url: JString,
        owner: JString,
        token_amount: jlong,
        token_symbol: JString
    ): JString

    external fun getSolendMarketInfo(
        tokens: JString,
        pool: JString
    ): JString

    external fun getSolendUserDepositBySymbol(
        owner: JString,
        symbol: JString,
        pool: JString
    )

    external fun getSolendUserDeposits(
        owner: JString,
        pool: JString
    ): JString
}
