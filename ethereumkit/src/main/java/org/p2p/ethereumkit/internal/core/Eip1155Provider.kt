package org.p2p.ethereumkit.internal.core

import org.p2p.core.rpc.IRpcApiProvider
import org.p2p.ethereumkit.internal.api.core.NodeApiProvider
import org.p2p.ethereumkit.internal.api.core.RpcBlockchain
import org.p2p.ethereumkit.internal.contracts.ContractMethod
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter
import org.p2p.ethereumkit.internal.models.RpcSource
import org.p2p.ethereumkit.internal.spv.core.toBigInteger
import io.reactivex.Single
import java.math.BigInteger

class Eip1155Provider(
    private val provider: IRpcApiProvider
) {

    class BalanceOfMethod(val owner: EthAddress, val tokenId: BigInteger) : ContractMethod() {
        override val methodSignature = "balanceOf(address,uint256)"
        override fun getArguments() = listOf(owner, tokenId)
    }

    fun getTokenBalance(contractAddress: EthAddress, tokenId: BigInteger, address: EthAddress): Single<BigInteger> {
        val callRpc = RpcBlockchain.callRpc(contractAddress, BalanceOfMethod(address, tokenId).encodedABI(), DefaultBlockParameter.Latest)

        return provider
            .single(callRpc)
            .map { it.sliceArray(IntRange(0, 31)).toBigInteger() }
    }

    companion object {

        fun instance(rpcSource: RpcSource.Http): Eip1155Provider {
            val apiProvider = NodeApiProvider(rpcSource.urls, EthereumKit.gson, rpcSource.auth)

            return Eip1155Provider(apiProvider)
        }

    }

}
