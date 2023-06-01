package org.p2p.wallet.striga.signup.ui

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.mutableListQueueOf
import org.p2p.wallet.utils.plantTimberToStdout

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupSecondStepPresenterTest {
    @MockK(relaxed = true)
    lateinit var countryRepository: CountryRepository

    @MockK(relaxed = true)
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    lateinit var interactor: StrigaSignupInteractor

    private val signupDataValidator = StrigaSignupDataValidator()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    init {
        plantTimberToStdout("StrigaSignupSecondStepPresenterTest")
    }

    private fun createPresenterSecondStep(): StrigaSignUpSecondStepPresenter {
        return StrigaSignUpSecondStepPresenter(
            dispatchers = dispatchers,
            interactor = interactor,
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

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenterSecondStep()
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

        Assert.assertEquals(initialSignupData, resultSignupData)

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
        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns "## ### ### ## ##"

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenterSecondStep()
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
        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns "## ### ### ## ##"

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenterSecondStep()
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

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenterSecondStep()
        presenter.attach(view)

        presenter.onFieldChanged("any occupation", StrigaSignupDataType.OCCUPATION)
        presenter.onFieldChanged("any source", StrigaSignupDataType.SOURCE_OF_FUNDS)
        presenter.onFieldChanged("any country", StrigaSignupDataType.COUNTRY)
        presenter.onFieldChanged("any city", StrigaSignupDataType.CITY)
        presenter.onFieldChanged("any address", StrigaSignupDataType.CITY_ADDRESS_LINE)
        presenter.onFieldChanged("any zip-code", StrigaSignupDataType.CITY_POSTAL_CODE)
        presenter.onFieldChanged("any state", StrigaSignupDataType.CITY_STATE)
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.navigateNext() }

        presenter.saveChanges()
        presenter.detach()
    }
}
