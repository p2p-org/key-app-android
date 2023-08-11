package org.p2p.wallet.striga.signup.ui

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeInMemoryRepository
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.inapp.StrigaSimulateWeb3Flag
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.steps.first.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.steps.first.StrigaSignUpFirstStepPresenter
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.steps.validation.PhoneNumberInputValidator
import org.p2p.wallet.striga.signup.steps.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.mutableListQueueOf

private val SupportedCountry = CountryCode(
    countryName = "United Kingdom",
    flagEmoji = "🇬🇧",
    nameCodeAlpha2 = "gb",
    nameCodeAlpha3 = "gbr",
    phoneCode = "",
    mask = ""
)

private val TurkeyCountry = CountryCode(
    countryName = "Turkey",
    flagEmoji = "\uF1F9\uF1F7",
    nameCodeAlpha2 = "TR",
    nameCodeAlpha3 = "TUR",
    phoneCode = "90",
    mask = " 212 345 67 89"
)

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupFirstStepPresenterTest {

    private val currentWorkingDir = Paths.get("").toAbsolutePath().toString()
    private val assetsRoot = File(currentWorkingDir, "build/intermediates/assets/debug")
    private lateinit var countryCodeRepository: CountryCodeRepository

    @MockK(relaxed = true)
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    @MockK
    lateinit var userInteractor: StrigaUserInteractor

    @MockK
    lateinit var metadataInteractor: MetadataInteractor

    @MockK(relaxed = true)
    lateinit var inAppFeatureFlags: InAppFeatureFlags

    lateinit var interactor: StrigaSignupInteractor

    private val signupDataValidator = StrigaSignupDataValidator()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    init {
        initCountryCodeLocalRepository()
    }

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "StrigaSignupFirstStepPresenterTest"
        )
    }

    private fun createPresenter(): StrigaSignUpFirstStepPresenter {
        return StrigaSignUpFirstStepPresenter(
            dispatchers = dispatchers,
            interactor = interactor,
            countryRepository = countryCodeRepository,
            metadataInteractor = metadataInteractor,
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val simulateWeb3Flag = mockk<StrigaSimulateWeb3Flag>(relaxed = true) {
            every { featureValue } returns false
        }
        val simulateUserCreateFlag = mockk<StrigaSimulateWeb3Flag>(relaxed = true) {
            every { featureValue } returns false
        }
        every { inAppFeatureFlags.strigaSimulateWeb3Flag } returns simulateWeb3Flag
        every { inAppFeatureFlags.strigaSimulateWeb3Flag } returns simulateUserCreateFlag

        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "",
            customSharePhoneNumberE164 = "+905348558899",
            socialShareOwnerEmail = "email@email.email",
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )
        interactor = spyk(
            StrigaSignupInteractor(
                appScope = appScope,
                inAppFeatureFlags = inAppFeatureFlags,
                validator = signupDataValidator,
                countryCodeRepository = countryCodeRepository,
                signupDataRepository = signupDataRepository,
                userInteractor = userInteractor,
                metadataInteractor = metadataInteractor,
                strigaOtpConfirmInteractor = mockk(relaxed = true),
                strigaUserStatusRepository = mockk(relaxed = true),
                strigaPresetDataLocalRepository = mockk(relaxed = true)
            )
        )
    }

    @Test
    fun `GIVEN initial state WHEN presenter created THEN check presenter loads and sets saved data`() = runTest {
        val expectedEmail = "email@email.email"
        val expectedPhoneCode = "+90"
        val expectedPhoneNumber = "5348558899"
        val initialSignupData = listOf<StrigaSignupData>()
        val expectedSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, expectedEmail),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, expectedPhoneNumber)
        )
        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "",
            customSharePhoneNumberE164 = "${expectedPhoneCode}$expectedPhoneNumber",
            socialShareOwnerEmail = expectedEmail,
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val updatedFieldValueStates = mutableListQueueOf<String>()
        val updatedFieldTypeStates = mutableListQueueOf<StrigaSignupDataType>()
        // exactly 2 - means presenter set email and phone from metadata
        verify(exactly = 2) {
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

        assertEquals(expectedSignupData, resultSignupData)

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

        presenter.onFieldChanged(StrigaSignupDataType.PHONE_NUMBER, "123")
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

        presenter.onFieldChanged(StrigaSignupDataType.PHONE_NUMBER, "123")
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.setErrors(any()) }
        verify(exactly = 1) { view.setButtonIsEnabled(false) }
        verify(exactly = 1) { view.scrollToFirstError(any()) }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN valid user data WHEN next clicked THEN check we go to the next screen`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "",
            customSharePhoneNumberE164 = "",
            socialShareOwnerEmail = "email@email.email",
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        interactor.setPhoneValidator(PhoneNumberInputValidator("90", countryCodeRepository))
        presenter.onPhoneCountryCodeChanged(
            CountryCode("tr", "tur", "90", "Turkey", ","),
            true
        )
        presenter.onPhoneNumberChanged("5556667788")
        presenter.onFieldChanged(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "+90")
        presenter.onFieldChanged(StrigaSignupDataType.FIRST_NAME, "Vasya")
        presenter.onFieldChanged(StrigaSignupDataType.LAST_NAME, "Pupkin")
        presenter.onFieldChanged(StrigaSignupDataType.DATE_OF_BIRTH, "10.10.2010")
        presenter.onCountryOfBirthdayChanged(
            CountryCode("France", "emoji", "fr", "fra", ",")
        )
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
        presenter.onCountryOfBirthdayChanged(chosenCountry)
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
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry
        coEvery { countryCodeRepository.detectCountryOrDefault() } returns SupportedCountry

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
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onFieldChanged(StrigaSignupDataType.FIRST_NAME, "123")
        presenter.onSubmit()
        presenter.onFieldChanged(StrigaSignupDataType.FIRST_NAME, "123")
        advanceUntilIdle()

        verify { view.setErrors(any()) }
        verify { view.setButtonIsEnabled(any()) }
        verify { view.clearError(StrigaSignupDataType.FIRST_NAME) }

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN users clicks phone country THEN check country picker is opened`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, TurkeyCountry.nameCodeAlpha3)
        )
        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "",
            customSharePhoneNumberE164 = "+905348558899",
            socialShareOwnerEmail = "email@email.email",
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.detectCountryOrDefault() } returns TurkeyCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(TurkeyCountry.nameCodeAlpha2) } returns TurkeyCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(TurkeyCountry.nameCodeAlpha3) } returns TurkeyCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onPhoneCountryCodeClicked()
        advanceUntilIdle()

        verify(exactly = 1) { view.showPhoneCountryCodePicker(TurkeyCountry) }

        advanceUntilIdle()
        presenter.onPhoneCountryCodeChanged(TurkeyCountry, true)
        verify { view.showPhoneCountryCode(TurkeyCountry) }

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN user clicks country of birth THEN check country picker is opened`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.nameCodeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.detectCountryOrDefault() } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onCountryOfBirthClicked()
        advanceUntilIdle()

        verify(exactly = 1) { view.showCountryOfBirthPicker(SupportedCountry) }

        presenter.onCountryOfBirthdayChanged(SupportedCountry)
        verify {
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3,
                "${SupportedCountry.flagEmoji} ${SupportedCountry.countryName}"
            )
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN pre-saved phone number WHEN screen is opened THEN check phone number and code correctly displayed`() = runTest {
        val expectedPhoneCountry = TurkeyCountry
        val expectedPhoneNumber = "5348558899"

        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "+${expectedPhoneCountry.phoneCode}"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, expectedPhoneNumber),
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.detectCountryOrDefault() } returns expectedPhoneCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(expectedPhoneCountry.nameCodeAlpha2) } returns expectedPhoneCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(expectedPhoneCountry.nameCodeAlpha3) } returns expectedPhoneCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        verify { view.setupPhoneCountryCodePicker(expectedPhoneCountry, expectedPhoneNumber) }
    }

    @Test
    fun `GIVEN user phone number WHEN user changes phone number THEN check phone code is saved with plus sign`() = runTest {
        val expectedPhoneCountry = TurkeyCountry
        val expectedPhoneNumber = "5348558899"

        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "+${expectedPhoneCountry.phoneCode}"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, expectedPhoneNumber),
        ).sortedBy { it.type }

        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "Device",
            customSharePhoneNumberE164 = "${expectedPhoneCountry.phoneCodeWithPlusSign}$expectedPhoneNumber",
            socialShareOwnerEmail = "email@email.email",
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )

        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.detectCountryOrDefault() } returns expectedPhoneCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(expectedPhoneCountry.nameCodeAlpha2) } returns expectedPhoneCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(expectedPhoneCountry.nameCodeAlpha3) } returns expectedPhoneCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.saveChanges()
        advanceUntilIdle()

        val saveChangesState = slot<Collection<StrigaSignupData>>()
        verify { interactor.saveChanges(capture(saveChangesState)) }

        assertContentEquals(initialSignupData, saveChangesState.captured.toList().sortedBy { it.type })
    }

    @Test
    fun `GIVEN user input WHEN user types invalid data THEN check dynamic validation works after submit clicked`() = runTest {
        val expectedPhoneCountry = TurkeyCountry
        val expectedPhoneNumber = "5348558899"

        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "+${expectedPhoneCountry.phoneCode}"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, expectedPhoneNumber),
        ).sortedBy { it.type }

        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.detectCountryOrDefault() } returns expectedPhoneCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(expectedPhoneCountry.nameCodeAlpha2) } returns expectedPhoneCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(expectedPhoneCountry.nameCodeAlpha3) } returns expectedPhoneCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onSubmit()
        presenter.onFieldChanged(StrigaSignupDataType.FIRST_NAME, "")
        advanceUntilIdle()

        val errorsStates = mutableListQueueOf<List<StrigaSignupFieldState>>()

        verify { view.setErrors(capture(errorsStates)) }

        assertEquals(2, errorsStates.size)
        assertEquals(1, errorsStates[1].size)
        val firstNameErrorState = errorsStates.back()?.first()
        assertNotNull(firstNameErrorState)
        assertEquals(StrigaSignupDataType.FIRST_NAME, firstNameErrorState.type)
        assertFalse(firstNameErrorState.isValid)
    }

    /* TODO: test is broken since new changes
    @Test
    fun `GIVEN case when impossible to detect default country code WHEN screen is opened THEN check error is shown`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.nameCodeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.detectCountryOrDefault() } throws IllegalStateException("No default country code detected")
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onCountryOfBirthClicked()
        advanceUntilIdle()

        verify { view.showUiKitSnackBar(messageResId = R.string.error_general_message) }
    }
     */

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

        countryCodeRepository = spyk(
            CountryCodeInMemoryRepository(
                dispatchers = dispatchers,
                context = context,
                countryCodeHelper = parser
            )
        )
    }

    private fun readCountriesXmlFile(): InputStream {
        val ccpEnglishFile = File(currentWorkingDir, "src/main/res/raw/ccp_english.xml")
        Assert.assertTrue(ccpEnglishFile.exists())

        return ccpEnglishFile.inputStream()
    }
}
