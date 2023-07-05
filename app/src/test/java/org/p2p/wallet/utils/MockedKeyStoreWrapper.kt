package org.p2p.wallet.utils

import io.mockk.every
import io.mockk.mockk
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper

/**
 * Returns the original value to encode / decode without any changes
 * no need for encoding stuff in Unit tests
 */
object MockedKeyStoreWrapper {
    fun get(): KeyStoreWrapper = mockk {
        every { encode(any<String>(), any()) }.answers {
            secondArg()
        }
        every { decode(any<String>(), any()) }.answers {
            secondArg()
        }
        every { deleteKeyAlias(any()) }.returns(Unit)
    }
}
