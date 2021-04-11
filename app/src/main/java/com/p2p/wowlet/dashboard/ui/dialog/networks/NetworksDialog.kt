package com.p2p.wowlet.dashboard.ui.dialog.networks

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.ui.dialog.networks.viewmodel.NetworkViewModel
import kotlinx.android.synthetic.main.dialog_networks.devNetRb
import kotlinx.android.synthetic.main.dialog_networks.mainNetRb
import kotlinx.android.synthetic.main.dialog_networks.testNetRb
import kotlinx.android.synthetic.main.dialog_networks.vClose
import kotlinx.android.synthetic.main.dialog_networks.vDone
import kotlinx.android.synthetic.main.dialog_networks.vNetworkGroup
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.p2p.solanaj.rpc.Cluster

class NetworksDialog(
    private val doneClickListener: ((Cluster) -> Unit)
) : DialogFragment() {

    private val viewModel: NetworkViewModel by viewModel()
    private var cluster = Cluster.MAINNET

    companion object {
        const val TAG_NETWORKS_DIALOG = "NetworksDialog"
        fun newInstance(doneClickListener: ((Cluster) -> Unit)): NetworksDialog {
            return NetworksDialog(doneClickListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_networks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSelectedNetwork()

        vDone.setOnClickListener {
            dismiss()
            viewModel.saveSelectedNetwork(cluster)
            doneClickListener.invoke(cluster)
        }
        vClose.setOnClickListener {
            dismiss()
        }

        vNetworkGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.devNetRb -> cluster = Cluster.DEVNET
                R.id.testNetRb -> cluster = Cluster.TESTNET
                R.id.mainNetRb -> cluster = Cluster.MAINNET
            }
        }

        viewModel.getSelectedNetwork.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    Cluster.DEVNET -> {
                        devNetRb.isChecked = true
                        cluster = it
                    }
                    Cluster.TESTNET -> {
                        testNetRb.isChecked = true
                        cluster = it
                    }
                    Cluster.MAINNET -> {
                        mainNetRb.isChecked = true
                        cluster = it
                    }
                    else -> {
                        mainNetRb.isChecked = true
                        cluster = Cluster.MAINNET
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}