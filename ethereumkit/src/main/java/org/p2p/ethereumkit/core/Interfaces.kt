package org.p2p.ethereumkit.core

import org.p2p.ethereumkit.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.api.jsonrpc.models.RpcBlock
import org.p2p.ethereumkit.api.jsonrpc.models.RpcTransaction
import org.p2p.ethereumkit.api.jsonrpc.models.RpcTransactionReceipt
import org.p2p.ethereumkit.api.models.AccountState
import org.p2p.ethereumkit.contracts.ContractEventInstance
import org.p2p.ethereumkit.contracts.ContractMethod
import org.p2p.ethereumkit.decorations.TransactionDecoration
import org.p2p.ethereumkit.models.*
import org.p2p.ethereumkit.spv.models.AccountStateSpv
import org.p2p.ethereumkit.spv.models.BlockHeader
import io.reactivex.Single
import org.p2p.ethereumkit.models.ProviderEip1155Transaction
import org.p2p.ethereumkit.models.ProviderEip721Transaction
import org.p2p.ethereumkit.models.ProviderInternalTransaction
import org.p2p.ethereumkit.models.ProviderTokenTransaction
import org.p2p.ethereumkit.models.ProviderTransaction
import java.math.BigInteger


interface IApiStorage {
    fun getLastBlockHeight(): Long?
    fun saveLastBlockHeight(lastBlockHeight: Long)

    fun getAccountState(): AccountState?
    fun saveAccountState(state: AccountState)
}

interface ISpvStorage {
    fun getLastBlockHeader(): BlockHeader?
    fun saveBlockHeaders(blockHeaders: List<BlockHeader>)
    fun getBlockHeadersReversed(fromBlockHeight: Long, limit: Int): List<BlockHeader>

    fun getAccountState(): AccountStateSpv?
    fun saveAccountSate(accountState: AccountStateSpv)
}

interface IBlockchain {
    val source: String
    var listener: IBlockchainListener?

    fun start()
    fun refresh()
    fun stop()
    fun syncAccountState()

    val syncState: EthereumKit.SyncState
    val lastBlockHeight: Long?
    val accountState: AccountState?

    fun send(rawTransaction: RawTransaction, signature: Signature): Single<Transaction>
    fun getNonce(defaultBlockParameter: DefaultBlockParameter): Single<Long>
    fun estimateGas(to: EthAddress?, amount: BigInteger?, gasLimit: Long?, gasPrice: GasPrice, data: ByteArray?): Single<Long>
    fun getTransactionReceipt(transactionHash: ByteArray): Single<RpcTransactionReceipt>
    fun getTransaction(transactionHash: ByteArray): Single<RpcTransaction>
    fun getBlock(blockNumber: Long): Single<RpcBlock>

    fun getLogs(address: EthAddress?, topics: List<ByteArray?>, fromBlock: Long, toBlock: Long, pullTimestamps: Boolean): Single<List<TransactionLog>>
    fun getStorageAt(contractAddress: EthAddress, position: ByteArray, defaultBlockParameter: DefaultBlockParameter): Single<ByteArray>
    fun call(contractAddress: EthAddress, data: ByteArray, defaultBlockParameter: DefaultBlockParameter): Single<ByteArray>

    fun <T> rpcSingle(rpc: JsonRpc<T>): Single<T>
}

interface IBlockchainListener {
    fun onUpdateLastBlockHeight(lastBlockHeight: Long)
    fun onUpdateSyncState(syncState: EthereumKit.SyncState)
    fun onUpdateAccountState(accountState: AccountState)
}

interface ITransactionStorage {
    fun getTransactions(hashes: List<ByteArray>): List<Transaction>
    fun getTransaction(hash: ByteArray): Transaction?
    fun getTransactionsBeforeAsync(tags: List<List<String>>, hash: ByteArray?, limit: Int?): Single<List<Transaction>>
    fun save(transactions: List<Transaction>)

    fun getPendingTransactions(): List<Transaction>
    fun getPendingTransactions(tags: List<List<String>>): List<Transaction>
    fun getNonPendingTransactionsByNonces(from: EthAddress, pendingTransactionNonces: List<Long>): List<Transaction>

    fun getLastInternalTransaction(): InternalTransaction?
    fun getInternalTransactions(): List<InternalTransaction>
    fun getInternalTransactionsByHashes(hashes: List<ByteArray>): List<InternalTransaction>
    fun saveInternalTransactions(internalTransactions: List<InternalTransaction>)

    fun saveTags(tags: List<TransactionTag>)
}

interface IEip20Storage {
    fun getLastEvent(): Eip20Event?
    fun save(events: List<Eip20Event>)
    fun getEvents(): List<Eip20Event>
    fun getEventsByHashes(hashes: List<ByteArray>): List<Eip20Event>
}

interface ITransactionSyncer {
    fun getTransactionsSingle(): Single<Pair<List<Transaction>, Boolean>>
}

interface IMethodDecorator {
    fun contractMethod(input: ByteArray): ContractMethod?
}

interface IEventDecorator {
    fun contractEventInstancesMap(transactions: List<Transaction>): Map<String, List<ContractEventInstance>>
    fun contractEventInstances(logs: List<TransactionLog>): List<ContractEventInstance>
}

interface ITransactionDecorator {
    fun decoration(
        from: EthAddress?,
        to: EthAddress?,
        value: BigInteger?,
        contractMethod: ContractMethod?,
        internalTransactions: List<InternalTransaction>,
        eventInstances: List<ContractEventInstance>
    ): TransactionDecoration?
}

interface ITransactionProvider {
    fun getTransactions(startBlock: Long): Single<List<ProviderTransaction>>
    fun getInternalTransactions(startBlock: Long): Single<List<ProviderInternalTransaction>>
    fun getInternalTransactionsAsync(hash: ByteArray): Single<List<ProviderInternalTransaction>>
    fun getTokenTransactions(startBlock: Long): Single<List<ProviderTokenTransaction>>
    fun getEip721Transactions(startBlock: Long): Single<List<ProviderEip721Transaction>>
    fun getEip1155Transactions(startBlock: Long): Single<List<ProviderEip1155Transaction>>
}
