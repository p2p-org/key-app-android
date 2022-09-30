package org.p2p.wallet.sdk.facade

import com.google.gson.Gson
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.mapper.SolendMethodResultMapper
import org.p2p.wallet.sdk.facade.model.SolendConfigResponse
import org.p2p.wallet.sdk.facade.model.SolendEnvironment
import org.p2p.wallet.sdk.facade.model.SolendFeePayerTokenData
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendCollateralAccount
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendCollateralAccountsList
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendDepositTransactions
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendMarketInformation
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendTokenDepositFees
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendUserDeposit
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendUserDepositByTokenResponse
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendUserDepositsResponse
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess.SolendWithdrawTransactions
import org.p2p.wallet.utils.Base58String
import timber.log.Timber
import kotlinx.coroutines.withContext

private const val TAG = "SolendSdkFacade"

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
            handleSdkResultWithHandler<SolendDepositTransactions>(result)
        }
    }

    suspend fun createWithdrawTransactions(
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
            handleSdkResultWithHandler<SolendWithdrawTransactions>(result)
        }
    }

    suspend fun getSolendCollateralAccounts(
        ownerAddress: Base58String
    ): List<SolendCollateralAccount> = withContext(dispatchers.io) {
        Timber.tag(TAG).i("Method getSolendCollateralAccounts called")

        methodResultMapper.fromSdk<SolendCollateralAccountsList>(
            solendSdk.getSolendCollateralAccounts(
                rpc_url = currentNetworkEnvironment.endpoint,
                owner = ownerAddress.value
            )
        )
            .getOrThrow()
            .accounts
    }

    suspend fun getSolendConfig(): SolendConfigResponse = withContext(dispatchers.io) {
        Timber.tag(TAG).i("Method getSolendConfig called")

        methodResultMapper.fromSdk<SolendConfigResponse>(
            solendSdk.getSolendConfig(
                environment = solendEnvironment.sdkValue
            )
        )
            .getOrThrow()
    }

    suspend fun getSolendDepositFees(
        addressOwner: Base58String,
        tokenAmountToDeposit: Long,
        tokenAddressToDeposit: Base58String
    ): SolendTokenDepositFees {
        return withContext(dispatchers.io) {
            Timber.tag(TAG).i("Method getSolendDepositFees called")

            methodResultMapper.fromSdk<SolendTokenDepositFees>(
                solendSdk.getSolendDepositFees(
                    rpc_url = currentNetworkEnvironment.endpoint,
                    owner = addressOwner.value,
                    token_amount = tokenAmountToDeposit,
                    token_symbol = tokenAddressToDeposit.value
                )
            )
                .getOrThrow()
        }
    }

    suspend fun getSolendMarketInfo(
        tokens: List<Token>,
        solendPoolName: String
    ) {
        return withContext(dispatchers.io) {
            Timber.tag(TAG).i("Method getSolendMarketInfo called")

            val tokenSymbols = tokens.joinToString(separator = ",", transform = Token::tokenSymbol)
            methodResultMapper.fromSdk<SolendMarketInformation>(
                solendSdk.getSolendMarketInfo(
                    tokens = tokenSymbols,
                    pool = solendPoolName
                )
            )
                .getOrThrow()
        }
    }

    suspend fun getSolendUserDepositByTokenSymbol(
        userAddress: Base58String,
        tokenSymbol: String,
        solendPoolAddress: SolendPool
    ): SolendUserDeposit = withContext(dispatchers.io) {
        Timber.tag(TAG).i("Method getSolendUserDepositBySymbol with $tokenSymbol called")

        methodResultMapper.fromSdk<SolendUserDepositByTokenResponse>(
            solendSdk.getSolendUserDepositBySymbol(
                owner = userAddress.value,
                symbol = tokenSymbol,
                pool = solendPoolAddress.poolAddress.value
            )
        )
            .getOrThrow()
            .userDepositBySymbol
    }

    suspend fun getAllSolendUserDeposits(
        userAddress: Base58String,
        solendPoolAddress: SolendPool
    ): List<SolendUserDeposit> = withContext(dispatchers.io) {
        Timber.tag(TAG).i("Method getSolendUserDeposits called")

        methodResultMapper.fromSdk<SolendUserDepositsResponse>(
            solendSdk.getSolendUserDeposits(
                owner = userAddress.value,
                pool = solendPoolAddress.poolAddress.value
            )
        )
            .getOrThrow()
            .deposits
    }

    private inline fun <reified T : SolendMethodResultSuccess> handleSdkResultWithHandler(result: String) {
        methodResultMapper.fromSdk<T>(result)
            .onResultSuccess { methodResultHandler.handleResultSuccess(it) }
            .onResultError { methodResultHandler.handleResultError(it) }
    }
}
