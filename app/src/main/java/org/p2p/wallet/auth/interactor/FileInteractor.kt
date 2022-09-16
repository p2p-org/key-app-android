package org.p2p.wallet.auth.interactor

import android.content.Context
import org.p2p.wallet.auth.repository.FileRepository
import java.io.File

class FileInteractor(
    private val context: Context,
    private val fileRepository: FileRepository
) {

    companion object {
        private const val TERMS_OF_USE_PDF = "p2p_terms_of_service.pdf"
        private const val PRIVACY_POLICY_PDF = "p2p_privacy_policy.pdf"
        private const val TERMS_OF_USE = "p2p_terms_of_service"
        private const val PRIVACY_POLICY = "p2p_privacy_policy"
    }

    fun getTermsOfUseFile(): File {
        val inputStream = context.assets.open(TERMS_OF_USE_PDF)
        return fileRepository.savePdf(TERMS_OF_USE, inputStream.readBytes())
    }

    fun getPrivacyPolicyFile(): File {
        val inputStream = context.assets.open(PRIVACY_POLICY_PDF)
        return fileRepository.savePdf(PRIVACY_POLICY, inputStream.readBytes())
    }
}
