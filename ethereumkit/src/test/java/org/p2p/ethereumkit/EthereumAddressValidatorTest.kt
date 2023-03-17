package org.p2p.ethereumkit

import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import org.p2p.ethereumkit.external.utils.EthereumUtils
import org.p2p.ethereumkit.internal.core.AddressValidator

class EthereumAddressValidatorTest {

    private val validAddresses = listOf(
        "0xff096cc01a7cc98ae3cd401c1d058baf991faf76",
        "0xff096cc01a7cc98ae3cd401c1d0512af991faf76",
        "0xc82d35fcc85c56d9b7c3b1d23e80c49598b2a513",
        "0xbed713fdb50a66dd5980cfaad82778236f0dc8df",
        "ff096cc01a7cc98ae3cd401c1d0512af991faf76",
    )

    private val invalidAddresses = listOf(
        "0xff096cc01a7cc98ae3cd401c1d058baf991faf",
        "1a7cc98ae3cd401c1d058baf991faf",
        "0xff096cc01123sfwfccc98ae3cd401c1d058baf991faf",
    )

    @Test
    fun `test-valid-addresses-for-ethereum`() {
        validAddresses.forEach { address ->
            assert(EthereumUtils.isValidAddress(address))
            AddressValidator.validate(address)
        }
    }

    @Test
    fun `test-invalid-addresses-for-ethereum`() {
        invalidAddresses.forEach { address ->
            assertFalse(EthereumUtils.isValidAddress(address))
            assertFailsWith(
                exceptionClass = AddressValidator.InvalidAddressLength::class,
                message = "Address is not valid",
                block = { AddressValidator.validate(address) }
            )
        }
    }
}
