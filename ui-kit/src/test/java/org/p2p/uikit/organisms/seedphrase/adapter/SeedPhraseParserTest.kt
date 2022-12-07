package org.p2p.uikit.organisms.seedphrase.adapter

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.core.utils.emptyString

class SeedPhraseParserTest {
    private var seedPhraseParser = SeedPhraseParser()

    @Before
    fun beforeEach() {
        seedPhraseParser = SeedPhraseParser()
    }

    @Test
    fun `given empty string when parse then return empty seed phrase`() {
        // given
        val emptyStringText = emptyString()
        // when
        val resultSeedPhrase = seedPhraseParser.parse(emptyStringText)
        // then
        assertThat(resultSeedPhrase)
            .isEmpty()
    }

    @Test
    fun `given one finished word string when parse then return one word seed phrase`() {
        // given
        val oneWordString = "one"
        val finishOneWordAction = " "
        // when
        val resultSeedPhrase = seedPhraseParser.parse(oneWordString + finishOneWordAction)
        // then
        assertThat(resultSeedPhrase)
            .hasSize(1)
            .anyMatch { it.text == oneWordString }
    }

    @Test
    fun `given multiple word string when parse then return multiple word seed phrase`() {
        // given
        val wordString = "one two three four five six seven eight nine ten"
        val wordStringSize = wordString.split(" ").size
        // when
        val resultSeedPhrase = seedPhraseParser.parse(wordString)
        // then
        val expectedResult = wordString.split(" ").map { SeedPhraseWord(it, true) }
        assertThat(resultSeedPhrase)
            .hasSize(wordStringSize)
            .containsSequence(expectedResult)
    }

    @Test
    fun `given whitespaces word string when parse then return empty word seed phrase`() {
        // given
        val wordString = "          "
        // when
        val resultSeedPhrase = seedPhraseParser.parse(wordString)
        // then
        assertThat(resultSeedPhrase)
            .isEmpty()
    }

    @Test
    fun `given multiple word with whitespaces string when parse then return empty word seed phrase`() {
        // given
        val wordString = "one  two three"
        val wordStringSize = wordString
            .split(" ")
            .filter { it.isNotBlank() }
            .size
        // when
        val resultSeedPhrase = seedPhraseParser.parse(wordString)
        // then
        val expectedResult = wordString.split(" ")
            .filter { it.isNotBlank() }
            .map { SeedPhraseWord(it, true) }
        // then
        assertThat(resultSeedPhrase)
            .hasSize(wordStringSize)
            .containsSequence(expectedResult)
    }
}
