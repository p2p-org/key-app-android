package org.p2p.wallet.jupiter.interactor

import assertk.Assert
import assertk.assertions.isInstanceOf
import assertk.assertions.support.expected
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.toBase64Instance
import org.p2p.core.network.data.ErrorCode
import org.p2p.core.network.data.ServerException
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.core.network.data.transactionerrors.TransactionInstructionError
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.sdk.facade.model.relay.RelaySdkSignedTransaction
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.generateRandomBytes
import org.p2p.wallet.utils.stub
import org.p2p.core.crypto.toBase58Instance

class JupiterSendSwapTransactionDelegateTest {

    private val rpcSolanaRepository: RpcSolanaRepository = mockk()
    private val relaySdkFacade: RelaySdkFacade = mockk {
        coEvery { signTransaction(any<Base64String>(), any(), any()) }
            .returns(RelaySdkSignedTransaction(generateRandomBytes().toBase58Instance()))
        coEvery { signTransaction(any<Base58String>(), any(), any()) }
            .returns(RelaySdkSignedTransaction(generateRandomBytes().toBase58Instance()))
    }
    private val tokenKeyProvider: TokenKeyProvider = mockk {
        val randomPublicKey = generateRandomBytes().toBase58Instance()
        val randomKeyPair = generateRandomBytes(64)
        every { publicKey }.returns(randomPublicKey.base58Value)
        every { publicKeyBase58 }.returns(randomPublicKey)
        every { keyPair }.returns(randomKeyPair)
    }
    private val swapTransactionRepository: JupiterSwapTransactionRepository = mockk()

    private val stubJupiterSwapRoute = mockk<JupiterSwapRoute>(relaxed = true)

    private lateinit var sendSwapTransactionDelegate: JupiterSwapSendTransactionDelegate

    private val stubTransaction: Base64String = generateRandomBytes().toBase64Instance()

    @Before
    fun setUp() {
        sendSwapTransactionDelegate = JupiterSwapSendTransactionDelegate(
            rpcSolanaRepository = rpcSolanaRepository,
            swapTransactionRepository = swapTransactionRepository,
            tokenKeyProvider = tokenKeyProvider,
            relaySdkFacade = relaySdkFacade
        )
    }

    @Test
    fun `GIVEN success send transaction WHEN send transaction THEN return success`() = runBlocking {
        // GIVEN
        rpcSolanaRepository.stub {
            coEvery { sendTransaction(any(), Encoding.BASE64) }.returns("")
        }

        // WHEN
        val result = sendSwapTransactionDelegate.sendSwapTransaction(stubJupiterSwapRoute, stubTransaction)

        // THEN
        result.assertThat()
            .assertIsSuccess()
        Unit
    }

    @Test
    fun `GIVEN LowSlippage error WHEN send transaction THEN return LowSlippage error`() = runBlocking {
        // GIVEN
        rpcSolanaRepository.stub {
            coEvery { sendTransaction(any(), Encoding.BASE64) }
                .throws(createCustomInstructionError(6001L))
        }

        // WHEN
        val result = sendSwapTransactionDelegate.sendSwapTransaction(stubJupiterSwapRoute, stubTransaction)

        // THEN
        result.assertThat()
            .isInstanceOf(JupiterSwapTokensResult.Failure::class)
            .transform { it.cause }
            .isInstanceOf(JupiterSwapTokensResult.Failure.LowSlippageRpcError::class)
        Unit
    }

    @Test
    fun `GIVEN InvalidTimestamp error WHEN send transaction THEN try send again with success`() = runBlocking {
        // GIVEN
        rpcSolanaRepository.stub {
            coEvery { sendTransaction(any(), Encoding.BASE64) }
                .throws(createCustomInstructionError(6022L))
                .andThen("")
        }
        swapTransactionRepository.stub {
            coEvery { createSwapTransactionForRoute(any(), any()) }
                .returns(stubTransaction)
        }

        // WHEN
        val result = sendSwapTransactionDelegate.sendSwapTransaction(stubJupiterSwapRoute, stubTransaction)

        // THEN
        result.assertThat().assertIsSuccess()
        coVerify { swapTransactionRepository.createSwapTransactionForRoute(any(), any()) }
        coVerify(exactly = 2) {
            rpcSolanaRepository.sendTransaction(any(), Encoding.BASE64)
        }
    }

    @Test
    fun `GIVEN InvalidTimestamp twice error WHEN send transaction THEN try send again with error`() = runBlocking {
        // GIVEN
        rpcSolanaRepository.stub {
            coEvery { sendTransaction(any(), Encoding.BASE64) }
                .throws(createCustomInstructionError(6022L))
                .andThenThrows(createCustomInstructionError(6022L))
        }
        swapTransactionRepository.stub {
            coEvery { createSwapTransactionForRoute(any(), any()) }
                .returns(stubTransaction)
        }

        // WHEN
        val result = sendSwapTransactionDelegate.sendSwapTransaction(stubJupiterSwapRoute, stubTransaction)

        // THEN
        result.assertThat()
            .isInstanceOf(JupiterSwapTokensResult.Failure::class)
        coVerify(exactly = 2) {
            swapTransactionRepository.createSwapTransactionForRoute(any(), any())
        }
        coVerify(exactly = 2) {
            rpcSolanaRepository.sendTransaction(any(), Encoding.BASE64)
        }
    }

    private fun createCustomInstructionError(errorCode: Long): ServerException {
        return ServerException(
            errorCode = ErrorCode.SERVER_ERROR,
            fullMessage = "FAILED",
            errorMessage = null,
            jsonErrorBody = null,
            domainErrorType = RpcTransactionError.InstructionError(
                instructionIndex = 1,
                instructionErrorType = TransactionInstructionError.Custom(errorCode)
            )
        )
    }

    private fun Assert<JupiterSwapTokensResult>.assertIsSuccess() {
        given { actual ->
            if (actual is JupiterSwapTokensResult.Success) return
            actual as JupiterSwapTokensResult.Failure
            expected("result is :${JupiterSwapTokensResult.Success::class} but was :${actual::class} with $actual")
        }
    }
}
