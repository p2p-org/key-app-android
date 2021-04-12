package com.p2p.wallet.dashboard.ui.dialog.networks

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.dashboard.ui.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wallet.databinding.DialogNetworksBinding
import com.p2p.wallet.utils.viewbinding.viewBinding
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

    private val binding: DialogNetworksBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_networks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSelectedNetwork()

        binding.vDone.setOnClickListener {
            dismiss()
            viewModel.saveSelectedNetwork(cluster)
            doneClickListener.invoke(cluster)
        }
        binding.vClose.setOnClickListener {
            dismiss()
        }

        binding.vNetworkGroup.setOnCheckedChangeListener { _, checkedId ->
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
                        binding.devNetRb.isChecked = true
                        cluster = it
                    }
                    Cluster.TESTNET -> {
                        binding.testNetRb.isChecked = true
                        cluster = it
                    }
                    Cluster.MAINNET -> {
                        binding.mainNetRb.isChecked = true
                        cluster = it
                    }
                    else -> {
                        binding.mainNetRb.isChecked = true
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