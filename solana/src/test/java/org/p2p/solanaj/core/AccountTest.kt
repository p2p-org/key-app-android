package org.p2p.solanaj.core

import io.mockk.InternalPlatformDsl.toStr
import org.junit.Test
import kotlin.test.assertEquals
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.utils.crypto.Base58Utils

class AccountTest {

    @Test
    fun `test restore account from secretKey`() {
        val secretKey =
            Base58Utils.decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs")

        val account = Account(secretKey)

        assertEquals("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo", account.publicKey.toBase58())
        assertEquals(64, account.keypair.size)
    }

    @Test
    fun `test restore account from seedPhrase`() {
        val phrase12 = "miracle pizza supply useful steak border same again youth silver access hundred".split(" ")
        val account12 = Account.fromBip32Mnemonic(phrase12, 0)
        assertEquals("3h1zGmCwsRJnVk5BuRNMLsPaQu1y2aqXqXDWYCgrp5UG", account12.publicKey.toBase58())

        val phrase24 =
            "budget resource fluid mutual ankle salt demise long burst sting doctor ozone risk magic wrap clap post pole jungle great update air interest abandon"
                .split(" ")
        val account24 = Account.fromBip44Mnemonic(phrase24, 0, DerivationPath.BIP44CHANGE)
        assertEquals("9avcmC97zLPwHKXiDz6GpXyjvPn9VcN3ggqM5gsRnjvv", account24.publicKey.toBase58())
    }

    @Test
    fun `test restore account from non-mnemonic seedPhrase, BIP44CHANGE, modern`() {
        val phrase1 = "y5HMpD^G639xab^8".toCharArray().map { it.toStr() }
        val account1 = Account.fromBip44Mnemonic(phrase1, 0, DerivationPath.BIP44CHANGE, includeSpaces = false)
        assertEquals("ADBWhBBb8di17PKfdXi4VhQwXnBBiUk8FJBNA6pwUdUc", account1.publicKey.toBase58())
        assertEquals(
            "5ruGYGx7Hco9gFoUAHo4nL9Ar7DwJZg9acQVofiYDof6pN8DFcv6vu1ikUbPmEoj7v8RvDGrXrcbDQ4c5jVqqqkC",
            account1.getEncodedKeyPair()
        )

        val phrase2 = "HelloWorld".toCharArray().map { it.toStr() }
        val account2 = Account.fromBip44Mnemonic(phrase2, 0, DerivationPath.BIP44CHANGE, includeSpaces = false)
        assertEquals("E3hcmVpjJEsLLYwWZKa2EdtwGrypR8QXfioTnnaJ2rwq", account2.publicKey.toBase58())
        assertEquals(
            "YixumKVXM5QwZsKc1k1y3niyWtaH51UxUByVpJMxGswXL6cAp556htJ8rBGPe3m1Q9PiKmtKVgrcMrMATpDZDzJ",
            account2.getEncodedKeyPair()
        )

        val phrase3 = "Lnj6uTyccG8WETn9".toCharArray().map { it.toStr() }
        val account3 = Account.fromBip44Mnemonic(phrase3, 0, DerivationPath.BIP44CHANGE, includeSpaces = false)
        assertEquals("3mxR3Z2kBkxDJTfYTPcRHbZMeD4CvQTrW8UHvT1FEHd8", account3.publicKey.toBase58())
        assertEquals(
            "54oi838sVS7EnEdUgwaf1hLcYxDNZ7HLwrkAAggE9i9rCEPb5hZi9MkEU4r3ReDZ1EGWRpv36zTx7ZeLFZW3E1PL",
            account3.getEncodedKeyPair()
        )
    }
}
