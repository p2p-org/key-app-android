package org.p2p.solanaj.crypto

import org.p2p.solanaj.utils.SolanjLogger

/**
 * Utility class for Solana BIP-44 CUSTOM paths
 */
class SolanaBip44Custom(
    private val purpose: Long,
    private val type: Long,
    private val account: Long,
    private val change: Long,
    private val isHardened: Boolean
) {
    private val hdKeyGenerator = HdKeyGenerator()

    private val solanaCoin: SolanaCoin = SolanaCoin()

    /**
     * Get a root account address for a given seed using bip44 to match sollet implementation
     *
     * @param seed  seed
     * @return PrivateKey
     */
    fun getPrivateKeyFromSeed(seed: ByteArray): ByteArray = getPrivateKeyFromBip44Seed(seed)

    private fun getPrivateKeyFromBip44Seed(seed: ByteArray): ByteArray {
        val masterAddress = hdKeyGenerator.getAddressFromSeed(seed, solanaCoin)
        val purposeAddress = hdKeyGenerator.getAddress(masterAddress, purpose, isHardened)
        val coinTypeAddress = hdKeyGenerator.getAddress(purposeAddress, type, isHardened)
        val accountAddress = hdKeyGenerator.getAddress(coinTypeAddress, account, isHardened)
        val changeAddress = hdKeyGenerator.getAddress(accountAddress, change, solanaCoin.alwaysHardened)

        SolanjLogger.d("Generating private key using path: $purpose/$type/$account/$change")
        return changeAddress.privateKey.privateKey
    }
}
