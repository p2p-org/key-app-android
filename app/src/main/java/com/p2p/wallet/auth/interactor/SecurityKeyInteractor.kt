package com.p2p.wallet.auth.interactor

import com.p2p.wallet.infrastructure.persistence.PreferenceService
import com.p2p.wallet.dashboard.repository.WowletApiCallRepository

class SecurityKeyInteractor(
    val preferenceService: PreferenceService,
    val wowletApiCallRepository: WowletApiCallRepository
) {
    // var phraseList = listOf<String>()

    var phraseList = mutableListOf<String>()
    suspend fun initUser() {
        val userData = wowletApiCallRepository.initAccount(phraseList)
        preferenceService.setSingleWalletData(userData)
    }

    fun generateKeys(): List<String> {
        // phraseList = wowletApiCallRepository.generatePhrase()
        // miracle pizza supply useful steak border same again youth silver access hundred
        phraseList.clear()
        phraseList.add("miracle")
        phraseList.add("pizza")
        phraseList.add("supply")
        phraseList.add("useful")
        phraseList.add("steak")
        phraseList.add("border")
        phraseList.add("same")
        phraseList.add("again")
        phraseList.add("youth")
        phraseList.add("silver")
        phraseList.add("access")
        phraseList.add("hundred")
        // ozone puppy agent bundle suit music purity license rebuild acquire swamp aim
//        phraseList.add("ozone")
//        phraseList.add("puppy")
//        phraseList.add("agent")
//        phraseList.add("bundle")
//        phraseList.add("suit")
//        phraseList.add("music")
//        phraseList.add("purity")
//        phraseList.add("license")
//        phraseList.add("rebuild")
//        phraseList.add("acquire")
//        phraseList.add("swamp")
//        phraseList.add("aim")

        // my wallet1 biliary galaxy pike trod highbred washing rhizopod quass snow aesthete agar bionics
/*        phraseList.add("biliary")
        phraseList.add("galaxy")
        phraseList.add("pike")
        phraseList.add("trod")
        phraseList.add("highbred")
        phraseList.add("washing")
        phraseList.add("rhizopod")
        phraseList.add("quass")
        phraseList.add("snow")
        phraseList.add("aesthete")
        phraseList.add("agar")
        phraseList.add("bionics")*/

        // my wallet2  ablaze cordoba striate cestode cameleer caloyer higgle chewink acetone shier an graphite
//        phraseList.add("ablaze")
//        phraseList.add("cordoba")
//        phraseList.add("striate")
//        phraseList.add("cestode")
//        phraseList.add("cameleer")
//        phraseList.add("caloyer")
//        phraseList.add("higgle")
//        phraseList.add("chewink")
//        phraseList.add("acetone")
//        phraseList.add("shier")
//        phraseList.add("an")
//        phraseList.add("graphite")

        return phraseList
    }
}