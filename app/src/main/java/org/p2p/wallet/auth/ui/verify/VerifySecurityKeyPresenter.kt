package org.p2p.wallet.auth.ui.verify

import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor

private const val VERIFY_WORDS_COUNT = 3
private const val GENERATE_WORD_COUNT = 2
private const val KEY_SIZE = 24

class VerifySecurityKeyPresenter(
    private val secretKeyInteractor: SecretKeyInteractor
) : BasePresenter<VerifySecurityKeyContract.View>(),
    VerifySecurityKeyContract.Presenter {

    private var generatedPairs = HashMap<Int, HashMap<String, Boolean>>(KEY_SIZE)
    private var generatedIndexes = mutableListOf<Int>()
    private val phrases = mutableListOf<String>()

    override fun load(selectedKeys: List<String>) {
        phrases.addAll(selectedKeys)
        launch {
            view?.showLoading(true)
            repeat(VERIFY_WORDS_COUNT) {
                val words = HashMap<String, Boolean>()
                var randomIndex = generateRandomIndex(selectedKeys.size)

                words[selectedKeys[randomIndex]] = false

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
            val keysRow = generatedPairs[keyIndex] ?: return
            val isSelected = keysRow[selectedKey] ?: false

            keysRow.keys.forEach { key ->
                if (key == selectedKey) {
                    keysRow[key] = !isSelected
                } else {
                    keysRow[key] = false
                }
            }

            val items = generateTuples(generatedPairs)
            view?.showKeys(items)
        }
    }

    private fun isValid(): Boolean {
        val selectedWords = generatedPairs.map { (keyIndex, keys) ->
            val entry = keys.entries.first { it.value }
            Pair(keyIndex, entry.key)
        }

        var isValid = true

        selectedWords.forEach { item ->
            val index = item.first
            val value = item.second
            if (phrases[index] != value) {
                isValid = false
            }
        }
        return isValid
    }

    private fun generateTuples(items: Map<Int, Map<String, Boolean>>): MutableList<SecurityKeyTuple> {
        val tuples = mutableListOf<SecurityKeyTuple>()
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
            view?.showLoading(isLoading = false)
        }
    }
}