package com.p2p.wallet.auth.repository

import com.p2p.wallet.utils.mnemoticgenerator.English
import com.p2p.wallet.utils.mnemoticgenerator.MnemonicGenerator
import com.p2p.wallet.utils.mnemoticgenerator.Words
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class AuthRemoteRepository : AuthRepository {

    override suspend fun generatePhrase(): List<String> = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWENTY_FOUR.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append)
        sb.toString().split(" ")
    }
}