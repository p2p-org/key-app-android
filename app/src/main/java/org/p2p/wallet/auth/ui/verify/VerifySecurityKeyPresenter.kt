package org.p2p.wallet.auth.ui.verify

import kotlin.random.Random
import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor

private const val VERIFY_WORDS_COUNT = 4
private const val GENERATE_WORD_COUNT = 2
private const val KEY_SIZE = 24

@Deprecated("old onboarding, delete someday")
class VerifySecurityKeyPresenter(
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<VerifySecurityKeyContract.View>(),
    VerifySecurityKeyContract.Presenter {

    private var generatedPairs = HashMap<Int, HashMap<String, Boolean>>(KEY_SIZE)
    private var generatedIndexes = mutableListOf<Int>()
    private val phrases = mutableListOf<String>()
    private val generatedTuples = mutableListOf<SecurityKeyTuple>()

    override fun load(selectedKeys: List<String>, shuffle: Boolean) {
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.SEED_VERIFY)
        launch {
            view?.showLoading(true)
            phrases.addAll(selectedKeys)
            repeat(VERIFY_WORDS_COUNT) {
                val words = HashMap<String, Boolean>()
                // Генерируем рандомный индекс правильного ключа
                var randomIndex = generateRandomIndex(selectedKeys.size)
                // Добавляем его в лист
                words[selectedKeys[randomIndex]] = false
                // Создаем еще 2 неверных варианта
                repeat(GENERATE_WORD_COUNT) {
                    randomIndex = generateRandomIndex(selectedKeys.size)
                    words[selectedKeys[randomIndex]] = false
                }
                generatedPairs[randomIndex] = words
            }
            val items = generateTuples(generatedPairs)
            view?.showKeys(keys = items)
        }.invokeOnCompletion {
            view?.showLoading(false)
        }
    }

    override fun onKeySelected(keyIndex: Int, selectedKey: String) {
        launch {
            val keysRow = generatedPairs[keyIndex] ?: return@launch
            val isSelected = keysRow[selectedKey] ?: false

            keysRow.keys.forEach { key ->
                if (key == selectedKey) {
                    keysRow[key] = !isSelected
                } else {
                    keysRow[key] = false
                }
            }

            val items = generateTuples(generatedPairs)
            checkButtonState()
            view?.showKeys(items)
        }
    }

    /*
        Валидация
        Если ключи под выбранным индексом не сходятся с ключами пользователя
        возвращаем false иначе true
     */
    private fun isKeysValid(): Boolean {
        // Преобразовываем ключи в структуру [индекс ключа, ключ]
        var isValid = true
        val selectedKeys = findSelectedKeys()

        selectedKeys.forEach { item ->
            val index = item.first
            val value = item.second
            if (phrases[index] != value) {
                isValid = false
            }
        }
        return isValid
    }

    private fun generateTuples(items: Map<Int, Map<String, Boolean>>): List<SecurityKeyTuple> {
        if (generatedTuples.isEmpty()) {
            items.keys.map { index ->
                val keys = generatedPairs[index]?.entries?.map { it.toPair() } ?: emptyList()
                generatedTuples.add(SecurityKeyTuple(index = index, keys = keys))
            }
            generatedTuples.shuffle()
            return generatedTuples
        }
        items.keys.map { index ->
            val keys = generatedPairs[index]?.entries?.map { it.toPair() } ?: emptyList()
            generatedTuples.first { it.index == index }.keys = keys
        }
        return generatedTuples

        // Для каждого индекса правильного ключа
        // находим сгенерированые ключи и преобразовываем их в пары
        // Дальше оборачиваем в модельку
    }

    private fun generateRandomIndex(range: Int): Int {
        val random = Random.nextInt(0, KEY_SIZE)
        return if (random in generatedIndexes) {
            generateRandomIndex(range)
        } else {
            generatedIndexes.add(random)
            random
        }
    }

    override fun validateSecurityKey() {
        launch {
            view?.showLoading(isLoading = true)
            if (isKeysValid()) {
                seedPhraseInteractor.createAndSaveAccount(
                    path = DerivationPath.BIP44CHANGE,
                    mnemonicPhrase = phrases,
                    walletIndex = 0
                )
                view?.navigateToReserve()

                onboardingAnalytics.logWalletCreated(lastScreenName = ScreenNames.OnBoarding.CREATE_MANUAL)
                return@launch
            }
            view?.showKeysDoesNotMatchError()
            onboardingAnalytics.logBackingUpRenew()
        }
            .invokeOnCompletion {
                clear()
                view?.showLoading(isLoading = false)
            }
    }

    override fun retry() {
        launch {
            clear()
            view?.onCleared()
            onboardingAnalytics.logBackingUpError()
        }
    }

    private fun clear() {
        generatedIndexes.clear()
        generatedPairs.clear()
        generatedTuples.clear()
        phrases.clear()
        view?.showEnabled(false)
    }

    private fun checkButtonState() {
        view?.showEnabled(findSelectedKeys().size == VERIFY_WORDS_COUNT)
    }

    private fun findSelectedKeys(): List<Pair<Int, String>> = kotlin.runCatching {
        generatedPairs.map { (keyIndex, keys) ->
            val entry = keys.entries.first { it.value }
            keyIndex to entry.key
        }
    }
        .getOrDefault(emptyList())
}
