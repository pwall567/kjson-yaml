/*
 * @(#) YAMLParseExceptionTest.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2020, 2021, 2024 Peter Wall
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

import kotlin.test.Test
import kotlin.test.expect

class YAMLParseExceptionTest {

    @Test fun `should create YAMLParseException`() {
        val line = Line(8, "  Hello  ")
        val ype = YAMLParseException("Something went wrong", line)
        expect("Something went wrong") { ype.text }
        expect("Something went wrong, at 8:3") { ype.message }
        expect(8) { ype.line.lineNumber }
    }

    @Test fun `should create YAMLParseException with nested exception`() {
        val line = Line(123, "XXX")
        val nested = IllegalArgumentException("Dummy")
        val ype = YAMLParseException("Oh no!", line).withCause(nested)
        expect("Oh no!") { ype.text }
        expect("Oh no!, at 123:1") { ype.message }
        expect(nested) { ype.cause }
    }

}
