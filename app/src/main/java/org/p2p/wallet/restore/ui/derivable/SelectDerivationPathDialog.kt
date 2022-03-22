package org.p2p.wallet.restore.ui.derivable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.NonDraggableBottomSheetDialogFragment
import org.p2p.wallet.databinding.DialogSelectDerivationPathBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.solanaj.crypto.DerivationPath

private const val EXTRA_PATH = "EXTRA_PATH"

class SelectDerivationPathDialog(
    private val onPathSelected: (DerivationPath) -> Unit
) : NonDraggableBottomSheetDialogFragment() {

    companion object {

        fun show(fm: FragmentManager, currentPath: DerivationPath, onPathSelected: (DerivationPath) -> Unit) {
            SelectDerivationPathDialog(onPathSelected)
                .withArgs(EXTRA_PATH to currentPath)
                .show(fm, SelectDerivationPathDialog::javaClass.name)
        }
    }

    private val binding: DialogSelectDerivationPathBinding by viewBinding()

    private val path: DerivationPath by args(EXTRA_PATH)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_select_derivation_path, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            bip44Button.text = DerivationPath.BIP44.stringValue
            bip44ChangeButton.text = DerivationPath.BIP44CHANGE.stringValue
            bip32Button.text = DerivationPath.BIP32DEPRECATED.stringValue

            when (path) {
                DerivationPath.BIP44CHANGE -> pathGroup.check(R.id.bip44ChangeButton)
                DerivationPath.BIP44 -> pathGroup.check(R.id.bip44Button)
                DerivationPath.BIP32DEPRECATED -> pathGroup.check(R.id.bip32Button)
            }

            pathGroup.setOnCheckedChangeListener { _, checkedId ->
                val path = when (checkedId) {
                    R.id.bip44Button -> DerivationPath.BIP44
                    R.id.bip44ChangeButton -> DerivationPath.BIP44CHANGE
                    else -> DerivationPath.BIP32DEPRECATED
                }

                onPathSelected.invoke(path)
                dismissAllowingStateLoss()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded
}
