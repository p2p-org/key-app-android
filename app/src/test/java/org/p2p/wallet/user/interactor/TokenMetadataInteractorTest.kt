package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.core.utils.emptyString
import org.p2p.token.service.model.UpdateTokenMetadataResult
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.wallet.common.storage.ExternalFile
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.coVerifyNone
import org.p2p.wallet.utils.coVerifyOnce
import org.p2p.wallet.utils.verifyOnce

@ExperimentalCoroutinesApi
internal class TokenMetadataInteractorTest {

    private val externalStorageRepository: ExternalStorageRepository = mockk(relaxed = true)

    private val userLocalRepository: UserLocalRepository = mockk(relaxed = true)

    private val metadataRepository: TokenMetadataRepository = mockk()

    private val gson: Gson = mockk()

    private lateinit var tokenMetadataInteractor: TokenMetadataInteractor

    private val metadataJson: String = """{
      "timestamp": "11.11.2011",
      "tokens": []
    }
    """.trimIndent()

    @Before
    fun setUp() {
        tokenMetadataInteractor = TokenMetadataInteractor(
            externalStorageRepository = externalStorageRepository,
            userLocalRepository = userLocalRepository,
            metadataRepository = metadataRepository,
            gson = gson
        )
    }

    @Test
    fun `GIVEN absent json file WHEN loadAllTokensData THEN cache updated and create file`() = runTest {
        // GIVEN
        val expectedMetadata = TokensMetadataInfo(emptyString(), emptyList())
        coEvery { externalStorageRepository.readJsonFile(any()) }.returns(null)
        coEvery { metadataRepository.loadSolTokensMetadata(any()) }.returns(
            UpdateTokenMetadataResult.NewMetadata(
                expectedMetadata
            )
        )

        // WHEN
        tokenMetadataInteractor.loadAllTokensMetadata()

        // THEN
        verifyOnce { userLocalRepository.setTokenData(expectedMetadata.tokens) }
        coVerifyOnce { externalStorageRepository.saveAsJsonFile(expectedMetadata, any()) }
    }

    @Test
    fun `GIVEN existent json file and NoUpdate WHEN loadAllTokensData THEN cache is not updated`() = runTest {
        // GIVEN
        val expectedMetadata = TokensMetadataInfo(emptyString(), emptyList())

        every { gson.fromJson<TokensMetadataInfo>(any<JsonReader>(), TokensMetadataInfo::class.java) }.returns(expectedMetadata)
        coEvery { externalStorageRepository.readJsonFileAsStream(any()) }.returns(mockk())
        coEvery { metadataRepository.loadSolTokensMetadata(any()) }.returns(UpdateTokenMetadataResult.NoUpdate)

        // WHEN
        tokenMetadataInteractor.loadAllTokensMetadata()

        // THEN
        coVerifyOnce { userLocalRepository.setTokenData(expectedMetadata.tokens) }
        coVerifyNone { externalStorageRepository.saveAsJsonFile(expectedMetadata, any()) }
    }

    @Test
    fun `GIVEN Error from remote WHEN loadAllTokensData THEN nothing happens`() = runTest {
        // GIVEN
        coEvery { externalStorageRepository.readJsonFile(any()) }.returns(null)
        coEvery { metadataRepository.loadSolTokensMetadata(any()) }.returns(UpdateTokenMetadataResult.Error(Throwable()))

        // WHEN
        tokenMetadataInteractor.loadAllTokensMetadata()

        // THEN
        coVerifyNone { userLocalRepository.setTokenData(any()) }
        coVerifyNone { externalStorageRepository.saveAsJsonFile(any<TokensMetadataInfo>(), any()) }
    }

    @Test
    fun `GIVEN error from IO THEN method throws and cache is not updated`() = runTest {
        // GIVEN
        coEvery { externalStorageRepository.readJsonFile(any()) }.throws(IOException("Illegal access denied"))

        // THEN
        coVerifyNone { userLocalRepository.setTokenData(any()) }
        coVerifyNone { externalStorageRepository.saveAsJsonFile(any<TokensMetadataInfo>(), any()) }
    }

    @Test
    fun `GIVEN error from gson THEN method throws and cache is not updated`() = runTest {
        // GIVEN
        coEvery { externalStorageRepository.readJsonFile(any()) }.returns(ExternalFile(metadataJson))
        every { gson.fromJson(metadataJson, TokensMetadataInfo::class.java) }.throws(JsonParseException("Unknown symbol found"))

        // THEN
        coVerifyNone { tokenMetadataInteractor.loadAllTokensMetadata() }
        coVerifyNone { userLocalRepository.setTokenData(any()) }
        coVerifyNone { externalStorageRepository.saveAsJsonFile(any<TokensMetadataInfo>(), any()) }
    }
}
