package org.p2p.wallet.common.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.p2p.wallet.databinding.DialogBaseRecyclerPartBinding

/**
 * Base BottomSheet extended from [BaseDoneBottomSheet] used to implement [RecyclerView] as an innerView
 **/
abstract class BaseRecyclerDoneBottomSheet : BaseDoneBottomSheet() {

    lateinit var recyclerBinding: DialogBaseRecyclerPartBinding

    override fun onCreateInnerView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        recyclerBinding = DialogBaseRecyclerPartBinding.inflate(inflater, container, false)
        return recyclerBinding.root
    }
}
