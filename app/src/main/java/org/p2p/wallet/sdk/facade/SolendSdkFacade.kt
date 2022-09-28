package org.p2p.wallet.sdk.facade

import com.google.gson.Gson
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.sdk.facade.mapper.SolendMethodResultMapper
import org.p2p.wallet.sdk.facade.model.SolendEnvironment
import org.p2p.wallet.sdk.facade.model.SolendFeePayerTokenData
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendDepositTransactions
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendWithdrawTransactions
import timber.log.Timber
import kotlinx.coroutines.withContext

private const val TAG = "P2PSdkFacade"

class SolendSdkFacade(
    private val solendSdk: SolendSdk,
    private val solendEnvironment: SolendEnvironment,
    private val networkEnvironmentManager: NetworkEnvironmentManager,
    private val methodResultHandler: SolendMethodResultHandler,
    private val methodResultMapper: SolendMethodResultMapper,
    private val gson: Gson,
    private val dispatchers: CoroutineDispatchers
) {

    private val currentNetworkEnvironment: NetworkEnvironment
        get() = networkEnvironmentManager.loadCurrentEnvironment()

    suspend fun createDepositTransactions(
        relayProgramId: String,
        depositAmount: ULong,
        currencySymbol: String,
        ownerAddress: Base58String,
        lendingMarketAddress: String?,
        currentBlockhash: String,
        remainingFreeTransactionsCount: UInt,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: Base58String
    ) {
        withContext(dispatchers.io) {
            Timber.tag(TAG).i("Method createSolendDepositTransactions called")

            val result = solendSdk.createSolendDepositTransactions(
                solana_rpc_url = currentNetworkEnvironment.endpoint,
                relay_program_id = relayProgramId,
                amount = depositAmount,
                symbol = currencySymbol,
                ownerAddres = ownerAddress.value,
                environment = solendEnvironment.sdkValue,
                lendng_market_address = lendingMarketAddress.orEmpty(),
                blockhash = currentBlockhash,
                free_transactions_count = remainingFreeTransactionsCount,
                need_to_use_relay = payFeeWithRelay,
                pay_fee_in_token = feePayerToken?.let { gson.toJson(it) }.orEmpty(),
                fee_payer_address = realFeePayerAddress.value
            )
            handleSdkResult<SolendDepositTransactions>(result)
        }
    }

    fun createWithdrawTransactions(
        relayProgramId: String,
        depositAmount: ULong,
        currencySymbol: String,
        ownerAddress: Base58String,
        lendingMarketAddress: String?,
        currentBlockhash: String,
        remainingFreeTransactionsCount: UInt,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: Base58String
    ) {
        Timber.tag(TAG).i("Method createSolendWithdrawTransactions called")

        val result = solendSdk.createSolendWithdrawTransactions(
            solana_rpc_url = currentNetworkEnvironment.endpoint,
            relay_program_id = relayProgramId,
            amount = depositAmount,
            symbol = currencySymbol,
            owner_address = ownerAddress.value,
            environment = solendEnvironment.sdkValue,
            lendng_market_address = lendingMarketAddress.orEmpty(),
            blockhash = currentBlockhash,
            free_transactions_count = remainingFreeTransactionsCount,
            need_to_use_relay = payFeeWithRelay,
            pay_fee_in_token = feePayerToken?.let { gson.toJson(it) }.orEmpty(),
            fee_payer_address = realFeePayerAddress.value
        )
        handleSdkResult<SolendWithdrawTransactions>(result)
    }

    private inline fun <reified T : SolendMethodResultSuccess> handleSdkResult(result: String) {
        methodResultMapper.fromSdk<T>(result)
            .onResultSuccess { methodResultHandler.handleResultSuccess(it) }
            .onResultError {
                methodResultHandler.handleResultError(it)
            }
    }
}
