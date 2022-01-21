package org.p2p.wallet.auth.ui.verify

import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import timber.log.Timber

private const val VERIFY_WORDS_COUNT = 4
private const val GENERATE_WORD_COUNT = 2
private const val KEY_SIZE = 24

class VerifySecurityKeyPresenter(
    private val secretKeyInteractor: SecretKeyInteractor
) : BasePresenter<VerifySecurityKeyContract.View>(),
    VerifySecurityKeyContract.Presenter {

    private var generatedPairs = HashMap<Int, HashMap<String, Boolean>>(KEY_SIZE)
    private var generatedIndexes = mutableListOf<Int>()
    private val phrases = mutableListOf<String>()

    override fun load(selectedKeys: List<String>, shuffle: Boolean) {

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
            val items = generateTuples(generatedPairs, shuffle)
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
    private fun isValid(): Boolean {
        // Преобразовываем ключи в структуру [индекс ключа, ключ]

        var isValid = true
        val selectedKeys = findSelectedKeys()
        Timber.tag("_____").d(findSelectedKeys().toString())

        selectedKeys.forEach { item ->
            val index = item.first
            val value = item.second
            if (phrases[index] != value) {
                isValid = false
            }
        }
        Timber.tag("_____").d(isValid.toString())
        return isValid
    }

    private fun generateTuples(
        items: Map<Int, Map<String, Boolean>>,
        shuffle: Boolean = false
    ): MutableList<SecurityKeyTuple> {
        val tuples = mutableListOf<SecurityKeyTuple>()
        // Для каждого индекса правильного ключа
        // находим сгенерированые ключи и преобразовываем их в пары
        // Дальше оборачиваем в модельку
        items.keys.forEach { index ->
            val keys = generatedPairs[index]?.entries?.map { it.toPair() } ?: emptyList()
            val tuple = SecurityKeyTuple(index = index, keys = keys)
            tuples.add(tuple)
        }
        return tuples
    }

    private fun generateRandomIndex(range: Int): Int {
        val random = (0 until range).random()
        return if (random in generatedIndexes) {
            generateRandomIndex(range)
        } else {
            generatedIndexes.add(random)
            random
        }
    }

    override fun validate() {
        launch {
            view?.showLoading(isLoading = true)
            if (isValid()) {
                secretKeyInteractor.createAndSaveAccount(
                    path = DerivationPath.BIP44CHANGE,
                    keys = phrases,
                    lookup = false
                )
                view?.navigateToReserve()
                return@launch
            }
            view?.showKeysDoesNotMatchError()
        }.invokeOnCompletion {
            clear()
            view?.showLoading(isLoading = false)
        }
    }

    override fun retry() {
        launch {
            clear()
            view?.onCleared()
        }
    }

    private fun clear() {
        generatedIndexes.clear()
        generatedPairs.clear()
        phrases.clear()
    }

    private fun checkButtonState() {
        view?.showEnabled(findSelectedKeys().size == VERIFY_WORDS_COUNT)
    }

    private fun findSelectedKeys(): List<Pair<Int, String>> =
        try {
            generatedPairs.map { (keyIndex, keys) ->
                val entry = keys.entries.first { it.value }
                Pair(keyIndex, entry.key)
            }
        } catch (e: NoSuchElementException) {
            emptyList()
        }
}