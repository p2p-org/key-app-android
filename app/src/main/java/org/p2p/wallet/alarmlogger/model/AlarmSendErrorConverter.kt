package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.model.CurrencyMode
import org.p2p.core.network.data.ServerException
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSendRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSvlRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsUsernameRequest
import org.p2p.wallet.auth.username.repository.model.UsernameServiceError
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.newsend.model.SearchResult

class AlarmSendErrorConverter(
    private val gson: Gson,
    private val throwableFormatter: AlarmThrowableFormatter
) : AlarmFeatureConverter {

    fun toSendErrorRequest(
        token: Token.Active,
        currencyMode: CurrencyMode,
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
            currency = currencyMode.getTypedSymbol()
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
            recipientPubkey = recipientAddress.address.toBase58Instance(),
            recipientName = (recipientAddress as? SearchResult.UsernameFound)?.username.orEmpty(),
            simulationError = throwableFormatter.formatSimulationError(error).orEmpty(),
            feeRelayerError = throwableFormatter.formatFeeRelayerError(error).orEmpty(),
            blockchainError = throwableFormatter.formatBlockchainError(error).orEmpty()
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
            simulationError = throwableFormatter.formatSimulationError(error).orEmpty(),
            feeRelayerError = throwableFormatter.formatFeeRelayerError(error).orEmpty(),
            blockchainError = throwableFormatter.formatBlockchainError(error).orEmpty()
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
            simulationError = throwableFormatter.formatSimulationError(error).orEmpty(),
            feeRelayerError = throwableFormatter.formatFeeRelayerError(error).orEmpty(),
            blockchainError = throwableFormatter.formatBlockchainError(error).orEmpty()
        )

        return AlarmErrorsRequest(
            logsTitle = "Link Claim Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toUsernameErrorRequest(
        username: String,
        userPublicKey: Base58String,
        error: Throwable
    ): AlarmErrorsRequest {
        val errorDescription: String = when (error) {
            is UsernameServiceError -> "${error.javaClass.simpleName}: ${error.errorCode}, ${error.message}"
            is ServerException -> error.jsonErrorBody?.toString() ?: error.message ?: error.toString()
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
}
