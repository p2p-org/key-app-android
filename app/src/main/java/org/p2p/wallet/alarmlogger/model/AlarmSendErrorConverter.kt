package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import java.math.BigInteger
import java.net.UnknownHostException
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.alarmlogger.api.AlarmErrorsBridgeClaimRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSendBridgeRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSendRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSvlRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsUsernameRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsWeb3Request
import org.p2p.wallet.auth.username.repository.model.UsernameServiceError
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.feerelayer.model.FeeRelayerException
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.data.SimulationException
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance

class AlarmSendErrorConverter(
    private val gson: Gson
) {

    fun toSendErrorRequest(
        token: Token.Active,
        currency: String,
        amount: String,
        feePayerToken: Token.Active,
        accountCreationFee: String?,
        transactionFee: String?,
        relayAccount: RelayAccount,
        userPublicKey: Base58String,
        recipientAddress: SearchResult,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToSend = AlarmErrorsSendRequest.TokenToSend(
            tokenName = token.tokenName,
            mint = token.mintAddress.toBase58Instance(),
            amount = amount,
            currency = currency
        )

        val fees = buildSendFee(feePayerToken, accountCreationFee, transactionFee)

        val relayAccountState = AlarmErrorsSendRequest.RelayAccountStateRequest(
            created = relayAccount.isCreated,
            balance = relayAccount.balance?.toString() ?: "none",
        )

        val request = AlarmErrorsSendRequest(
            tokenToSend = tokenToSend,
            fees = fees,
            relayAccountState = relayAccountState,
            userPubkey = userPublicKey,
            recipientPubkey = recipientAddress.addressState.address.toBase58Instance(),
            recipientName = (recipientAddress as? SearchResult.UsernameFound)?.username.orEmpty(),
            simulationError = error.getSimulationError(),
            feeRelayerError = error.getFeeRelayerError(),
            blockchainError = error.getBlockchainError()
        )

        return AlarmErrorsRequest(
            logsTitle = "Send Android Alarm",
            payload = gson.toJson(request)
        )
    }

    private fun buildSendFee(
        feePayerToken: Token.Active,
        accountCreationFee: String?,
        transactionFee: String?
    ): AlarmErrorsSendRequest.Fees {
        if (accountCreationFee == null && transactionFee == null) {
            return AlarmErrorsSendRequest.Fees(
                transactionFeeAmount = 0.toString(),
                accountCreationFee = null
            )
        }

        return AlarmErrorsSendRequest.Fees(
            transactionFeeAmount = transactionFee ?: 0.toString(),
            accountCreationFee = AlarmErrorsSendRequest.AccountCreationFee(
                paymentToken = AlarmErrorsSendRequest.PaymentToken(
                    name = feePayerToken.tokenName,
                    mint = feePayerToken.mintAddress.toBase58Instance()
                ),
                amount = accountCreationFee ?: 0.toString()
            )
        )
    }

    fun toSendViaLinkErrorRequest(
        token: Token.Active,
        lamports: BigInteger,
        userPublicKey: Base58String,
        currency: String,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToSend = AlarmErrorsSvlRequest.TokenToSend(
            tokenName = token.tokenName,
            mint = token.mintAddress.toBase58Instance(),
            amount = lamports.fromLamports(token.decimals).toPlainString(),
            currency = currency
        )

        val request = AlarmErrorsSvlRequest(
            tokenToSend = tokenToSend,
            tokenToClaim = null,
            userPubkey = userPublicKey,
            simulationError = error.getSimulationError(),
            feeRelayerError = error.getFeeRelayerError(),
            blockchainError = error.getBlockchainError(),
        )

        return AlarmErrorsRequest(
            logsTitle = "Link Create Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toClaimViaLinkErrorRequest(
        token: Token.Active,
        lamports: BigInteger,
        userPublicKey: Base58String,
        currency: String,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToClaim = AlarmErrorsSvlRequest.TokenToSend(
            tokenName = token.tokenName,
            mint = token.mintAddress.toBase58Instance(),
            amount = lamports.fromLamports(token.decimals).toPlainString(),
            currency = currency
        )

        val request = AlarmErrorsSvlRequest(
            tokenToSend = null,
            tokenToClaim = tokenToClaim,
            userPubkey = userPublicKey,
            simulationError = error.getSimulationError(),
            feeRelayerError = error.getFeeRelayerError(),
            blockchainError = error.getBlockchainError(),
        )

        return AlarmErrorsRequest(
            logsTitle = "Link Claim Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toBridgeClaimErrorRequest(
        userPublicKey: Base58String,
        userEthAddress: String,
        token: Token.Eth,
        claimAmount: String,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToClaim = AlarmErrorsBridgeClaimRequest.TokenToClaim(
            tokenName = token.tokenName,
            solanaMint = token.mintAddress.toBase58Instance(),
            ethMint = token.publicKey,
            claimAmount = claimAmount,
        )
        val request = AlarmErrorsBridgeClaimRequest(
            tokenToClaim = tokenToClaim,
            userPubkey = userPublicKey,
            userEthPubkey = userEthAddress,
            simulationError = error.getSimulationError(),
            feeRelayerError = error.getFeeRelayerError(),
            blockchainError = error.getBlockchainError(),
        )

        return AlarmErrorsRequest(
            logsTitle = "Wormhole Claim Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toBridgeSendErrorRequest(
        token: Token.Active,
        userPublicKey: Base58String,
        currency: String,
        sendAmount: String,
        arbiterFeeAmount: String,
        recipientEthPubkey: String,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToClaim = AlarmErrorsSendBridgeRequest.TokenToSend(
            tokenName = token.tokenName,
            mint = token.mintAddress.toBase58Instance(),
            amount = sendAmount,
            currency = currency
        )
        val request = AlarmErrorsSendBridgeRequest(
            tokenToSend = tokenToClaim,
            arbiterFeeAmount = arbiterFeeAmount,
            userPubkey = userPublicKey,
            recipientEthPubkey = recipientEthPubkey,
            simulationError = error.getSimulationError(),
            feeRelayerError = error.getFeeRelayerError(),
            blockchainError = error.getBlockchainError(),
        )

        return AlarmErrorsRequest(
            logsTitle = "Wormhole Send Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toUsernameErrorRequest(
        username: String,
        userPublicKey: Base58String,
        error: Throwable
    ): AlarmErrorsRequest {
        val errorDescription = when (error) {
            is UsernameServiceError -> "${error.javaClass.simpleName}: ${error.errorCode}, ${error.message}"
            else -> error.message ?: error.toString()
        }
        val request = AlarmErrorsUsernameRequest(
            username = username,
            userPubkey = userPublicKey,
            nameServiceError = errorDescription
        )

        return AlarmErrorsRequest(
            logsTitle = "Name Create Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toWeb3ErrorRequest(web3Error: String): AlarmErrorsRequest {
        // no user public key here, because we don't have it at this step
        val request = AlarmErrorsWeb3Request(
            web3Error = web3Error
        )

        return AlarmErrorsRequest(
            logsTitle = "Web3 Registration Android Alarm",
            payload = gson.toJson(request)
        )
    }
}

private fun Throwable.getSimulationError(): String = when (this) {
    is SimulationException -> "Simulation error: ${getDirectMessage().orEmpty()}"
    else -> emptyString()
}

private fun Throwable.getFeeRelayerError(): String = when (this) {
    is FeeRelayerException -> "FeeRelayer error: ${getDirectMessage().orEmpty()}"
    else -> emptyString()
}

private fun Throwable.getBlockchainError(): String = when (this) {
    is ServerException -> "Blockchain error: ${getDirectMessage().orEmpty()}"
    is UnknownHostException -> "Internet error ${message ?: localizedMessage}"
    is BridgeResult.Error -> "Bridge error: ${this.javaClass.simpleName}"
    is FeeRelayerException -> emptyString()
    is SimulationException -> emptyString()
    else -> "Unknown error: ${message ?: localizedMessage}"
}
