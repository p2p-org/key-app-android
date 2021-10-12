package com.p2p.wallet.swap.orca.repository

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.orcaswap.TokenSwap

class OrcaSwapRemoteRepository(
    private val rpcRepository: RpcRepository
) : OrcaSwapRepository {

    override suspend fun loadPoolInfoList(swapProgramId: String): List<Pool.PoolInfo> {
        val publicKey = PublicKey(swapProgramId)
        return rpcRepository.getPools(publicKey)
    }

    override suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance =
        rpcRepository.getTokenAccountBalance(publicKey)

    override suspend fun swap(
        path: DerivationPath,
        keys: List<String>,
        request: OrcaSwapRequest,
        accountA: Token.Active?,
        associatedAddress: PublicKey,
        shouldCreateAssociatedInstruction: Boolean
    ): String = withContext(Dispatchers.IO) {
        val owner = when (path) {
            DerivationPath.BIP44 -> Account.fromBip44Mnemonic(keys, 0)
            DerivationPath.BIP44CHANGE -> Account.fromBip44MnemonicWithChange(keys, 0)
            DerivationPath.BIP32DEPRECATED -> Account.fromBip32Mnemonic(keys, 0)
        }

        val tokenSwap = TokenSwap()

        return@withContext tokenSwap.swap(
            owner = owner,
            pool = request.pool,
            slippage = request.slippage,
            amountIn = request.amount,
            balanceA = request.balanceA,
            balanceB = request.balanceB,
            wrappedSolAccount = Token.WRAPPED_SOL_MINT.toPublicKey(),
            accountAddressA = accountA?.publicKey?.toPublicKey(),
            associatedAddress = associatedAddress,
            shouldCreateAssociatedInstruction = shouldCreateAssociatedInstruction,
            getAccountInfo = { rpcRepository.getAccountInfo(it) },
            getBalanceNeeded = { rpcRepository.getMinimumBalanceForRentExemption(it) },
            getRecentBlockhash = { rpcRepository.getRecentBlockhash().recentBlockhash },
            sendTransaction = { transaction ->
                rpcRepository.sendTransaction(transaction)
            }
        )
    }
}