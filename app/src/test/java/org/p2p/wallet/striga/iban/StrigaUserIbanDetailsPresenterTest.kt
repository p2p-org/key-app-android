package org.p2p.wallet.striga.iban

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.p2p.wallet.striga.onramp.iban.StrigaUserIbanDetailsContract
import org.p2p.wallet.striga.onramp.iban.presenter.StrigaUserIbanDetailsPresenter
import org.p2p.wallet.striga.onramp.iban.presenter.StrigaUserIbanUiMapper
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.plantStubTimber
import org.p2p.wallet.utils.stub
import org.p2p.wallet.utils.verifyNone
import org.p2p.wallet.utils.verifyOnce

class StrigaUserIbanDetailsPresenterTest {

    private lateinit var presenter: StrigaUserIbanDetailsContract.Presenter

    init {
        plantStubTimber()
    }

    private val mapper: StrigaUserIbanUiMapper = mockk()
    private val interactor: StrigaOnRampInteractor = mockk(relaxed = true) {
        every { isIbanNotesHidden } returns false
    }
    private val walletInteractor: StrigaWalletInteractor = mockk()

    @Before
    fun beforeEach() {
        presenter = StrigaUserIbanDetailsPresenter(
            interactor = interactor,
            walletInteractor = walletInteractor,
            mapper = mapper,
            dispatchers = UnconfinedTestDispatchers()
        )
    }

    @Test
    fun `GIVEN valid iban response WHEN get iban THEN return non-empty ui models`() {
        // GIVEN
        mapper.stub {
            every { mapToCellModels(any()) }.returns(ArrayList(4))
            every { mapImportantNotesInformerModel(any()) } returns mockk(relaxed = true)
        }
        walletInteractor.stub {
            coEvery { getFiatAccountDetails() }.returns(mockk(relaxed = true))
        }

        // WHEN
        val view = mockk<StrigaUserIbanDetailsContract.View>(relaxed = true)
        presenter.attach(view = view)

        // THEN
        coVerify(exactly = 1) { walletInteractor.getFiatAccountDetails() }
        verify { mapper.mapToCellModels(any()) }
        verify { view.showIbanDetails(any()) }
        verifyNone { view.navigateBack() }
    }

    @Test
    fun `GIVEN exception for iban response WHEN get iban THEN navigate back and show error`() {
        // GIVEN
        walletInteractor.stub {
            coEvery { getFiatAccountDetails() }.throws(mockk(relaxed = true))
        }

        // WHEN
        val view = mockk<StrigaUserIbanDetailsContract.View>(relaxed = true)
        presenter.attach(view = view)

        // THEN
        coVerify(exactly = 1) { walletInteractor.getFiatAccountDetails() }
        verifyOnce { view.navigateBack() }
        verifyNone { mapper.mapToCellModels(any()) }
        verifyNone { view.showIbanDetails(any()) }
    }
}
