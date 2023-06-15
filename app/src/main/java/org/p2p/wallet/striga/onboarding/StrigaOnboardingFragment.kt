package org.p2p.wallet.striga.onboarding

import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.core.view.isVisible
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentStrigaOnboardingBinding
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.striga.onboarding.StrigaOnboardingContract.View.AvailabilityState
import org.p2p.wallet.striga.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.ui.StrigaSignUpFirstStepFragment
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaOnboardingFragment :
    BaseMvpFragment<StrigaOnboardingContract.View, StrigaOnboardingContract.Presenter>(
        R.layout.fragment_striga_onboarding
    ),
    StrigaOnboardingContract.View {

    companion object {
        private const val REQUEST_KEY_PICKER = "request_key"
        private const val RESULT_KEY_PICKER = "result_picker"

        fun create(): StrigaOnboardingFragment = StrigaOnboardingFragment()
    }

    override val presenter: StrigaOnboardingContract.Presenter by inject()

    private val binding: FragmentStrigaOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBackPress()
        bindHelpText()

        binding.buttonContinue.setOnClickListener {
            presenter.onCountryClicked()
        }
        binding.blockChangeCountry.setOnClickListener {
            presenter.onCountryClicked()
        }
        binding.textViewPoweredBy.setOnClickListener {
            Intent(Intent.ACTION_VIEW, getString(R.string.striga_powered_by_url).toUri())
                .also { startActivity(it) }
        }
    }

    private fun initBackPress() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    override fun navigateNext() {
        replaceFragment(StrigaSignUpFirstStepFragment.create())
    }

    override fun setCurrentCountry(country: CountryCode) {
        mapCountryView(country.countryName, country.flagEmoji)
    }

    override fun setAvailabilityState(state: AvailabilityState) {
        when (state) {
            AvailabilityState.Available -> handleAvailableState(state)
            AvailabilityState.Unavailable -> handleUnavailableState(state)
        }

        binding.imageViewImage.animate()
            .alpha(1f)
            .setDuration(150L)
            .start()
    }

    override fun showCountryPicker(selectedItem: CountryCode?) {
        replaceFragmentForResult(
            target = StrigaPresetDataPickerFragment.create(
                requestKey = REQUEST_KEY_PICKER,
                resultKey = RESULT_KEY_PICKER,
                dataToPick = StrigaPresetDataItem.Country(selectedItem)
            ),
            requestKey = REQUEST_KEY_PICKER,
            onResult = { _, result ->
                result.getParcelableCompat<StrigaPresetDataItem.Country>(RESULT_KEY_PICKER)
                    ?.also { presenter.onCurrentCountryChanged(it.details ?: return@also) }
            }
        )
    }

    private fun openCountrySelection(country: CountryCode?) {
    }

    override fun openHelp() {
        IntercomService.showMessenger()
    }

    private fun onBackPressed() {
        popBackStack()
    }

    private fun bindHelpText() {
        val helpHighlightText = getString(R.string.striga_onboarding_help_highlight_text)
        val helpCommonText = getString(R.string.striga_onboarding_help_common_text, helpHighlightText)
        val helpTextSpannable = SpanUtils.highlightLinkNoUnderline(
            text = helpCommonText,
            linkToHighlight = helpHighlightText,
            linkColor = getColor(R.color.sky),
            onClick = { presenter.onClickHelp() }
        )

        with(binding.textViewHelp) {
            highlightColor = Color.TRANSPARENT
            movementMethod = LinkMovementMethod()
            bind(TextViewCellModel.Raw(TextContainer.Raw(helpTextSpannable)))
        }
    }

    private fun mapCountryView(countryName: String, countryFlag: String) {
        binding.blockChangeCountry.bind(
            MainCellModel(
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

    private fun handleAvailableState(state: AvailabilityState) {
        handleViewState(state)
        binding.buttonContinue.setOnClickListener {
            presenter.onClickContinue()
        }
    }

    private fun handleUnavailableState(state: AvailabilityState) {
        handleViewState(state)
        binding.buttonContinue.setOnClickListener {
            presenter.onCountryClicked()
        }
    }

    private fun handleViewState(state: AvailabilityState) = with(binding) {
        imageViewImage.bind(getImageModel(state))
        textViewTitle.bind(getTitleModel(state))
        textViewHelp.isVisible = state.isHelpVisible
        textViewPoweredBy.isVisible = state.isPoweredByStrigaVisible

        buttonContinue.apply {
            setText(state.buttonTextRes)
            icon = if (state.isButtonArrowVisible) {
                getDrawable(R.drawable.ic_arrow_right)
            } else {
                null
            }
        }
    }

    private fun getImageModel(state: AvailabilityState) = ImageViewCellModel(
        DrawableContainer(state.imageRes)
    )

    private fun getTitleModel(state: AvailabilityState) = TextViewCellModel.Raw(
        TextContainer.Res(state.titleTextRes),
    )
}
