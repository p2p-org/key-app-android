package org.p2p.wallet.auth.interactor

import android.content.Context
import org.p2p.wallet.auth.repository.FileRepository
import java.io.File

class FileInteractor(
    private val context: Context,
    private val fileRepository: FileRepository
) {

    fun getTermsOfUseFile(): File {
        val inputStream = context.assets.open(FileRepository.TERMS_OF_USE_PDF)
        return fileRepository.savePdf(FileRepository.TERMS_OF_USE, inputStream.readBytes())
    }

    fun getPrivacyPolicyFile(): File {
        val inputStream = context.assets.open(FileRepository.PRIVACY_POLICY_PDF)
        return fileRepository.savePdf(FileRepository.PRIVACY_POLICY, inputStream.readBytes())
    }
}
