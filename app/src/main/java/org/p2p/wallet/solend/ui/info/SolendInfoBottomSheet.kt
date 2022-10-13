package org.p2p.wallet.solend.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseCloseBottomSheet
import org.p2p.wallet.databinding.DialogSolendInfoPagerPartBinding
import org.p2p.wallet.utils.withArgs

class SolendInfoBottomSheet : BaseCloseBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String
        ) = SolendInfoBottomSheet()
            .withArgs(ARG_TITLE to title)
            .show(fm, SolendInfoBottomSheet::javaClass.name)
    }

    private lateinit var binding: DialogSolendInfoPagerPartBinding

    private val fragments = List(3) { SolendInfoSliderFragment::class }
    private val args: List<Bundle>
        get() = listOf(
            SolendInfoSliderFragmentArgs(
                R.drawable.ic_solend_slider_1,
                R.string.solend_info_slider_text_1,
            ).toBundle(),
            SolendInfoSliderFragmentArgs(
                R.drawable.ic_solend_slider_2,
                R.string.solend_info_slider_text_2,
            ).toBundle(),
            SolendInfoSliderFragmentArgs(
                R.drawable.ic_solend_slider_3,
                R.string.solend_info_slider_text_3,
            ).toBundle()
        )

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSolendInfoPagerPartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            solendInfoSliderPager.adapter = BaseFragmentAdapter(childFragmentManager, lifecycle, fragments, args)
            solendInfoSliderDotsIndicator.attachTo(solendInfoSliderPager)
        }
    }
}
