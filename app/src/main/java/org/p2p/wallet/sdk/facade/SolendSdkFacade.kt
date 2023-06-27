package org.p2p.wallet.sdk.facade

import com.google.gson.Gson
import org.p2p.solanaj.core.PublicKey
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.sdk.SolendSdk
import org.p2p.wallet.sdk.facade.mapper.SdkMethodResultMapper
import org.p2p.wallet.sdk.facade.model.solend.SolendCollateralAccountResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendCollateralAccountsListResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendConfigRootResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendDepositTransactionsResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendEnvironment
import org.p2p.wallet.sdk.facade.model.solend.SolendFeePayerTokenData
import org.p2p.wallet.sdk.facade.model.solend.SolendMarketInformationResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendPool
import org.p2p.wallet.sdk.facade.model.solend.SolendTokenDepositFeesResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendUserDepositByTokenResponseResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendUserDepositResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendUserDepositsResponseResponse
import org.p2p.wallet.sdk.facade.model.solend.SolendWithdrawTransactionsResponse
import org.p2p.core.crypto.Base58String
import kotlinx.coroutines.withContext

class SolendSdkFacade(
    private val solendSdk: SolendSdk,
    private val networkEnvironmentManager: NetworkEnvironmentManager,
    private val methodResultMapper: SdkMethodResultMapper,
    private val logger: AppSdkLogger,
    private val gson: Gson,
    private val dispatchers: CoroutineDispatchers
) {

    private val currentNetworkEnvironment: NetworkEnvironment
        //        get() = networkEnvironmentManager.loadCurrentEnvironment()
        get() = NetworkEnvironment.MAINNET

    private val solendEnvironment: SolendEnvironment
        get() = SolendEnvironment.PRODUCTION

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
        realFeePayerAddress: PublicKey
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
            fee_payer_address = realFeePayerAddress.toBase58()
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
        realFeePayerAddress: PublicKey
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
            fee_payer_address = realFeePayerAddress.toBase58()
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
        feePayer: Base58String,
        tokenAmount: Long,
        tokenSymbol: String
    ): SolendTokenDepositFeesResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendDepositFees", ownerAddress, tokenAmount, tokenSymbol)

        val response = solendSdk.getSolendDepositFees(
            rpc_url = currentNetworkEnvironment.endpoint,
            owner = ownerAddress.base58Value,
            fee_payer = feePayer.base58Value,
            token_amount = tokenAmount,
            token_symbol = tokenSymbol
        )
        logger.logResponse("getSolendDepositFees", response)
        methodResultMapper.fromSdk(response)
    }

    suspend fun getSolendWithdrawFees(
        ownerAddress: Base58String,
        feePayer: Base58String,
        tokenAmount: Long,
        tokenSymbol: String
    ): SolendTokenDepositFeesResponse = withContext(dispatchers.io) {
        logger.logRequest("getSolendWithdrawFees", ownerAddress, tokenAmount, tokenSymbol)

        val response = solendSdk.getSolendWithdrawFees(
            rpc_url = currentNetworkEnvironment.endpoint,
            owner = ownerAddress.base58Value,
            fee_payer = feePayer.base58Value,
            token_amount = tokenAmount,
            token_symbol = tokenSymbol
        )
        logger.logResponse("getSolendWithdrawFees", response)
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
            pool = solendPoolAddress.poolName
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
            pool = solendPoolAddress.poolName
        )
        logger.logResponse("getSolendUserDeposits", response)
        methodResultMapper.fromSdk<SolendUserDepositsResponseResponse>(response).deposits
    }
}
