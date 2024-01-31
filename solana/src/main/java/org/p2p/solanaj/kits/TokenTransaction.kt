package org.p2p.solanaj.kits

import android.util.Base64
import org.p2p.core.crypto.Base64Utils
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.PublicKey.Companion.findProgramAddress
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData
import org.p2p.solanaj.programs.TokenProgram.MintData
import org.p2p.solanaj.rpc.RpcException

object TokenTransaction {

    fun getAccountInfoData(
        accountInfo: AccountInfo?,
        programId: PublicKey
    ): AccountInfoData {
        if (accountInfo?.value == null) {
            throw RpcException("Failed to find account")
        }
        require(accountInfo.value.owner == programId.toString()) { "Invalid account owner" }
        val base64Data = accountInfo.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)
        return AccountInfoData.decode(data)
    }

    @JvmStatic
    @Throws(RpcException::class)
    fun getMintData(accountInfo: AccountInfo?, programId: PublicKey): MintData? {
        if (accountInfo?.value == null) {
            throw RpcException("Failed to find mint account")
        }
        require(accountInfo.value.owner == programId.toString()) { "Invalid mint owner" }
        val base64Data = accountInfo.value.data!![0]
        val data: ByteArray = Base64Utils.decode(base64Data)
        return MintData.decode(data)
    }

    fun parseAccountInfoData(
        accountInfo: AccountInfo?,
        programId: PublicKey
    ): AccountInfoData? {
        if (accountInfo?.value == null) return null

        val isValid = accountInfo.value.owner == programId.toString()
        if (!isValid) return null

        val base64Data = accountInfo.value.data!![0]
        val data = Base64.decode(base64Data, Base64.DEFAULT)
        return AccountInfoData.decode(data)
    }

    fun decodeAccountInfo(info: AccountInfo?): AccountInfoData? {
        if (info == null) return null

        val base64Data = info.value?.data?.get(0)
        if (base64Data.isNullOrEmpty()) return null

        val data = Base64.decode(base64Data, Base64.DEFAULT)
        return AccountInfoData.decode(data)
    }

    fun getMultipleAccounts(
        accounts: MultipleAccountsInfo,
        keys: List<String>
    ): MultipleAccountsInfo {
        val parsedAccounts: MutableList<AccountInfoParsed> = ArrayList()
        for (i in accounts.accountsInfoParsed.indices) {
            val current = accounts.accountsInfoParsed[i]
            current.address = keys[i]
            parsedAccounts.add(current)
        }
        accounts.accountsInfoParsed = parsedAccounts
        return accounts
    }

    @Throws(Exception::class)
    fun getAssociatedTokenAddress(
        mint: PublicKey,
        owner: PublicKey,
        programId: PublicKey = TokenProgram.PROGRAM_ID,
    ): PublicKey {
        return findProgramAddress(
            listOf(owner.asByteArray(), programId.asByteArray(), mint.asByteArray()),
            TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID
        )
            .address
    }
}
