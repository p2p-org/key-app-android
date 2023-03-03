package org.p2p.solanaj.programs

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals
import org.p2p.solanaj.core.PublicKey

internal class MemoProgramTest {

    private val publicKey = PublicKey("11111111111111111111111111111111")

    @Test
    fun `memo instruction`() {
        val memo = "memo"
        val instruction = MemoProgram.createMemoInstruction(publicKey, memo)
        Assert.assertArrayEquals(memo.toByteArray(), instruction.data)
        assertEquals(instruction.keys.size, 1)
        assertEquals(instruction.keys.first().publicKey.toBase58(), publicKey.toBase58())
    }
}
