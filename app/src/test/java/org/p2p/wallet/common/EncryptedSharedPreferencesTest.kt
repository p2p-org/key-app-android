package org.p2p.wallet.common

import androidx.core.content.edit
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.google.gson.Gson
import org.junit.Before
import org.junit.Test
import org.p2p.wallet.common.crypto.keystore.EncoderDecoder
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.utils.InMemorySharedPreferences
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.crypto.FakeAndroidKeyStoreProvider
import org.p2p.wallet.utils.crypto.FakeEncoderDecoder
import org.p2p.wallet.utils.generateRandomBytes
import org.p2p.wallet.utils.mnemoticgenerator.English

class EncryptedSharedPreferencesTest {
    private val keyStore = FakeAndroidKeyStoreProvider.getKeyStore()
    private val encoderDecoder: EncoderDecoder = FakeEncoderDecoder(keyStore)
    private val inMemorySharedPreferences = InMemorySharedPreferences()

    private val encryptedSharedPreferences = EncryptedSharedPreferences(
        keyStoreWrapper = KeyStoreWrapper(encoderDecoder, keyStore, inMemorySharedPreferences),
        sharedPreferences = inMemorySharedPreferences,
        gson = Gson()
    )

    @Before
    fun before() {
        inMemorySharedPreferences.edit { clear() }
        keyStore.aliases()
            .toList()
            .forEach(keyStore::deleteEntry)
    }

    @Test
    fun `GIVEN primitive data WHEN save and get THEN data is correct`() {
        // GIVEN
        val expectedString = "key_1" to "hello world"
        val expectedStringSet = "key_2" to setOf("hello_world")
        val expectedLong = "key_3" to 999L
        val expectedBoolean = "key_4" to true
        val expectedBytes = "key_5" to generateRandomBytes(16)

        // WHEN
        encryptedSharedPreferences.saveString(expectedString.first, expectedString.second)
        val actualString = encryptedSharedPreferences.getString(expectedString.first)

        encryptedSharedPreferences.saveStringSet(expectedStringSet.first, expectedStringSet.second)
        val actualStringSet = encryptedSharedPreferences.getStringSet(expectedStringSet.first)

        encryptedSharedPreferences.saveLong(expectedLong.first, expectedLong.second)
        val actualLong = encryptedSharedPreferences.getLong(expectedLong.first, -1)

        encryptedSharedPreferences.saveBoolean(expectedBoolean.first, expectedBoolean.second)
        val actualBoolean = encryptedSharedPreferences.getBoolean(expectedBoolean.first, false)

        encryptedSharedPreferences.saveBytes(expectedBytes.first, expectedBytes.second)
        val actualBytes = encryptedSharedPreferences.getBytes(expectedBytes.first)

        // THEN
        actualString.assertThat().isEqualTo(expectedString.second)
        actualStringSet.assertThat().isEqualTo(expectedStringSet.second)
        actualLong.assertThat().isEqualTo(expectedLong.second)
        actualBoolean.assertThat().isEqualTo(expectedBoolean.second)
        (actualBytes.contentEquals(expectedBytes.second)).assertThat().isTrue()
    }

    @Test
    fun `GIVEN valid object WHEN save and get THEN object is correct`() {
        // GIVEN
        val expectedObject = ValidObject(
            fieldString = "string",
            fieldInt = 1,
            fieldLong = 10L,
            fieldBoolean = true
        )
        // WHEN
        encryptedSharedPreferences.saveObject(expectedObject.fieldString, expectedObject)
        val actualObject = encryptedSharedPreferences.getObject(expectedObject.fieldString, ValidObject::class)

        // THEN
        actualObject.assertThat().isEqualTo(expectedObject)
    }

    @Test
    fun `GIVEN list of strings  WHEN save and get THEN list of strings are correct`() {
        // GIVEN
        val expectedStrings = English.INSTANCE.words.toList().take(12)
        // WHEN
        encryptedSharedPreferences.saveObjectList("words", expectedStrings)
        val actualObjects = encryptedSharedPreferences.getObjectList<String>("words")

        // THEN
        actualObjects.assertThat().isEqualTo(expectedStrings)
    }

    // getObjectList doesn't work properly
    @Test(expected = org.opentest4j.AssertionFailedError::class)
    fun `GIVEN valid list of objects WHEN save and get THEN list of objects is correct`() {
        // GIVEN
        val expectedObjects = buildList {
            repeat(3) {
                this += ValidObject(
                    fieldString = "string $it",
                    fieldInt = it,
                    fieldLong = 10L,
                    fieldBoolean = it == it.inc()
                )
            }
        }
        // WHEN
        encryptedSharedPreferences.saveObjectList(expectedObjects[0].fieldString, expectedObjects)
        val actualObjects = encryptedSharedPreferences.getObjectList<ValidObject>(expectedObjects[0].fieldString)

        // THEN
        actualObjects.assertThat().isEqualTo(expectedObjects)
    }

    private data class ValidObject(
        val fieldString: String,
        val fieldInt: Int,
        val fieldLong: Long,
        val fieldBoolean: Boolean
    )
}
