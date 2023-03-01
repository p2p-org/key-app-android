package org.p2p.ethereumkit.external.utils

import java.math.BigInteger

const val ADDRESS_SIZE = 160
const val ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE shr 2

object EthereumUtils {

    /**
     * convert string key to BigInteger to use in keyPair
     **/
    fun toBigInt(hexValue: String): BigInteger {
        val cleanValue = cleanHexPrefix(hexValue)
        return toBigIntNoPrefix(cleanValue)
    }

    fun isValidAddress(input: String): Boolean {
        return isValidAddress(input, ADDRESS_LENGTH_IN_HEX)
    }

    fun isValidAddress(input: String, addressLength: Int): Boolean {
        val cleanInput = cleanHexPrefix(input)
        try {
            toBigIntNoPrefix(cleanInput)
        } catch (e: NumberFormatException) {
            return false
        }
        return cleanInput.length == addressLength
    }

    private fun toBigIntNoPrefix(hexValue: String): BigInteger {
        return BigInteger(hexValue, 16)
    }

    private fun cleanHexPrefix(input: String): String {
        return if (containsHexPrefix(input)) {
            input.substring(2)
        } else {
            input
        }
    }

    private fun containsHexPrefix(input: String): Boolean {
        return input.isNotEmpty() &&
            input.length > 1 &&
            input[0] == '0' &&
            input[1] == 'x'
    }
}
