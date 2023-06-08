package org.p2p.wallet.striga.signup.ui

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.verify
import io.mockk.mockk
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeInMemoryRepository
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.PhoneNumberInputValidator
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.plantTimberToStdout
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.mutableListQueueOf

private val SupportedCountry = CountryCode(
    countryName = "United Kingdom",
    flagEmoji = "🇬🇧",
    nameCodeAlpha2 = "gb",
    nameCodeAlpha3 = "gbr",
    phoneCode = "",
    mask = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupFirstStepPresenterTest {

    private val currentWorkingDir = Paths.get("").toAbsolutePath().toString()
    private val assetsRoot = File(currentWorkingDir, "build/intermediates/assets/debug")
    private lateinit var countryCodeRepository: CountryCodeRepository

    @MockK(relaxed = true)
    lateinit var countryRepository: CountryCodeRepository

    @MockK(relaxed = true)
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository
    lateinit var interactor: StrigaSignupInteractor

    private val signupDataValidator = StrigaSignupDataValidator()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    init {
        plantTimberToStdout("StrigaSignupFirstStepPresenterTest")
        initCountryCodeLocalRepository()
    }

    private fun createPresenter(): StrigaSignUpFirstStepPresenter {
        return StrigaSignUpFirstStepPresenter(
            dispatchers = dispatchers,
            interactor = interactor,
            countryRepository = countryCodeRepository
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        interactor = StrigaSignupInteractor(
            appScope = appScope,
            validator = signupDataValidator,
            countryRepository = countryRepository,
            signupDataRepository = signupDataRepository,
        )
    }

    @Test
    fun `GIVEN initial state WHEN presenter created THEN check presenter loads and sets saved data`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val updatedFieldValueStates = mutableListQueueOf<String>()
        val updatedFieldTypeStates = mutableListQueueOf<StrigaSignupDataType>()
        verify(exactly = initialSignupData.size) {
            view.updateSignupField(
                capture(updatedFieldTypeStates),
                capture(updatedFieldValueStates)
            )
        }
        verify(exactly = 0) { view.setErrors(any()) }
        verify(exactly = 0) { view.clearErrors() }
        verify(exactly = 0) { view.setButtonIsEnabled(any()) }
        verify(exactly = 0) { view.navigateNext() }

        val resultSignupData = updatedFieldTypeStates.mapIndexed { index, strigaSignupDataType ->
            StrigaSignupData(strigaSignupDataType, updatedFieldValueStates[index])
        }

        assertEquals(initialSignupData, resultSignupData)

        presenter.saveChanges()
        presenter.detach()
    }

    /**
     * Don't show errors until pressed next button
     */
    @Test
    fun `GIVEN invalid user data WHEN input changed with wrong value THEN check nothing happens`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged("123", StrigaSignupDataType.PHONE_NUMBER)
        advanceUntilIdle()

        verify(exactly = 0) { view.setErrors(any()) }
        verify(exactly = 0) { view.setButtonIsEnabled(false) }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN invalid user data WHEN next clicked THEN check errors are shown`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged("123", StrigaSignupDataType.PHONE_NUMBER)
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.setErrors(any()) }
        verify(exactly = 1) { view.setButtonIsEnabled(false) }
        verify(exactly = 1) { view.scrollToFirstError(any()) }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN valid user data WHEN next clicked THEN check we go to next screen`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        interactor.addValidator(PhoneNumberInputValidator("90", "5556667788", countryCodeRepository))
        presenter.onFieldChanged("+90 555 666 77 88", StrigaSignupDataType.PHONE_NUMBER)
        presenter.onFieldChanged("+90", StrigaSignupDataType.PHONE_CODE_WITH_PLUS)
        presenter.onFieldChanged("Vasya", StrigaSignupDataType.FIRST_NAME)
        presenter.onFieldChanged("Pupkin", StrigaSignupDataType.LAST_NAME)
        presenter.onFieldChanged("10.10.2010", StrigaSignupDataType.DATE_OF_BIRTH)
        presenter.onCountryChanged(CountryCode("France", "emoji", "fr", "fra", ","))
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.navigateNext() }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN selected country WHEN country chosen THEN check view updates country`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val chosenCountry = CountryCode("Turkey", "\uD83C\uDDF9\uD83C\uDDF7", "TR", "TUR", "", "996")

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onCountryChanged(chosenCountry)
        advanceUntilIdle()

        verify(exactly = 1) {
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3,
                "${chosenCountry.flagEmoji} ${chosenCountry.countryName}"
            )
        }
    }

    @Test
    fun `GIVEN initial state with saved data WHEN presenter created THEN check country shows to user`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.nameCodeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        verify {
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3,
                SupportedCountry.nameCodeAlpha3
            )
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3,
                "${SupportedCountry.flagEmoji} ${SupportedCountry.countryName}"
            )
        }
    }

    @Test
    fun `GIVEN invalid user input WHEN user inputs new data THEN check errors are cleared`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.nameCodeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onFieldChanged("123", StrigaSignupDataType.PHONE_NUMBER)
        presenter.onSubmit()
        presenter.onFieldChanged("123", StrigaSignupDataType.PHONE_NUMBER)
        advanceUntilIdle()

        verify { view.setErrors(any()) }
        verify { view.setButtonIsEnabled(any()) }
        verify { view.clearError(StrigaSignupDataType.PHONE_NUMBER) }
    }

    private fun initCountryCodeLocalRepository() {
        val assetManager: AssetManager = mockk {
            every { open(any()) } answers {
                val file = File(assetsRoot, arg<String>(0))
                if (!file.exists()) throw IllegalStateException("File not found: ${file.absolutePath}")
                file.inputStream()
            }
        }

        val context = mockk<Context> {
            every { assets } returns assetManager
        }
        val resources = mockk<Resources> {
            every { openRawResource(R.raw.ccp_english) } returns readCountriesXmlFile()
        }
        val phoneNumberUtil = PhoneNumberUtil.createInstance(context)

        val parser = CountryCodeXmlParser(resources, phoneNumberUtil)

        countryCodeRepository = CountryCodeInMemoryRepository(
            dispatchers = dispatchers,
            context = context,
            countryCodeHelper = parser
        )
    }

    private fun readCountriesXmlFile(): InputStream {
        val ccpEnglishFile = File(currentWorkingDir, "src/main/res/raw/ccp_english.xml")
        Assert.assertTrue(ccpEnglishFile.exists())

        return ccpEnglishFile.inputStream()
    }
}
