/*
 * @(#) YAMLDocumentTest.kt
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

package io.kjson.yaml

import kotlin.test.Test

import java.io.File

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldThrow

import io.kjson.JSONString
import io.kjson.pointer.JSONPointer

class YAMLDocumentTest {

    @Test fun `should create YAMLDocument`() {
        val testNode = JSONString("hello")
        val doc = YAMLDocument(testNode, emptyMap())
        doc.rootNode shouldBe testNode
        doc.majorVersion shouldBe 1
        doc.minorVersion shouldBe 2
    }

    @Test fun `should get default tags`() {
        val document = YAML.parse(File("src/test/resources/alltypes.yaml"))
        document.getTag(JSONPointer("/string")) shouldBe "tag:yaml.org,2002:str"
        document.getTag(JSONPointer("/int")) shouldBe "tag:yaml.org,2002:int"
        document.getTag(JSONPointer("/decimal")) shouldBe "tag:yaml.org,2002:float"
        document.getTag(JSONPointer("/boolean")) shouldBe "tag:yaml.org,2002:bool"
        document.getTag(JSONPointer("/empty")) shouldBe "tag:yaml.org,2002:null"
        document.getTag(JSONPointer("/array")) shouldBe "tag:yaml.org,2002:seq"
        document.getTag(JSONPointer("/object")) shouldBe "tag:yaml.org,2002:map"
    }

    @Test fun `should throw exception getting tag for unknown node`() {
        val document = YAML.parse(File("src/test/resources/alltypes.yaml"))
        shouldThrow<YAMLException>("Node does not exist - /rubbish") {
            document.getTag(JSONPointer("/rubbish"))
        }
    }

}
