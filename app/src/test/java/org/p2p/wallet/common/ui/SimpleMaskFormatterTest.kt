package org.p2p.wallet.common.ui


import org.junit.Test
import kotlin.test.assertEquals

class SimpleMaskFormatterTest {

    @Test
    fun `GIVEN phone number WHEN format THEN should format input using mask`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ###-####")

        // when
        val result = formatter.format("1234567890")

        // then
        assertEquals("(123) 456-7890", result)
    }

    @Test
    fun `GIVEN phone number with placeholders WHEN format THEN format input with stable placeholders`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ###-####", stablePlaceholders = true)

        // when
        val result = formatter.format("123")

        // then
        assertEquals("(123) ___-____", result)
    }

    @Test
    fun `GIVEN phone number with custom placeholders WHEN format THEN check input with custom placeholders`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ### ####", stablePlaceholders = true, stablePlaceholderChar = '-')

        // when
        val result = formatter.format("123")

        // then
        assertEquals("(123) --- ----", result)
    }

    @Test
    fun `GIVEN phone number with hyphen WHEN format THEN should format input with spaces and hyphen using mask`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ###-####")

        // when
        val result = formatter.format("123 456 7890")

        // then
        assertEquals("(123) 456-7890", result)
    }

    @Test
    fun `GIVEN non-standard phone number WHEN format THEN should format input with extra chars using mask`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ###-####")

        // when
        val result = formatter.format("1234567890 ext. 123")

        // then
        assertEquals("(123) 456-7890", result)
    }

    @Test
    fun `GIVEN phone number WHEN input is not standard THEN input with extra chars using mask and placeholders`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ###-####", stablePlaceholders = true)

        // when
        val result = formatter.format("1234567890 ext. 123")

        // then
        assertEquals("(123) 456-7890", result)
    }

    @Test
    fun `GIVEN empty phone WHEN format THEN should format empty input using mask with stable placeholders`() {
        // given
        val formatter = SimpleMaskFormatter("(###) ###-####", stablePlaceholders = true)

        // when
        val result = formatter.format("")

        // then
        assertEquals("(___) ___-____", result)
    }

    @Test
    fun `GIVEN phone number WHEN custom mask char THEN should format input using mask with different placeholder chars`() {
        // given
        val formatter = SimpleMaskFormatter("(***) ***-****", maskChar = '*', stablePlaceholderChar = '-')

        // when
        val result = formatter.format("1234567890")

        // then
        assertEquals("(123) 456-7890", result)
    }


}
