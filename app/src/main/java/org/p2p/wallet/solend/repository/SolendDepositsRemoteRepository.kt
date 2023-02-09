package org.p2p.wallet.solend.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.model.solend.SolendFeePayerTokenData
import org.p2p.wallet.sdk.facade.model.solend.SolendPool
import org.p2p.wallet.solend.model.SolendDepositMapper
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.model.SolendMarketInfo
import org.p2p.wallet.solend.model.SolendTokenFee
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.Base58String
import timber.log.Timber
import java.math.BigInteger

class SolendDepositsRemoteRepository(
    private val sdkFacade: SolendSdkFacade,
    private val mapper: SolendDepositMapper,
    private val userLocalRepository: UserLocalRepository,
    private val homeLocalRepository: HomeLocalRepository
) : SolendRepository {

    private val currentSolendPool = SolendPool.MAIN

    override suspend fun getUserDeposits(
        ownerAddress: Base58String,
        tokenSymbols: List<String>
    ): List<SolendDepositToken> {
        val userTokens = homeLocalRepository.getUserTokens()

        val marketsInfo = try {
            getSolendMarketInfo(tokenSymbols)
        } catch (e: Throwable) {
            Timber.e(e, "Error loading user marketsInfo")
            emptyList()
        }

        val deposits = try {
            val response = sdkFacade.getAllSolendUserDeposits(ownerAddress, currentSolendPool)
            response.map { mapper.fromNetwork(it) }
        } catch (e: Throwable) {
            Timber.e(e, "Error loading user deposits")
            emptyList()
        }

        val noMarketInfoButHasDeposits = marketsInfo.isEmpty() && deposits.isNotEmpty()
        return if (noMarketInfoButHasDeposits) {
            deposits.mapNotNull { deposit ->
                val tokenData =
                    userLocalRepository.findTokenDataBySymbol(deposit.depositTokenSymbol) ?: return@mapNotNull null
                val tokenPrice =
                    userLocalRepository.getPriceByTokenId(deposit.depositTokenSymbol) ?: return@mapNotNull null
                val userToken = userTokens.find { it.tokenSymbol == deposit.depositTokenSymbol }
                mapper.fromNetwork(
                    tokenData = tokenData,
                    tokenPrice = tokenPrice,
                    userToken = userToken,
                    marketInfo = null,
                    activeDeposit = deposit
                )
            }
        } else {
            marketsInfo.mapNotNull { info ->
                val tokenData = userLocalRepository.findTokenDataBySymbol(info.tokenSymbol) ?: return@mapNotNull null
                val tokenPrice = userLocalRepository.getPriceByTokenId(info.tokenSymbol) ?: return@mapNotNull null
                val userToken = userTokens.find { it.tokenSymbol == info.tokenSymbol }
                val activeDeposit = deposits.find { it.depositTokenSymbol == info.tokenSymbol }
                mapper.fromNetwork(
                    tokenData = tokenData,
                    tokenPrice = tokenPrice,
                    userToken = userToken,
                    marketInfo = info,
                    activeDeposit = activeDeposit
                )
            }
        }
    }

    private suspend fun getSolendMarketInfo(tokens: List<String>): List<SolendMarketInfo> {
        val response = sdkFacade.getSolendMarketInfo(tokens, currentSolendPool.poolName)
        return mapper.fromNetwork(response)
    }

    override suspend fun createDepositTransaction(
        relayProgramId: String,
        ownerAddress: Base58String,
        token: SolendDepositToken,
        depositAmount: BigInteger,
        remainingFreeTransactionsCount: Int,
        lendingMarketAddress: String?,
        blockhash: String,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: PublicKey,
    ): String {
        val response = sdkFacade.createDepositTransactions(
            relayProgramId = relayProgramId,
            depositAmount = depositAmount.toLong().toULong(),
            currencySymbol = token.tokenSymbol,
            ownerAddress = ownerAddress,
            lendingMarketAddress = lendingMarketAddress,
            currentBlockhash = blockhash,
            remainingFreeTransactionsCount = remainingFreeTransactionsCount.toUInt(),
            payFeeWithRelay = payFeeWithRelay,
            feePayerToken = feePayerToken,
            realFeePayerAddress = realFeePayerAddress
        )

        val transactionId = response.transactions.firstOrNull()
        return transactionId ?: error("Error occurred while creating a withdraw transaction")
    }

    override suspend fun createWithdrawTransaction(
        relayProgramId: String,
        ownerAddress: Base58String,
        token: SolendDepositToken,
        withdrawAmount: BigInteger,
        remainingFreeTransactionsCount: Int,
        lendingMarketAddress: String?,
        blockhash: String,
        payFeeWithRelay: Boolean,
        feePayerToken: SolendFeePayerTokenData?,
        realFeePayerAddress: PublicKey,
    ): String? {
        val response = sdkFacade.createWithdrawTransactions(
            relayProgramId = relayProgramId,
            depositAmount = withdrawAmount.toLong().toULong(),
            currencySymbol = token.tokenSymbol,
            ownerAddress = ownerAddress,
            lendingMarketAddress = lendingMarketAddress,
            currentBlockhash = blockhash,
            remainingFreeTransactionsCount = remainingFreeTransactionsCount.toUInt(),
            payFeeWithRelay = payFeeWithRelay,
            feePayerToken = feePayerToken,
            realFeePayerAddress = realFeePayerAddress
        )

        return response.transactions.firstOrNull()
    }

    override suspend fun getDepositFee(
        owner: Base58String,
        feePayer: Base58String,
        tokenAmount: BigInteger,
        tokenSymbol: String
    ): SolendTokenFee {
        val response = sdkFacade.getSolendDepositFees(
            ownerAddress = owner,
            feePayer = feePayer,
            tokenAmount = tokenAmount.toLong(),
            tokenSymbol = tokenSymbol
        )

        return SolendTokenFee(
            transaction = response.fee.toBigInteger(),
            rent = response.rent.toBigInteger()
        )
    }

    override suspend fun getWithdrawFee(
        owner: Base58String,
        feePayer: Base58String,
        tokenAmount: BigInteger,
        tokenSymbol: String
    ): SolendTokenFee {
        val response = sdkFacade.getSolendWithdrawFees(
            ownerAddress = owner,
            feePayer = feePayer,
            tokenAmount = tokenAmount.toLong(),
            tokenSymbol = tokenSymbol
        )

        return SolendTokenFee(
            transaction = response.fee.toBigInteger(),
            rent = response.rent.toBigInteger()
        )
    }
}
