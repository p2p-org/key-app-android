package org.p2p.wallet.sdk.facade

import com.google.gson.Gson
import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.mapper.SolendMethodResultMapper
import org.p2p.wallet.sdk.facade.model.SolendCollateralAccountResponse
import org.p2p.wallet.sdk.facade.model.SolendCollateralAccountsListResponse
import org.p2p.wallet.sdk.facade.model.SolendConfigRootResponse
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
        //        get() = networkEnvironmentManager.loadCurrentEnvironment()
        get() = NetworkEnvironment.MAINNET

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
            ownerAddres = ownerAddress.base58Value,
            environment = solendEnvironment.sdkValue,
            lendng_market_address = lendingMarketAddress.orEmpty(),
            blockhash = currentBlockhash,
            free_transactions_count = remainingFreeTransactionsCount,
            need_to_use_relay = payFeeWithRelay,
            pay_fee_in_token = feePayerToken?.let { gson.toJson(it) }.orEmpty(),
            fee_payer_address = realFeePayerAddress.base58Value
        )
        logger.logResponse("createDepositTransactions", response)
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
            owner_address = ownerAddress.base58Value,
            environment = solendEnvironment.sdkValue,
            lendng_market_address = lendingMarketAddress.orEmpty(),
            blockhash = currentBlockhash,
            free_transactions_count = remainingFreeTransactionsCount,
            need_to_use_relay = payFeeWithRelay,
            pay_fee_in_token = feePayerToken?.let { gson.toJson(it) }.orEmpty(),
            fee_payer_address = realFeePayerAddress.base58Value
        )
        logger.logResponse("createWithdrawTransactions", response)
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendCollateralAccounts(
        ownerAddress: Base58String
    ): List<SolendCollateralAccountResponse> = withContext(dispatchers.io) {
        logger.logRequest("getSolendCollateralAccounts", ownerAddress)

        val response = solendSdk.getSolendCollateralAccounts(
            rpc_url = currentNetworkEnvironment.endpoint,
            owner = ownerAddress.base58Value
        )
        logger.logResponse("getSolendCollateralAccounts", response)
        methodResultMapper.fromSdk<SolendCollateralAccountsListResponse>(response).accounts
    }

    suspend fun getSolendConfig(): SolendConfigRootResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendConfig")

        val response = solendSdk.getSolendConfig(
            environment = solendEnvironment.sdkValue
        )
        logger.logResponse("getSolendConfig", response)
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
            owner = ownerAddress.base58Value,
            token_amount = tokenAmountToDeposit,
            token_symbol = tokenAddressToDeposit.base58Value
        )
        logger.logResponse("getSolendDepositFees", response)
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendMarketInfo(
        tokenSymbols: List<String>,
        solendPoolName: String
    ): SolendMarketInformationResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendMarketInfo", tokenSymbols, solendPoolName)

        val response = solendSdk.getSolendMarketInfo(
            tokens = tokenSymbols.joinToString(separator = ","),
            pool = solendPoolName
        )
        logger.logResponse("getSolendMarketInfo", response)

        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendUserDepositByTokenSymbol(
        userAddress: Base58String,
        tokenSymbol: String,
        solendPoolAddress: SolendPool
    ): SolendUserDepositResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendUserDepositByTokenSymbol", userAddress, tokenSymbol, solendPoolAddress)

        val response = solendSdk.getSolendUserDepositBySymbol(
            owner = userAddress.base58Value,
            symbol = tokenSymbol,
            pool = solendPoolAddress.poolAddress.base58Value
        )
        logger.logResponse("getSolendUserDepositByTokenSymbol", response)
        methodResultMapper.fromSdk<SolendUserDepositByTokenResponseResponse>(response).userDepositBySymbol
    }

    suspend fun getAllSolendUserDeposits(
        userAddress: Base58String,
        solendPoolAddress: SolendPool
    ): List<SolendUserDepositResponse> = withContext(dispatchers.io) {
        logger.logRequest("getSolendUserDeposits", userAddress, solendPoolAddress)

        val response = solendSdk.getSolendUserDeposits(
            owner = userAddress.base58Value,
            pool = solendPoolAddress.poolAddress.base58Value
        )
        logger.logResponse("getSolendUserDeposits", response)
        methodResultMapper.fromSdk<SolendUserDepositsResponseResponse>(response).deposits
    }
}
