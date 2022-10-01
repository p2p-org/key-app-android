package org.p2p.wallet.sdk.facade

import com.google.gson.Gson
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.mapper.SolendMethodResultMapper
import org.p2p.wallet.sdk.facade.model.SolendCollateralAccountResponse
import org.p2p.wallet.sdk.facade.model.SolendCollateralAccountsListResponse
import org.p2p.wallet.sdk.facade.model.SolendConfigResponse
import org.p2p.wallet.sdk.facade.model.SolendDepositTransactionsResponse
import org.p2p.wallet.sdk.facade.model.SolendEnvironment
import org.p2p.wallet.sdk.facade.model.SolendFeePayerTokenData
import org.p2p.wallet.sdk.facade.model.SolendMarketInformationResponse
import org.p2p.wallet.sdk.facade.model.SolendPool
import org.p2p.wallet.sdk.facade.model.SolendTokenDepositFeesResponse
import org.p2p.wallet.sdk.facade.model.SolendUserDepositByTokenResponseResponse
import org.p2p.wallet.sdk.facade.model.SolendUserDepositResponse
import org.p2p.wallet.sdk.facade.model.SolendUserDepositsResponseResponse
import org.p2p.wallet.sdk.facade.model.SolendWithdrawTransactionsResponse
import org.p2p.wallet.utils.Base58String
import kotlinx.coroutines.withContext

class SolendSdkFacade(
    private val solendSdk: SolendSdk,
    private val solendEnvironment: SolendEnvironment,
    private val networkEnvironmentManager: NetworkEnvironmentManager,
    private val methodResultMapper: SolendMethodResultMapper,
    private val logger: SolendSdkLogger,
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
    ): SolendDepositTransactionsResponse = withContext(dispatchers.io) {
        logger.logRequest(
            "createDepositTransactions",
            relayProgramId,
            depositAmount,
            currencySymbol,
            ownerAddress,
            lendingMarketAddress,
            currentBlockhash,
            remainingFreeTransactionsCount,
            payFeeWithRelay,
            feePayerToken,
            realFeePayerAddress
        )

        val response = solendSdk.createSolendDepositTransactions(
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
        methodResultMapper.fromSdk(response)
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
    ): SolendWithdrawTransactionsResponse = withContext(dispatchers.io) {
        logger.logRequest(
            "createWithdrawTransactions",
            relayProgramId,
            depositAmount,
            currencySymbol,
            ownerAddress,
            lendingMarketAddress,
            currentBlockhash,
            remainingFreeTransactionsCount,
            payFeeWithRelay,
            feePayerToken,
            realFeePayerAddress
        )

        val response = solendSdk.createSolendWithdrawTransactions(
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
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendCollateralAccounts(
        ownerAddress: Base58String
    ): List<SolendCollateralAccountResponse> = withContext(dispatchers.io) {
        logger.logRequest("getSolendCollateralAccounts", ownerAddress)

        val response = solendSdk.getSolendCollateralAccounts(
            rpc_url = currentNetworkEnvironment.endpoint,
            owner = ownerAddress.value
        )
        methodResultMapper.fromSdk<SolendCollateralAccountsListResponse>(response).accounts
    }

    suspend fun getSolendConfig(): SolendConfigResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendConfig")

        val response = solendSdk.getSolendConfig(
            environment = solendEnvironment.sdkValue
        )
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendDepositFees(
        ownerAddress: Base58String,
        tokenAmountToDeposit: Long,
        tokenAddressToDeposit: Base58String
    ): SolendTokenDepositFeesResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendDepositFees", ownerAddress, tokenAmountToDeposit, tokenAddressToDeposit)

        val response = solendSdk.getSolendDepositFees(
            rpc_url = currentNetworkEnvironment.endpoint,
            owner = ownerAddress.value,
            token_amount = tokenAmountToDeposit,
            token_symbol = tokenAddressToDeposit.value
        )
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendMarketInfo(
        tokens: List<Token>,
        solendPoolName: String
    ): SolendMarketInformationResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendMarketInfo", tokens, solendPoolName)

        val tokenSymbols = tokens.joinToString(separator = ",", transform = Token::tokenSymbol)
        val response = solendSdk.getSolendMarketInfo(
            tokens = tokenSymbols,
            pool = solendPoolName
        )
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendUserDepositByTokenSymbol(
        userAddress: Base58String,
        tokenSymbol: String,
        solendPoolAddress: SolendPool
    ): SolendUserDepositResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendUserDepositByTokenSymbol", userAddress, tokenSymbol, solendPoolAddress)

        val response = solendSdk.getSolendUserDepositBySymbol(
            owner = userAddress.value,
            symbol = tokenSymbol,
            pool = solendPoolAddress.poolAddress.value
        )
        methodResultMapper.fromSdk<SolendUserDepositByTokenResponseResponse>(response).userDepositBySymbol
    }

    suspend fun getAllSolendUserDeposits(
        userAddress: Base58String,
        solendPoolAddress: SolendPool
    ): List<SolendUserDepositResponse> = withContext(dispatchers.io) {
        logger.logRequest("getSolendUserDeposits", userAddress, solendPoolAddress)

        val response = solendSdk.getSolendUserDeposits(
            owner = userAddress.value,
            pool = solendPoolAddress.poolAddress.value
        )
        methodResultMapper.fromSdk<SolendUserDepositsResponseResponse>(response).deposits
    }
}
