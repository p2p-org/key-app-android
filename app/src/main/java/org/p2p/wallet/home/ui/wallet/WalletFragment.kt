package org.p2p.wallet.home.ui.wallet

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentWalletBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.utils.HomeScreenLayoutManager
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class WalletFragment :
    BaseMvpFragment<WalletContract.View, WalletContract.Presenter>(R.layout.fragment_wallet),
    WalletContract.View {

    companion object {
        fun create(): WalletFragment = WalletFragment()
    }

    override val presenter: WalletContract.Presenter by inject()

    private val binding: FragmentWalletBinding by viewBinding()

    private val receiveAnalytics: ReceiveAnalytics by inject()

    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupView()

        lifecycle.addObserver(presenter)
    }

    override fun showAddressCopied(addressOrUsername: String) {
        requireContext().copyToClipBoard(addressOrUsername)
        showUiKitSnackBar(
            message = getString(R.string.home_address_snackbar_text),
            actionButtonResId = R.string.common_ok,
            actionBlock = Snackbar::dismiss
        )
    }

    override fun showActionButtons(buttons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(buttons)
    }

    private fun FragmentWalletBinding.setupView() {
        layoutManager = HomeScreenLayoutManager(requireContext())
        layoutToolbar.setupToolbar()

        homeRecyclerView.doOnAttach {
            homeRecyclerView.layoutManager = layoutManager
        }
        homeRecyclerView.doOnDetach {
            homeRecyclerView.layoutManager = null
        }
        swipeRefreshLayout.setOnRefreshListener(presenter::refreshTokens)
        viewActionButtons.onButtonClicked = ::onActionButtonClicked

        if (BuildConfig.DEBUG) {
            with(layoutToolbar) {
                viewDebugShadow.isVisible = true
                imageViewDebug.isVisible = true
                imageViewDebug.setOnClickListener {
                    replaceFragment(DebugSettingsFragment.create())
                }
            }
        }
    }

    private fun LayoutHomeToolbarBinding.setupToolbar() {
        textViewAddress.setOnClickListener {
            receiveAnalytics.logAddressOnMainClicked()
            presenter.onAddressClicked()
        }
        imageViewProfile.setOnClickListener { presenter.onProfileClick() }
        imageViewQr.setOnClickListener { replaceFragment(ReceiveSolanaFragment.create(token = null)) }
    }

    private fun onActionButtonClicked(clickedButton: ActionButton) {
        when (clickedButton) {
            ActionButton.SEND_BUTTON -> {
                presenter.onSendClicked(clickSource = SearchOpenedFromScreen.MAIN)
            }
            ActionButton.SELL_BUTTON -> {
                presenter.onSellClicked()
            }
            ActionButton.SWAP_BUTTON -> {
                presenter.onSwapClicked()
            }
            ActionButton.TOP_UP_BUTTON -> {
                presenter.onTopupClicked()
            }
            else -> {
                // unsupported on this screen
            }
        }
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        binding.layoutToolbar.textViewAddress.text = ellipsizedAddress
    }

    override fun showBalance(cellModel: TextViewCellModel?) {
        binding.viewBalance.textViewAmount.bindOrGone(cellModel)
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showEmptyViewData(data: List<Any>) {
    }

    override fun showEmptyState(isEmpty: Boolean) {
    }

    override fun navigateToProfile() {
        replaceFragment(SettingsFragment.create())
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(from = ReserveUsernameOpenedFrom.SETTINGS))
    }
}
