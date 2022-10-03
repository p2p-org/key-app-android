package org.p2p.solanaj.crypto

import org.p2p.solanaj.utils.SolanjLogger

/**
 * Utility class for Solana BIP-44 CUSTOM paths
 */
class Bip44CustomDerivation(
    private val purpose: Long,
    private val type: Long,
    private val account: Long,
    private val change: Long,
    private val isHardened: Boolean
) {

    private val hdKeyGenerator = HdKeyGenerator()

    private val solanaCoin: SolanaCoin = SolanaCoin()

    fun derivePrivateKeyFromSeed(seed: ByteArray): ByteArray = getPrivateKeyFromBip44(seed)

    private fun getPrivateKeyFromBip44(seed: ByteArray): ByteArray {
        val masterAddress = hdKeyGenerator.getAddressFromSeed(seed, solanaCoin)
        val purposeAddress = hdKeyGenerator.getAddress(masterAddress, purpose, isHardened)
        val coinTypeAddress = hdKeyGenerator.getAddress(purposeAddress, type, isHardened)
        val accountAddress = hdKeyGenerator.getAddress(coinTypeAddress, account, isHardened)
        val changeAddress = hdKeyGenerator.getAddress(accountAddress, change, isHardened)

        SolanjLogger.d("Generating HD address using path: $purpose/$type/$account/$change")
        return changeAddress.privateKey.privateKey
    }
}
