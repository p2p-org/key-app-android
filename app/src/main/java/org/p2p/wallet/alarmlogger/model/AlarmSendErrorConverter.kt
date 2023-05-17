package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import java.math.BigInteger
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.alarmlogger.api.AlarmErrorsBridgeClaimRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSendRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSvlRequest
import org.p2p.wallet.alarmlogger.utils.getBlockchainError
import org.p2p.wallet.alarmlogger.utils.getFeeRelayerError
import org.p2p.wallet.alarmlogger.utils.getSimulationError
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

class AlarmSendErrorConverter(
    private val gson: Gson
) {

    fun toSendError(
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
            currency = currencyMode.getCurrencyModeSymbol()
        )

        val fees = buildSendFee(feePayerToken, accountCreationFee, transactionFee)

        val relayAccountState = AlarmErrorsSendRequest.RelayAccountState(
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

    fun toSendViaLinkError(
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

    fun toClaimViaLinkError(
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

    fun toBridgeClaimError(
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
}
