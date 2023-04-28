package org.p2p.wallet.restore.ui.derivable.bottomsheet

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.R
import org.p2p.wallet.databinding.DialogSelectDerivationPathBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_PATH = "EXTRA_PATH"
private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SelectDerivableAccountBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(
            fm: FragmentManager,
            currentPath: DerivationPath,
            requestKey: String,
            resultKey: String
        ) = SelectDerivableAccountBottomSheet()
            .withArgs(
                EXTRA_PATH to currentPath,
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            )
            .show(fm, SelectDerivableAccountBottomSheet::javaClass.name)
    }

    private val binding: DialogSelectDerivationPathBinding by viewBinding()

    private val currentPath: DerivationPath by args(EXTRA_PATH)

    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)

    private lateinit var selectedPath: DerivationPath

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_select_derivation_path, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedPath = currentPath
        with(binding) {
            bip44Button.text = DerivationPath.BIP44.stringValue
            bip44ChangeButton.text = DerivationPath.BIP44CHANGE.stringValue
            bip32Button.text = DerivationPath.BIP32DEPRECATED.stringValue

            when (currentPath) {
                DerivationPath.BIP44CHANGE -> pathGroup.check(R.id.bip44ChangeButton)
                DerivationPath.BIP44 -> pathGroup.check(R.id.bip44Button)
                DerivationPath.BIP32DEPRECATED -> pathGroup.check(R.id.bip32Button)
            }

            pathGroup.setOnCheckedChangeListener { _, checkedId ->
                selectedPath = when (checkedId) {
                    R.id.bip44Button -> DerivationPath.BIP44
                    R.id.bip44ChangeButton -> DerivationPath.BIP44CHANGE
                    else -> DerivationPath.BIP32DEPRECATED
                }
            }

            continueButton.setOnClickListener {
                setFragmentResult(requestKey, bundleOf(resultKey to selectedPath))
                dismissAllowingStateLoss()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow
}
