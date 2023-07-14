package org.p2p.wallet.user.interactor

import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.token.TokenMetadata
import org.p2p.core.token.TokensMetadataInfo
import org.p2p.token.service.model.UpdateTokenMetadataResult
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.wallet.common.storage.ExternalFile
import org.p2p.wallet.common.storage.ExternalStorageRepository
import org.p2p.wallet.user.repository.UserLocalRepository

@ExperimentalCoroutinesApi
internal class TokenMetadataInteractorTest {

    private val externalStorageRepository: ExternalStorageRepository = mockk()

    private val userLocalRepository: UserLocalRepository = mockk()

    private val metadataRepository: TokenMetadataRepository = mockk()

    private val gson: Gson = mockk()

    private lateinit var tokenMetadataInteractor: TokenMetadataInteractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        tokenMetadataInteractor = TokenMetadataInteractor(
            externalStorageRepository = externalStorageRepository,
            userLocalRepository = userLocalRepository,
            metadataRepository = metadataRepository,
            gson = gson
        )
    }

    @Test
    fun `GIVEN metadata WHEN backend not modified THEN use local file`() = runTest {
        val timestamp = "some timestamp"
        val file: ExternalFile = mockk()
        val metadata: TokensMetadataInfo = mockk()
        val tokens: List<TokenMetadata> = mockk()
        every { file.data } returns "metadata json"
        every { metadata.timestamp } returns timestamp
        every { metadata.tokens } returns tokens
        every { externalStorageRepository.readJsonFile(TOKENS_FILE_NAME) } returns file
        every { gson.fromJson(any<String>(), TokensMetadataInfo::class.java) } returns metadata
        every { userLocalRepository.setTokenData(tokens) } just Runs
        coEvery { metadataRepository.loadTokensMetadata(any()) } returns UpdateTokenMetadataResult.NoUpdate

        tokenMetadataInteractor.loadAllTokensData()

        verify { tokenMetadataInteractor.updateMemoryCache(tokens) }
    }

    @Test
    fun `GIVEN metadata WHEN metadata has changes THEN update local file and use a new one`() = runTest {
        val timestamp = "some timestamp"
        val file: ExternalFile = mockk()
        val tokens: List<TokenMetadata> = mockk()
        val metadata: TokensMetadataInfo = mockk()
        val metadataResult = UpdateTokenMetadataResult.NewMetadata(metadata)

        every { file.data } returns "metadata json"
        every { externalStorageRepository.readJsonFile(TOKENS_FILE_NAME) } returns file
        every { gson.fromJson(any<String>(), TokensMetadataInfo::class.java) } returns metadata
        every { metadata.timestamp } returns timestamp
        every { metadata.tokens } returns tokens
        every { userLocalRepository.setTokenData(any()) } returns Unit
        every { externalStorageRepository.saveJson(metadata, TOKENS_FILE_NAME) } returns Unit
        coEvery { metadataRepository.loadTokensMetadata(any()) } returns metadataResult

        tokenMetadataInteractor.loadAllTokensData()

        verify { tokenMetadataInteractor.updateMemoryCacheAndLocalFile(metadataResult) }
    }
}
