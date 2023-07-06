package org.p2p.wallet.solend.ui.info

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.common.ui.FragmentPageConfiguration
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.databinding.DialogSolendInfoPagerPartBinding
import org.p2p.wallet.utils.withArgs

class SolendInfoBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String
        ) = SolendInfoBottomSheet()
            .withArgs(ARG_TITLE to title)
            .show(fm, SolendInfoBottomSheet::javaClass.name)
    }

    private lateinit var binding: DialogSolendInfoPagerPartBinding

    private val pages: List<FragmentPageConfiguration> = listOf(
        FragmentPageConfiguration(
            SolendInfoSliderFragment::class,
            SolendInfoSliderFragmentArgs(
                R.drawable.ic_solend_slider_1,
                R.string.solend_info_slider_title_1,
                R.string.solend_info_slider_text_1,
            ).toBundle(),
        ),
        FragmentPageConfiguration(
            SolendInfoSliderFragment::class,
            SolendInfoSliderFragmentArgs(
                R.drawable.ic_solend_slider_2,
                R.string.solend_info_slider_title_2,
                R.string.solend_info_slider_text_2,
            ).toBundle(),
        ),
        FragmentPageConfiguration(
            SolendInfoSliderFragment::class,
            SolendInfoSliderFragmentArgs(
                R.drawable.ic_solend_slider_3,
                R.string.solend_info_slider_title_3,
                R.string.solend_info_slider_text_3,
            ).toBundle(),
        ),
    )

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSolendInfoPagerPartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDoneButtonVisibility(isVisible = false)
        setCloseButtonVisibility(isVisible = true)
        with(binding) {
            solendInfoSliderPager.adapter = BaseFragmentAdapter(childFragmentManager, lifecycle, pages)
            solendInfoSliderDotsIndicator.attachTo(solendInfoSliderPager)
        }
    }
}
