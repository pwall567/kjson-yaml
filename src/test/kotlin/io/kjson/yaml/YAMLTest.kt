/*
 * @(#) YAMLTest.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2020, 2021, 2023 Peter Wall
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

package io.kjson.yaml

import kotlin.test.Test
import kotlin.test.expect

import java.io.File

import io.kjson.JSON.asObject
import io.kjson.JSON.asString

class YAMLTest {

    @Test fun `should use YAML object functions`() {
        val file = File("src/test/resources/keyblock.yaml")
        val result = YAML.parse(file)
        expect("data") { result.rootNode.asObject["key"].asString }
        val inputStream = file.inputStream()
        expect("data") { YAML.parse(inputStream).rootNode.asObject["key"].asString }
        val reader = file.reader()
        expect("data") { YAML.parse(reader).rootNode.asObject["key"].asString }
    }

    @Test fun `should use YAML object functions for multi-document streams`() {
        val file = File("src/test/resources/keyblock.yaml")
        val result = YAML.parseStream(file)
        expect("data") { result.single().rootNode.asObject["key"].asString }
        val inputStream = file.inputStream()
        expect("data") { YAML.parseStream(inputStream).single().rootNode.asObject["key"].asString }
        val reader = file.reader()
        expect("data") { YAML.parseStream(reader).single().rootNode.asObject["key"].asString }
    }

}
