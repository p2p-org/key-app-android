package com.p2p.wallet.restore.interactor

import com.p2p.wallet.R
import com.p2p.wallet.main.model.SecretKey
import com.p2p.wallet.restore.model.SeedPhraseResult
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.mnemoticgenerator.English

class SecretKeyInteractor(
    private val userInteractor: UserInteractor
) {

    suspend fun verifySeedPhrase(secretKeys: List<SecretKey>): SeedPhraseResult {
        val words = English.INSTANCE.words
        val data = secretKeys.map { it.text }.filter { it.isNotEmpty() }

        val wordNotFound = data.any { !words.contains(it) }

        return if (wordNotFound) {
            SeedPhraseResult.Error(R.string.auth_wrong_seed_phrase)
        } else {
            userInteractor.createAndSaveAccount(data)
            SeedPhraseResult.Success
        }
    }
}