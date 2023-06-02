package org.p2p.wallet.striga.onboarding

import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.core.view.isVisible
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaOnboardingBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataToPick
import org.p2p.wallet.striga.signup.ui.StrigaSignUpFirstStepFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaOnboardingFragment :
    BaseMvpFragment<StrigaOnboardingContract.View, StrigaOnboardingContract.Presenter>(
        R.layout.fragment_striga_onboarding
    ),
    StrigaOnboardingContract.View {

    companion object {
        fun create(): StrigaOnboardingFragment = StrigaOnboardingFragment()
    }

    override val presenter: StrigaOnboardingContract.Presenter by inject()

    private val binding: FragmentStrigaOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleBackPress()
        bindHelpText()

        binding.buttonContinue.setOnClickListener {
            openCountrySelection()
        }
        binding.blockChangeCountry.setOnClickListener {
            openCountrySelection()
        }
        binding.textViewPoweredBy.setOnClickListener {
            Intent(Intent.ACTION_VIEW, getString(R.string.striga_powered_by_url).toUri())
                .also { startActivity(it) }
        }
    }

    override fun navigateNext() {
        // TODO: navigate to next screen
        replaceFragment(StrigaSignUpFirstStepFragment.create())
    }

    override fun setCurrentCountry(country: Country) {
        mapCountryView(country.name, country.flagEmoji)
    }

    override fun setAvailabilityState(state: StrigaOnboardingContract.View.AvailabilityState) {
        when (state) {
            StrigaOnboardingContract.View.AvailabilityState.Available -> handleAvailableState(state)
            StrigaOnboardingContract.View.AvailabilityState.Unavailable -> handleUnavailableState(state)
        }

        binding.imageViewImage.animate()
            .alpha(1f)
            .setDuration(150L)
            .start()
    }

    private fun openCountrySelection() {
        replaceFragment(
            StrigaPresetDataPickerFragment.create(StrigaPresetDataToPick.CURRENT_ADDRESS_COUNTRY)
        )
    }

    override fun openHelp() {
        IntercomService.showMessenger()
    }

    private fun handleBackPress() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    private fun onBackPressed() {
        popBackStack()
    }

    private fun bindHelpText() {
        val helpHighlightText = binding.getString(R.string.striga_onboarding_help_highlight_text)
        val helpCommonText = binding.getString(R.string.striga_onboarding_help_common_text, helpHighlightText)
        val helpTextSpannable =
            SpanUtils.highlightLinkNoUnderline(helpCommonText, helpHighlightText, binding.getColor(R.color.sky)) {
                presenter.onClickHelp()
            }

        with(binding.textViewHelp) {
            movementMethod = LinkMovementMethod()
            bind(
                TextViewCellModel.Raw(
                    TextContainer.Raw(helpTextSpannable),
                )
            )
        }
    }

    private fun mapCountryView(countryName: String, countryFlag: String) {
        binding.blockChangeCountry.bind(
            FinanceBlockCellModel(
                background = null,
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    icon = IconWrapperCellModel.SingleEmoji(countryFlag),
                    firstLineText = TextViewCellModel.Raw(
                        TextContainer(countryName),
                        textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        TextContainer(R.string.striga_onboarding_country_subtitle)
                    ),
                ),
                rightSideCellModel = RightSideCellModel.IconWrapper(
                    IconWrapperCellModel.SingleIcon(
                        ImageViewCellModel(
                            DrawableContainer(R.drawable.ic_chevron_right),
                            iconTint = R.color.mountain
                        )
                    )
                )
            )
        )
    }

    private fun handleAvailableState(state: StrigaOnboardingContract.View.AvailabilityState) {
        handleViewState(state)
        binding.buttonContinue.setOnClickListener {
            presenter.onClickContinue()
        }
    }

    private fun handleUnavailableState(state: StrigaOnboardingContract.View.AvailabilityState) {
        handleViewState(state)
        binding.buttonContinue.setOnClickListener {
            presenter.onClickChangeCountry()
        }
    }

    private fun handleViewState(state: StrigaOnboardingContract.View.AvailabilityState) {
        with(binding) {
            imageViewImage.bind(getImageModel(state))
            textViewTitle.bind(getTitleModel(state))
            textViewHelp.isVisible = state.isHelpVisible

            buttonContinue.apply {
                setText(state.buttonTextRes)
                icon = if (state.isButtonArrowVisible) {
                    binding.getDrawable(R.drawable.ic_arrow_right)
                } else {
                    null
                }
            }
        }
    }

    private fun getImageModel(state: StrigaOnboardingContract.View.AvailabilityState) = ImageViewCellModel(
        DrawableContainer(state.imageRes)
    )

    private fun getTitleModel(state: StrigaOnboardingContract.View.AvailabilityState) = TextViewCellModel.Raw(
        TextContainer.Res(state.titleTextRes),
    )
}
