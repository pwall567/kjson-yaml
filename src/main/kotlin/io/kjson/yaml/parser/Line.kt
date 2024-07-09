/*
 * @(#) Line.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2020, 2021, 2023, 2024 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.kjson.yaml.parser

import net.pwall.text.TextMatcher

/**
 * An extension of [TextMatcher] to represent a YAML line.
 *
 * @author  Peter Wall
 */
class Line(val lineNumber: Int, line: String) : TextMatcher(line) {

    init {
        skip { it == ' ' }
    }

    /**
     *  Match a character followed by end of line or whitespace.  If the match is successful, the index will be
     *  positioned at the end of the whitespace.
     */
    private fun matchWithWhitespace(ch: Char): Boolean {
        if (!match(ch))
            return false
        if (isAtEnd)
            return true
        val charIndex = start
        if (matchSpaces()) {
            start = charIndex
            return true
        }
        index = charIndex
        return false
    }

    fun matchColon(): Boolean = matchWithWhitespace(':')

    fun matchDash(): Boolean = matchWithWhitespace('-')

    /**
     * Test whether character at index is a `#` preceded by space or start of line.  This does not increment the index
     * in the case of a match.
     */
    fun isComment(): Boolean {
        if (!match('#'))
            return false
        index = start
        return start == 0 || isSpace(getChar(start - 1))
    }

    fun matchSpace(): Boolean = match(' ') || match('\t')

    fun matchSpaces(): Boolean = matchSeq { isSpace(it) }

    fun skipSpaces() {
        skip { isSpace(it) }
    }

    fun skipToSpace() {
        skip { !isSpace(it) }
    }

    fun atEnd(): Boolean = isAtEnd || isComment()

    fun skipBackSpaces() {
        while (index > 0 && isSpace(getChar(index - 1)))
            index--
    }

    private fun isSpace(ch: Char): Boolean = ch == ' ' || ch == '\t'

    /**
     * Return the co-ordinates of the current location, to allow the `Line` object to be used in a [YAMLParseException].
     */
    override fun toString(): String = "$lineNumber:${index + 1}"

}
