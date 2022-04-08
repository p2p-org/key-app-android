package org.p2p.solanaj.kits.renBridge

import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.p2p.solanaj.kits.renBridge.renVM.RenVMRepository
import org.p2p.solanaj.rpc.RpcSolanaInteractor
import org.robolectric.RobolectricTestRunner
import java.math.BigInteger
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class BurnAndReleaseTest : KoinTest {

    private val renVmRepository: RenVMRepository by inject()
    private val rpcSolanaInteractor: RpcSolanaInteractor by inject()
    private val state = LockAndMint.State()

    @Test
    fun getBurnStateTest() {
        val burnAndRelease = BurnAndRelease(
            renVMProvider = renVmRepository,
            rpcSolanaInteractor = rpcSolanaInteractor,
            state = state
        )
        val burnDetails = BurnDetails()
        burnDetails.confirmedSignature =
            "2kNe8duPRcE9xxKLLVP92e9TBH5WvmVVWQJ18gEjqhgxsrKtBEBVfeXNFz5Un3yEEQJZkxY2ysQR4dGQaytnDM1i"
        burnDetails.nonce = BigInteger.valueOf(35)
        burnDetails.recepient = "tb1ql7w62elx9ucw4pj5lgw4l028hmuw80sndtntxt"
        assertEquals(
            "I_HJMksqVC5_-0G9FE_z8AORRDMoxl1vZbSGEc2VfJ4",
            burnAndRelease.getBurnState(burnDetails, "1000").txHash
        )
    }
}
