/*
 * @(#) YAMLDocumentTest.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2020, 2021 Peter Wall
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

import io.kjson.JSONString
import io.kjson.pointer.JSONPointer

class YAMLDocumentTest {

    @Test fun `should create YAMLDocument`() {
        val testNode = JSONString("hello")
        val doc = YAMLDocument(testNode, emptyMap())
        expect(testNode) { doc.rootNode }
        expect(1) { doc.majorVersion }
        expect(2) { doc.minorVersion }
    }

    @Test fun `should get default tags`() {
        val document = YAML.parse(File("src/test/resources/alltypes.yaml"))
        expect("tag:yaml.org,2002:str") { document.getTag(JSONPointer("/string")) }
        expect("tag:yaml.org,2002:int") { document.getTag(JSONPointer("/int")) }
        expect("tag:yaml.org,2002:float") { document.getTag(JSONPointer("/decimal")) }
        expect("tag:yaml.org,2002:bool") { document.getTag(JSONPointer("/boolean")) }
        expect("tag:yaml.org,2002:null") { document.getTag(JSONPointer("/empty")) }
        expect("tag:yaml.org,2002:seq") { document.getTag(JSONPointer("/array")) }
        expect("tag:yaml.org,2002:map") { document.getTag(JSONPointer("/object")) }
    }

}
