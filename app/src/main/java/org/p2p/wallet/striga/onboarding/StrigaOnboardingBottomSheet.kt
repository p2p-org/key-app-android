package org.p2p.wallet.striga.onboarding

import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogStrigaOnboardingBinding

class StrigaOnboardingBottomSheet :
    BaseMvpBottomSheet<StrigaOnboardingContract.View, StrigaOnboardingContract.Presenter>(
        R.layout.dialog_striga_onboarding
    ),
    StrigaOnboardingContract.View {

    companion object {
        fun show(fragmentManager: FragmentManager, tag: String? = null) {
            if (fragmentManager.findFragmentByTag(tag) == null) {
                StrigaOnboardingBottomSheet().show(fragmentManager, tag)
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSmoke

    override val presenter: StrigaOnboardingContract.Presenter by inject()

    private lateinit var binding: DialogStrigaOnboardingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogStrigaOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonContinue.setOnClickListener {
            presenter.onClickChangeCountry()
        }
        binding.textViewPoweredBy.setOnClickListener {
            Intent(Intent.ACTION_VIEW, getString(R.string.striga_powered_by_url).toUri()).also { startActivity(it) }
        }
    }

    override fun navigateNext() {
        // TODO: navigate to next screen
    }

    override fun setCurrentCountry(country: Country) {
        mapCountryView(country.name, country.flagEmoji)
    }

    override fun setButtonState(state: StrigaOnboardingContract.View.ButtonState) {
        when (state) {
            StrigaOnboardingContract.View.ButtonState.Continue -> handleButtonContinueState(state)
            StrigaOnboardingContract.View.ButtonState.ChangeCountry -> handleButtonChangeCountryState(state)
        }
    }

    override fun openCountrySelection() {
        /* TODO: select country
            replaceFragmentForResult(Fragment(), "select_country", onResult = { requestKey, bundle ->
                val country = bundle.getParcelableCompat<Country>("country")
                presenter.onCountrySelected(country)
            })
        */
    }

    private fun mapCountryView(countryName: String, countryFlag: String) {
        binding.changeCountryBlock.bind(
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
                            DrawableContainer(R.drawable.ic_arrow_right),
                            iconTint = R.color.mountain
                        )
                    )

                )
            )
        )
    }

    private fun handleButtonContinueState(state: StrigaOnboardingContract.View.ButtonState) {
        handleButtonState(state)
        binding.buttonContinue.setOnClickListener {
            presenter.onClickContinue()
        }
    }

    private fun handleButtonChangeCountryState(state: StrigaOnboardingContract.View.ButtonState) {
        handleButtonState(state)
        binding.buttonContinue.setOnClickListener {
            presenter.onClickChangeCountry()
        }
    }

    private fun handleButtonState(state: StrigaOnboardingContract.View.ButtonState) {
        with(binding.buttonContinue) {
            setText(state.textRes)
            icon = if (state.showArrowRight) {
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)
            } else null
        }
    }
}
