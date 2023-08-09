package org.p2p.wallet.home.onofframp.ui

import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.ForegroundCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.NavigationDestination
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentAddMoneyOnboardingBinding
import org.p2p.wallet.home.onofframp.OnOffRampCountrySelectionContract
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.striga.signup.presetpicker.StrigaPresetDataPickerFragment
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.viewbinding.viewBinding

class OnOffRampCountrySelectionFragment :
    BaseMvpFragment<OnOffRampCountrySelectionContract.View, OnOffRampCountrySelectionContract.Presenter>(
        R.layout.fragment_add_money_onboarding
    ),
    OnOffRampCountrySelectionContract.View {

    companion object {
        val REQUEST_KEY: String = OnOffRampCountrySelectionFragment::class.java.name
        const val RESULT_KEY_COUNTRY_SELECTED: String = "RESULT_KEY_COUNTRY_SELECTED"
        private const val REQUEST_KEY_PICKER = "request_key"
        private const val RESULT_KEY_PICKER = "result_picker"

        fun create(
            navigationDestination: NavigationDestination
        ): OnOffRampCountrySelectionFragment = OnOffRampCountrySelectionFragment().apply {
            arguments = bundleOf(
                NavigationDestination.ARG_KEY to navigationDestination
            )
        }
    }

    override val presenter: OnOffRampCountrySelectionContract.Presenter by inject()

    private val binding: FragmentAddMoneyOnboardingBinding by viewBinding()

    private val navigationDestination: NavigationDestination by args(NavigationDestination.ARG_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBackPress()

        binding.blockChangeCountry.setOnClickListener {
            presenter.onCountryClicked()
        }
        binding.buttonContinue.setOnClickListener {
            presenter.onNextClicked()
        }
    }

    private fun initBackPress() {
        binding.toolbar.setNavigationOnClickListener { returnToMain() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            returnToMain()
        }
    }

    override fun navigateNext() {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_KEY_COUNTRY_SELECTED to true
            )
        )
        navigationDestination.navigateNext(this)
    }

    override fun setCurrentCountry(country: CountryCode) {
        mapCountryView(country.countryName, country.flagEmoji)
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

    private fun returnToMain() {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_KEY_COUNTRY_SELECTED to false
            )
        )
        popBackStackTo(MainContainerFragment::class)
    }

    private fun mapCountryView(countryName: String, countryFlag: String) {
        binding.blockChangeCountry.bind(
            MainCellModel(
                background = DrawableCellModel(
                    drawable = shapeDrawable(shapeRounded16dp()),
                    tint = R.color.bg_snow
                ),
                foreground = ForegroundCellModel.Ripple(shapeRounded16dp()),
                leftSideCellModel = LeftSideCellModel.IconWithText(
                    icon = IconWrapperCellModel.SingleEmoji(
                        emoji = countryFlag,
                        background = DrawableCellModel(tint = R.color.smoke),
                        clippingShape = shapeCircle()
                    ),
                    firstLineText = TextViewCellModel.Raw(
                        text = TextContainer(countryName),
                        textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3
                    ),
                    secondLineText = TextViewCellModel.Raw(
                        text = TextContainer(R.string.striga_onboarding_country_subtitle),
                        textAppearance = R.style.UiKit_TextAppearance_Regular_Label1
                    ),
                ),
                rightSideCellModel = RightSideCellModel.IconWrapper(
                    IconWrapperCellModel.SingleIcon(
                        ImageViewCellModel(
                            icon = DrawableContainer(R.drawable.ic_chevron_right),
                            iconTint = R.color.mountain
                        )
                    )
                )
            )
        )
    }
}
