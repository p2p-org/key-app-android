package org.p2p.ethereumkit

import org.junit.Test
import org.p2p.ethereumkit.external.utils.EthereumUtils

class EthereumAddressValidatorTest {

    @Test
    fun `validate addresses for Ethereum`() {
        val address1Valid = "0xff096cc01a7cc98ae3cd401c1d058baf991faf76"
        val address2Valid = "0xff096cc01a7cc98ae3cd401c1d0512af991faf76"
        val address3Valid = "0xc82d35fcc85c56d9b7c3b1d23e80c49598b2a513"
        val address4Valid = "0xbed713fdb50a66dd5980cfaad82778236f0dc8df"
        val address3ValidWithout0x = "ff096cc01a7cc98ae3cd401c1d0512af991faf76"
        val address1NotValid = "0xff096cc01a7cc98ae3cd401c1d058baf991faf"
        val address2NotValid = "0xff096cc01123sfwfccc98ae3cd401c1d058baf991faf"

        assert(EthereumUtils.isValidAddress(address1Valid))
        assert(EthereumUtils.isValidAddress(address2Valid))
        assert(EthereumUtils.isValidAddress(address3Valid))
        assert(EthereumUtils.isValidAddress(address4Valid))

        assert(EthereumUtils.isValidAddress(address3ValidWithout0x))

        assert(!EthereumUtils.isValidAddress(address1NotValid))
        assert(!EthereumUtils.isValidAddress(address2NotValid))
    }
}
