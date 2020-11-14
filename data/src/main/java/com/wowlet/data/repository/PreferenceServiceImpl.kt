package com.wowlet.data.repository

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.util.cipher.Cipher.Companion.decrypt
import com.wowlet.data.util.cipher.Cipher.Companion.encrypt
import com.wowlet.data.util.cipher.Cipher.Companion.generateSecretKeyCipher
import com.wowlet.data.util.cipher.Cipher.Companion.getSecretKey
import com.wowlet.data.util.cipher.Cipher.Companion.strSecretKey
import com.wowlet.entities.local.CipherData
import com.wowlet.entities.local.PinCodeData
import com.wowlet.entities.local.UserSecretData
import java.io.*


class PreferenceServiceImpl(val context: Context) : PreferenceService {

    private val authenticationKey = "authenticationKeys"
    private val pinCodeKey = "pinCodeKey"
    var pinCodeTemp: Int = 0
    val sharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)

    override fun setPinCodeValue(codeValue: PinCodeData) {
        // pinCode = codeValue
        put<PinCodeData>(codeValue, pinCodeKey)
    }

    override fun getPinCodeValue(): PinCodeData? = get<PinCodeData>(pinCodeKey)

    override fun enableNotification(isEnable: Boolean) {
        allowNotification = isEnable
    }

    override fun isAllowNotification(): Boolean = allowNotification

    override fun getSecretData(): UserSecretData? = get<UserSecretData>(authenticationKey)

    override fun setSecretData(userData: UserSecretData) {
        put<UserSecretData>(userData, authenticationKey)
    }

    var pinCode: Int
        get() = sharedPreferences.getInt(pinCodeKey, 0)
        set(accessToken) = sharedPreferences.edit().putInt(pinCodeKey, accessToken)
            .apply()

    var allowNotification: Boolean
        get() = sharedPreferences.getBoolean(pinCodeKey, false)
        set(enable) = sharedPreferences.edit().putBoolean(pinCodeKey, enable)
            .apply()

    inline fun <reified T> get(key: String): T? {
        val value = sharedPreferences.getString(key, null)
        return value?.let {
            val jsonAdapter: JsonAdapter<T> =
                Moshi.Builder().build().adapter(T::class.java)
            jsonAdapter.fromJson(it)
        }
    }

    inline fun <reified T> put(data: T, key: String) {
        val jsonAdapter: JsonAdapter<T> =
            Moshi.Builder().build().adapter(T::class.java)
        val jsonString = jsonAdapter.toJson(data)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    override fun setSecretDataInFile(userData: UserSecretData) {

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

    override fun getSecretDataAtFile(): UserSecretData? {

        loadFile()?.let {
            val jsonAdapterCipherData: JsonAdapter<CipherData> =
                Moshi.Builder().build().adapter(CipherData::class.java)
            val cipherData = jsonAdapterCipherData.fromJson(it)
            val strSecretKey = cipherData?.strSecretKey
            val secretKey = getSecretKey(strSecretKey)
            val decryptData = decrypt(cipherData?.userSecretData, secretKey)
            val jsonAdapterSecretData: JsonAdapter<UserSecretData> =
                Moshi.Builder().build().adapter(UserSecretData::class.java)

            decryptData?.let { data ->
                return jsonAdapterSecretData.fromJson(data)
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