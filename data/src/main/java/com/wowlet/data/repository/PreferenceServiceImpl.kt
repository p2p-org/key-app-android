package com.wowlet.data.repository

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.util.cipher.Cipher.Companion.decrypt
import com.wowlet.data.util.cipher.Cipher.Companion.encrypt
import com.wowlet.data.util.cipher.Cipher.Companion.generateSecretKeyCipher
import com.wowlet.data.util.cipher.Cipher.Companion.getSecretKey
import com.wowlet.data.util.cipher.Cipher.Companion.strSecretKey
import com.wowlet.entities.local.*
import java.io.*
import java.lang.reflect.Type

class PreferenceServiceImpl(val context: Context) : PreferenceService {

    private val authenticationKey = "authenticationKeys"
    private val pinCodeKey = "pinCodeKey"
    private val allowNotificationKey = "allowNotificationKey"
    private val fingerPrintPKey = "fingerPrintPKey"
    private val finishRegKey = "finishRegKey"

    val sharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)

    override fun setPinCodeValue(codeValue: PinCodeData): Boolean = put(codeValue, pinCodeKey)


    override fun getPinCodeValue(): PinCodeData? =
        get<PinCodeData>(pinCodeKey)

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

    override fun isCurrentLoginReg(): Boolean = finishReg

    override fun finishLoginReg(finishReg: Boolean) {
        this.finishReg = finishReg
    }


    private var finishReg: Boolean
        get() = sharedPreferences.getBoolean(finishRegKey, false)
        set(enable) = sharedPreferences.edit().putBoolean(finishRegKey, enable)
            .apply()

    inline fun <reified T> get(key: String): T? {
        val value = sharedPreferences.getString(key, null)
        return value?.let {
            val jsonAdapter: JsonAdapter<T> =
                Moshi.Builder().build().adapter(T::class.java)
            jsonAdapter.fromJson(it)
        }
    }

    inline fun <reified T> put(data: T, key: String): Boolean {
        val jsonAdapter: JsonAdapter<T> =
            Moshi.Builder().build().adapter(T::class.java)
        val jsonString = jsonAdapter.toJson(data)
        return sharedPreferences.edit().putString(key, jsonString).commit()
    }

    override fun setWalletItem(userData: UserSecretData) {

        val listMyData: Type =
            Types.newParameterizedType(MutableList::class.java, UserSecretData::class.java)
        val jsonAdapter: JsonAdapter<MutableList<UserSecretData>> =
            Moshi.Builder().build().adapter(listMyData)
        val userDataList = getWalletList()?.apply {
            add(userData)
        } ?: mutableListOf(userData)
        val secretDataByteArray = jsonAdapter.toJson(userDataList).toByteArray()
        val generateSecretKey = generateSecretKeyCipher()
        val encryptData = encrypt(secretDataByteArray, generateSecretKey)
        val strSecretKey = strSecretKey(generateSecretKey)
        val cipherData = CipherData(encryptData, strSecretKey)
        val jsonAdapterCipherData: JsonAdapter<CipherData> =
            Moshi.Builder().build().adapter(CipherData::class.java)
        val cipherDataByteArray = jsonAdapterCipherData.toJson(cipherData).toByteArray()

        saveFile(cipherDataByteArray)
    }

    override fun setSingleWalletData(userData: UserSecretData) {

        val jsonAdapterSecretData: JsonAdapter<UserSecretData> =
            Moshi.Builder().build().adapter(UserSecretData::class.java)
        val secretDataByteArray = jsonAdapterSecretData.toJson(userData).toByteArray()
        val generateSecretKey = generateSecretKeyCipher()
        val encryptData = encrypt(secretDataByteArray, generateSecretKey)
        val strSecretKey = strSecretKey(generateSecretKey)
        val cipherData = CipherData(encryptData, strSecretKey)
        val jsonAdapterCipherData: JsonAdapter<CipherData> =
            Moshi.Builder().build().adapter(CipherData::class.java)
        val cipherDataByteArray = jsonAdapterCipherData.toJson(cipherData).toByteArray()

        saveFile(cipherDataByteArray)
    }

    override fun getSingleWalletData(): UserSecretData? {

        loadFile()?.let {
            val jsonAdapterCipherData: JsonAdapter<CipherData> =
                Moshi.Builder().build().adapter(CipherData::class.java)
            val cipherData = jsonAdapterCipherData.fromJson(it)
            val strSecretKey = cipherData?.strSecretKey
            val secretKey = getSecretKey(strSecretKey)
            val decryptData = decrypt(cipherData?.userSecretData, secretKey)
            val jsonAdapterSecretData: JsonAdapter<UserSecretData> =
                Moshi.Builder().build().adapter(UserSecretData::class.java)

            decryptData?.let {
                val userSecretData = jsonAdapterSecretData.fromJson(decryptData)
                return userSecretData
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


    override fun updateWallet(userSecretData: UserSecretData) :Boolean {
        val userDataList = getSingleWalletData()?.apply {
            if (this.publicKey == userSecretData.publicKey) {
                this.phrase = userSecretData.phrase
                this.secretKey = userSecretData.secretKey
                this.publicKey = userSecretData.publicKey
            }

        }
        userDataList?.let {
            setSingleWalletData(it)
        }?:setSingleWalletData(userSecretData)

        /* userDataList?.run {
             val secretDataByteArray = jsonAdapter.toJson(this).toByteArray()
             val generateSecretKey = generateSecretKeyCipher()
             val encryptData = encrypt(secretDataByteArray, generateSecretKey)
             val strSecretKey = strSecretKey(generateSecretKey)
             val cipherData = CipherData(encryptData, strSecretKey)
             val jsonAdapterCipherData: JsonAdapter<CipherData> =
                 Moshi.Builder().build().adapter(CipherData::class.java)
             val cipherDataByteArray = jsonAdapterCipherData.toJson(cipherData).toByteArray()

             saveFile(cipherDataByteArray)
         }*/
        return true
    }

    override fun getWalletList(): MutableList<UserSecretData>? {

        loadFile()?.let {
            val jsonAdapterCipherData: JsonAdapter<CipherData> =
                Moshi.Builder().build().adapter(CipherData::class.java)
            val cipherData = jsonAdapterCipherData.fromJson(it)
            val strSecretKey = cipherData?.strSecretKey
            val secretKey = getSecretKey(strSecretKey)
            val decryptData = decrypt(cipherData?.userSecretData, secretKey)

            val listMyData: Type =
                Types.newParameterizedType(MutableList::class.java, UserSecretData::class.java)
            val adapter: JsonAdapter<MutableList<UserSecretData>> =
                Moshi.Builder().build().adapter(listMyData)
            decryptData?.let { data ->
                return adapter.fromJson(data)
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



