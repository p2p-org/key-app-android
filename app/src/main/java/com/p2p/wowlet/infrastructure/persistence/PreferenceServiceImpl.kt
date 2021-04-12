package com.p2p.wowlet.infrastructure.persistence

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.p2p.wowlet.dashboard.model.SelectedCurrency
import com.p2p.wowlet.dashboard.model.local.CipherData
import com.p2p.wowlet.dashboard.model.local.EnableFingerPrintModel
import com.p2p.wowlet.dashboard.model.local.EnableNotificationModel
import com.p2p.wowlet.dashboard.model.local.PinCodeResponse
import com.p2p.wowlet.dashboard.model.local.UserSecretData
import com.p2p.wowlet.dashboard.model.local.WalletItem
import com.p2p.wowlet.utils.cipher.Cipher.Companion.decrypt
import com.p2p.wowlet.utils.cipher.Cipher.Companion.encrypt
import com.p2p.wowlet.utils.cipher.Cipher.Companion.generateSecretKeyCipher
import com.p2p.wowlet.utils.cipher.Cipher.Companion.getSecretKey
import com.p2p.wowlet.utils.cipher.Cipher.Companion.strSecretKey
import org.p2p.solanaj.rpc.Cluster
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

@Deprecated("Workaround, remove unnecessary data storing logic, remove interfaces")
class PreferenceServiceImpl(
    private val context: Context,
    val gson: Gson,
    val sharedPreferences: SharedPreferences
) : PreferenceService {

    private val authenticationKey = "authenticationKeys"
    private val pinCodeKey = "pinCodeKey"
    private val allowNotificationKey = "allowNotificationKey"
    private val fingerPrintPKey = "fingerPrintPKey"
    private val finishRegKey = "finishRegKey"
    private val walletItemKey = "walletItemKey"
    private val networkItemKey = "networkItemKey"
    private val selectedCurrencyKey = "selectedCurrencyKey"

    override fun setPinCodeValue(codeValue: PinCodeResponse): Boolean = put(codeValue, pinCodeKey)

    override fun getPinCodeValue(): PinCodeResponse? =
        get<PinCodeResponse>(pinCodeKey)

    override fun enableNotification(isEnable: EnableNotificationModel) {
        put(isEnable, allowNotificationKey)
    }

    override fun isAllowNotification(): EnableNotificationModel? =
        get<EnableNotificationModel>(allowNotificationKey)

    override fun isSetFingerPrint(): EnableFingerPrintModel? =
        get<EnableFingerPrintModel>(fingerPrintPKey)

    override fun enableFingerPrint(isEnable: EnableFingerPrintModel) {
        put(isEnable, fingerPrintPKey)
    }

    override fun isCurrentLoginReg(): Boolean =
        sharedPreferences.getBoolean(finishRegKey, false)

    override fun finishLoginReg(finishReg: Boolean) {
        sharedPreferences.edit { putBoolean(finishRegKey, finishReg) }
    }

    override fun setWalletItemData(walletItem: WalletItem?) {
        put(walletItem, walletItemKey)
    }

    override fun getWalletItemData(): WalletItem? = get(walletItemKey)

    override fun setSelectedNetWork(cluster: Cluster) {
        put(cluster, networkItemKey)
    }

    override fun getSelectedCluster(): Cluster = get(networkItemKey) ?: Cluster.MAINNET

    override fun setSelectedCurrency(currency: SelectedCurrency) {
        put(currency, selectedCurrencyKey)
    }

    override fun getSelectedCurrency(): SelectedCurrency? = get(selectedCurrencyKey)

    // todo remove all this
    inline fun <reified T> get(key: String): T? {
        val value = sharedPreferences.getString(key, null)
        return value?.let {
            gson.fromJson(it, T::class.java)
        }
    }

    inline fun <reified T> put(data: T, key: String): Boolean {
        val result = gson.toJson(data)
        return sharedPreferences.edit().putString(key, result).commit()
    }

    override fun setWalletItem(userData: UserSecretData) {

        val userDataList = getWalletList()?.apply {
            add(userData)
        } ?: mutableListOf(userData)
        val secretDataByteArray = gson.toJson(userDataList).toByteArray()
        val generateSecretKey = generateSecretKeyCipher()
        val encryptData = encrypt(secretDataByteArray, generateSecretKey)
        val strSecretKey = strSecretKey(generateSecretKey)
        val cipherData = CipherData(encryptData, strSecretKey)
        val cipherDataByteArray = gson.toJson(cipherData).toByteArray()

        saveFile(cipherDataByteArray)
    }

    override fun setSingleWalletData(userData: UserSecretData) {

        val secretDataByteArray = gson.toJson(userData).toByteArray()
        val generateSecretKey = generateSecretKeyCipher()
        val encryptData = encrypt(secretDataByteArray, generateSecretKey)
        val strSecretKey = strSecretKey(generateSecretKey)
        val cipherData = CipherData(encryptData, strSecretKey)
        val cipherDataByteArray = gson.toJson(cipherData).toByteArray()

        saveFile(cipherDataByteArray)
    }

    override fun getSingleWalletData(): UserSecretData? {

        loadFile()?.let {
            val cipherData = gson.fromJson(it, CipherData::class.java)
            val strSecretKey = cipherData?.strSecretKey
            val secretKey = getSecretKey(strSecretKey)
            val decryptData = decrypt(cipherData?.userSecretData, secretKey)

            decryptData?.let {
                return gson.fromJson(decryptData, UserSecretData::class.java)
            }
        }

        return null
    }

    override fun getActiveWallet(): UserSecretData? {
        val walletAccount = getSingleWalletData()
        walletAccount?.let {
            if (it.secretKey != "")
                return walletAccount
        }
        return null
    }

    override fun updateWallet(userSecretData: UserSecretData): Boolean {
        setSingleWalletData(userSecretData)
        return true
    }

    override fun getWalletList(): MutableList<UserSecretData>? {

        loadFile()?.let {
            val cipherData = gson.fromJson(it, CipherData::class.java)
            val strSecretKey = cipherData?.strSecretKey
            val secretKey = getSecretKey(strSecretKey)
            val decryptData = decrypt(cipherData?.userSecretData, secretKey)

            decryptData?.let { data ->
                return gson.fromJson(data, object : TypeToken<List<UserSecretData>>() {}.type)
            }
        }

        return null
    }

    private fun saveFile(data: ByteArray) {
        val FILE_NAME = "wowlet.txt"
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)

            fos.write(data)

            Log.i("Saved to", "saveFile: " + context.filesDir.toString() + "/" + FILE_NAME)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadFile(): String? {
        val FILE_NAME = "wowlet.txt"
        var fis: FileInputStream? = null
        try {
            fis = context.openFileInput(FILE_NAME)
            val isr = InputStreamReader(fis)
            val br = BufferedReader(isr)
            val sb = StringBuilder()
            var text: String?
            while (br.readLine().also { text = it } != null) {
                sb.append(text).append("\n")
            }
            return sb.toString()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}