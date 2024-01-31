package org.p2p.wallet.send.model

import assertk.assertions.isInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.utils.assertThat

class SendButtonStateMinSolValidatorTest {

    private lateinit var solValidator: SendButtonStateMinSolValidator

    private val emptyRecipient = mockk<SearchResult.AddressFound> {
        every { isEmptyBalance }.returns(true)
    }

    private val nonEmptyRecipient = mockk<SearchResult.AddressFound> {
        every { isEmptyBalance }.returns(false)
    }

    @Test
    fun `GIVEN empty recipient THEN validator cases work`() {
        // entering amount that less than minRentExemption
        val minRentExemption = BigInteger("890000")
        // "0.015000000"
        val solAmount = BigInteger.valueOf(15_000_000)

        solValidator = SendButtonStateMinSolValidator(
            tokenToSend = createCustomSol(solAmount),
            minRentExemption = minRentExemption,
            recipient = emptyRecipient
        )

        // enter less than min rent
        var input = BigInteger.valueOf(880_000)
        var result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.EmptyRecipientMinAmountInvalid::class)

        // enter more == min rent
        input = "890000".toBigInteger()
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter more than min rent
        input = "890001".toBigInteger()
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter full amount
        input = solAmount
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter amount but mint rent is not left
        input = BigInteger.valueOf(14_999_000)
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.SenderNoRentExemptAmountLeft::class)
    }

    @Test
    fun `GIVEN non-empty recipient THEN validator cases work`() {
        val minRentExemption = BigInteger.valueOf(890_000)
        // "0.015000000"
        val solAmount = BigInteger.valueOf(15_000_000)

        solValidator = SendButtonStateMinSolValidator(
            tokenToSend = createCustomSol(solAmount),
            minRentExemption = minRentExemption,
            recipient = nonEmptyRecipient
        )

        // enter less than min rent
        var input = BigInteger.valueOf(880_000)
        var result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter equals min rent
        input = "890000".toBigInteger()
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter more than min rent
        input = "890001".toBigInteger()
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter full amount
        input = solAmount
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.AmountIsValid::class)

        // enter amount but mint rent is not left
        input = BigInteger.valueOf(14_999_000)
        result = solValidator.validateAmount(input)
        result.assertThat()
            .isInstanceOf(SendSolValidation.SenderNoRentExemptAmountLeft::class)
    }

    private fun createCustomSol(solAmount: BigInteger): Token.Active = mockk {
        every { isSOL }.returns(true)
        every { totalInLamports }.returns(solAmount)
        every { total }.returns(solAmount.fromLamports(9))
    }
}
