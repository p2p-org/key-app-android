package org.p2p.wallet.utils.mnemoticgenerator;

public interface WordList {
    /**
     * Get a word in the word list.
     *
     * @param index Index of word in the word list [0..2047] inclusive.
     * @return the word from the list.
     */
    String getWord(final int index);

    /**
     * Get the space character for this language.
     *
     * @return a whitespace character.
     */
    char getSpace();
}
