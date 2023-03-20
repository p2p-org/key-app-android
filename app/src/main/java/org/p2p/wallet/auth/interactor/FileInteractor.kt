package org.p2p.wallet.auth.interactor

import android.content.Context
import org.p2p.wallet.common.storage.FileRepository
import java.io.File

private const val TERMS_OF_USE_PDF = "p2p_terms_of_service.pdf"
private const val PRIVACY_POLICY_PDF = "p2p_privacy_policy.pdf"
private const val TERMS_OF_USE = "p2p_terms_of_service"
private const val PRIVACY_POLICY = "p2p_privacy_policy"

class FileInteractor(
    private val context: Context,
    private val fileRepository: FileRepository
) {

    fun getTermsOfUseFile(): File {
        return if (fileRepository.isFileExists(TERMS_OF_USE_PDF)) {
            fileRepository.getPdfFile(TERMS_OF_USE)
        } else {
            val inputStream = context.assets.open(TERMS_OF_USE_PDF)
            fileRepository.savePdf(TERMS_OF_USE, inputStream.readBytes())
        }
    }

    fun getPrivacyPolicyFile(): File {
        return if (fileRepository.isFileExists(PRIVACY_POLICY_PDF)) {
            fileRepository.getPdfFile(PRIVACY_POLICY)
        } else {
            val inputStream = context.assets.open(PRIVACY_POLICY_PDF)
            fileRepository.savePdf(PRIVACY_POLICY, inputStream.readBytes())
        }
    }
}
