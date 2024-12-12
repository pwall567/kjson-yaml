/*
 * @(#) ParserTest.kt
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

import kotlin.test.Test
import kotlin.test.fail

import java.io.File
import java.io.StringReader
import java.math.BigDecimal

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeSameInstance
import io.kstuff.test.shouldBeType
import io.kstuff.test.shouldThrow

import io.kjson.JSON.asArray
import io.kjson.JSON.asDecimal
import io.kjson.JSON.asInt
import io.kjson.JSON.asObject
import io.kjson.JSON.asString
import io.kjson.JSONArray
import io.kjson.JSONBoolean
import io.kjson.JSONDecimal
import io.kjson.JSONInt
import io.kjson.JSONObject
import io.kjson.JSONString
import io.kjson.pointer.JSONPointer
import io.kjson.pointer.find
import io.kjson.yaml.YAML.floatTag
import io.kjson.yaml.YAML.intTag
import io.kjson.yaml.YAML.seqTag
import io.kjson.yaml.YAML.strTag
import io.kjson.yaml.YAMLException
import net.pwall.json.JSONSimple
import net.pwall.log.getLogger

class ParserTest {

    @Test fun `should return null document for empty file`() {
        val emptyFile = File("src/test/resources/empty.yaml")
        val result = Parser().parse(emptyFile)
        result.rootNode shouldBe null
    }

    @Test fun `should return single null document for empty file as multi-document stream`() {
        val emptyFile = File("src/test/resources/empty.yaml")
        val result = Parser().parseStream(emptyFile)
        result.size shouldBe 1
        result[0].rootNode shouldBe null
    }

    @Test fun `should return null document for empty file using InputStream`() {
        val inputStream = File("src/test/resources/empty.yaml").inputStream()
        val result = Parser().parse(inputStream)
        result.rootNode shouldBe null
    }

    @Test fun `should return single null document for empty file as multi-document stream using InputStream`() {
        val inputStream = File("src/test/resources/empty.yaml").inputStream()
        val result = Parser().parseStream(inputStream)
        result.size shouldBe 1
        result[0].rootNode shouldBe null
    }

    @Test fun `should return null document for empty file using Reader`() {
        val reader = File("src/test/resources/empty.yaml").reader()
        val result = Parser().parse(reader)
        result.rootNode shouldBe null
    }

    @Test fun `should return single null document for empty file as multi-document stream using Reader`() {
        val reader = File("src/test/resources/empty.yaml").reader()
        val result = Parser().parseStream(reader)
        result.size shouldBe 1
        result[0].rootNode shouldBe null
    }

    @Test fun `should process file starting with separator`() {
        val file = File("src/test/resources/separator1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc"
    }

    @Test fun `should process file starting with separator as multi-document stream`() {
        val file = File("src/test/resources/separator1.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 1
        log.debug { result[0].rootNode?.toJSON() }
        result[0].rootNode.asString shouldBe "abc"
    }

    @Test fun `should process file starting with separator and ending with terminator`() {
        val file = File("src/test/resources/separator2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc"
        result.majorVersion shouldBe 1
        result.minorVersion shouldBe 2
    }

    @Test fun `should process file starting with separator and ending with terminator as multi-document stream`() {
        val file = File("src/test/resources/separator2.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 1
        log.debug { result[0].rootNode?.toJSON() }
        result[0].rootNode.asString shouldBe "abc"
        result[0].majorVersion shouldBe 1
        result[0].minorVersion shouldBe 2
    }

    @Test fun `should process file starting with separator with comment`() {
        val file = File("src/test/resources/separator3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "Hello"
        result.majorVersion shouldBe 1
        result.minorVersion shouldBe 2
    }

    @Test fun `should process file starting with YAML directive`() {
        val file = File("src/test/resources/directive1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc"
        result.majorVersion shouldBe 1
        result.minorVersion shouldBe 2
    }

    @Test fun `should process file starting with YAML 1 1 directive`() {
        val file = File("src/test/resources/directive2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc"
        result.majorVersion shouldBe 1
        result.minorVersion shouldBe 1
    }

    @Test fun `should process YAML 1 1 file allowing old constant types`() {
        val file = File("src/test/resources/yaml11.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 4
            this[0] shouldBe JSONBoolean.TRUE
            this[1] shouldBe JSONBoolean.FALSE
            this[2] shouldBe JSONInt(511)
            this[3] shouldBe JSONString("Yes")
        }
    }

    @Test fun `should process YAML 1 2 file ignoring old constant types`() {
        val file = File("src/test/resources/yaml12.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 4
            this[0] shouldBe JSONString("Yes")
            this[1] shouldBe JSONString("No")
            this[2] shouldBe JSONInt(777)
            this[3] shouldBe JSONString("Yes")
        }
    }

    @Test fun `should process file starting with YAML directive with comment`() {
        val file = File("src/test/resources/directive4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abcdef"
        result.majorVersion shouldBe 1
        result.minorVersion shouldBe 2
    }

    @Test fun `should process file starting with YAML and TAG directives`() {
        val file = File("src/test/resources/tag1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc"
        result.getTag(JSONPointer.root) shouldBe strTag
    }

    @Test fun `should fail on YAML directive not 1 x`() {
        val file = File("src/test/resources/directive3.yaml")
        shouldThrow<YAMLException>("%YAML version must be 1.x, at 1:10") {
            Parser().parse(file)
        }
    }

    @Test fun `should fail on invalid TAG handle`() {
        val file = File("src/test/resources/tag99.yaml")
        shouldThrow<YAMLException>("Illegal tag handle on %TAG directive, at 2:6") {
            Parser().parse(file)
        }
    }

    @Test fun `should process plain scalar`() {
        val file = File("src/test/resources/plainscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "http://pwall.net/schema.json#/aaa"
    }

    @Test fun `should process double quoted scalar`() {
        val file = File("src/test/resources/doublequotedscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "a b \n \r \" A A \u2014 A \uD83D\uDE02"
    }

    @Test fun `should process multi-line scalar`() {
        val file = File("src/test/resources/multilinescalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc def ghi"
    }

    @Test fun `should process integer scalar`() {
        val file = File("src/test/resources/integerscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asInt shouldBe 123
    }

    @Test fun `should process decimal scalar`() {
        val file = File("src/test/resources/decimalscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asDecimal shouldBe BigDecimal("12345.67")
    }

    @Test fun `should process simple key-value`() {
        val file = File("src/test/resources/keyvalue.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject["key"].asString shouldBe "value"
    }

    @Test fun `should process simple key-integer`() {
        val file = File("src/test/resources/keyinteger.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject["key"].asInt shouldBe 123
    }

    @Test fun `should process simple block property`() {
        val file = File("src/test/resources/keyblock.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject["key"].asString shouldBe "data"
    }

    @Test fun `should process nested block property`() {
        val file = File("src/test/resources/nestedblock.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode.asObject) {
            size shouldBe 1
            with(this["key"].asObject) {
                size shouldBe 1
                this["nested"].asString shouldBe "inner"
            }
        }
    }

    @Test fun `should process multiple properties`() {
        val file = File("src/test/resources/multipleproperties.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let {
            it.size shouldBe 3
            it["prop1"].asString shouldBe "abc"
            it["prop2"].asString shouldBe " X "
            it["prop3"] shouldBe null
        }
    }

    @Test fun `should process array with single item`() {
        val file = File("src/test/resources/array1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray[0].asString shouldBe "abc"
    }

    @Test fun `should process array with two items`() {
        val file = File("src/test/resources/array2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let {
            it.size shouldBe 2
            it[0].asString shouldBe "abc"
            it[1].asString shouldBe "def"
        }
    }

    @Test fun `should process literal block scalar`() {
        val file = File("src/test/resources/literalblockscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject["abc"].asString shouldBe "hello\nworld\n"
    }

    @Test fun `should process flow sequence`() {
        val file = File("src/test/resources/flowsequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let {
            it.size shouldBe 2
            it[0].asString shouldBe "abc"
            it[1].asString shouldBe "def"
        }
    }

    @Test fun `should process more complex flow sequence`() {
        val file = File("src/test/resources/flowsequence2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let {
            it.size shouldBe 3
            it[0].asString shouldBe "abc def"
            it[1].asString shouldBe "ghi"
            it[2].asString shouldBe "jkl"
        }
    }

    @Test fun `should process flow sequence of mappings`() {
        val file = File("src/test/resources/flowsequence3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let { sequence ->
            sequence.size shouldBe 3
            sequence[0].asObject.let {
                it.size shouldBe 1
                it["abc"].asInt shouldBe 123
            }
            sequence[1].asObject.let {
                it.size shouldBe 1
                it["abc"].asInt shouldBe 456
            }
            sequence[2].asObject.let {
                it.size shouldBe 1
                it["def"].asInt shouldBe 789
            }
        }
    }

    @Test fun `should process nested flow sequences`() {
        val file = File("src/test/resources/flowsequence4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode.asArray) {
            size shouldBe 3
            with(this[0].asArray) {
                size shouldBe 1
                this[0].asString shouldBe "abc"
            }
            with(this[1].asArray) {
                size shouldBe 2
                this[0].asString shouldBe "def"
                this[1].asInt shouldBe 888
            }
            with(this[2].asArray) {
                size shouldBe 0
            }
        }
    }

    @Test fun `should process flow mapping`() {
        val file = File("src/test/resources/flowmapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode.asObject) {
            size shouldBe 2
            this["abcde"].asInt shouldBe 1234
            this["hello"].asString shouldBe "World!"
        }
    }

    @Test fun `should process array as property of mapping`() {
        val file = File("src/test/resources/arrayProperty1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["alpha"].asObject.let { inner ->
                inner.size shouldBe 1
                inner["beta"].asArray.let { array ->
                    array.size shouldBe 2
                    array[0].asInt shouldBe 123
                    array[1].asInt shouldBe 456
                }
            }
        }
    }

    @Test fun `should process array as property of mapping with comment line`() {
        val file = File("src/test/resources/arrayProperty2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["alpha"].asObject.let { inner ->
                inner.size shouldBe 1
                inner["beta"].asArray.let { array ->
                    array.size shouldBe 2
                    array[0].asInt shouldBe 123
                    array[1].asInt shouldBe 789
                }
            }
        }
    }

    @Test fun `should process nested properties`() {
        val file = File("src/test/resources/propertyProperty1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["alpha"].asObject.let { inner ->
                inner.size shouldBe 1
                inner["beta"].asObject.let { third ->
                    third.size shouldBe 2
                    third["gamma"].asInt shouldBe 123
                    third["delta"].asInt shouldBe 456
                }
            }
        }
    }

    @Test fun `should process nested properties with comment line`() {
        val file = File("src/test/resources/propertyProperty2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["alpha"].asObject.let { inner ->
                inner.size shouldBe 1
                inner["beta"].asObject.let { third ->
                    third.size shouldBe 2
                    third["gamma"].asInt shouldBe 123
                    third["epsilon"].asInt shouldBe 789
                }
            }
        }
    }

    @Test fun `should process comments in block scalars`() {
        val file = File("src/test/resources/blockwithcomments.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["outer"].asArray.let { array ->
                array.size shouldBe 3
                array[0].asObject.let { inner ->
                    inner.size shouldBe 2
                    inner["inner1"].asString shouldBe "xxx"
                    inner["inner2"].asString.trim() shouldBe "alpha beta"
                }
                array[1].asObject.let { inner ->
                    inner.size shouldBe 2
                    inner["inner1"].asString shouldBe "yyy"
                    inner["inner2"].asString.trim() shouldBe "gamma delta"
                }
                array[2].asObject.let { inner ->
                    inner.size shouldBe 2
                    inner["inner1"].asString shouldBe "zzz"
                    inner["inner2"].asString.trim() shouldBe "epsilon"
                }
            }
        }
    }

    @Test fun `should process explicit block mapping`() {
        val file = File("src/test/resources/explicitblockmapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["outer"].asObject.let { nested ->
                nested.size shouldBe 1
                nested["key1"].asObject.let { inner->
                    inner.size shouldBe 1
                    inner["inner1"].asString shouldBe "value1"
                }
            }
        }
    }

    @Test fun `should process block sequence with empty first line`() {
        val file = File("src/test/resources/blocksequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            outer.size shouldBe 1
            outer["outer"].asArray.let { array ->
                array.size shouldBe 2
                array[0].asObject.let { inner ->
                    inner.size shouldBe 1
                    inner["inner"].asString shouldBe "value1"
                }
                array[1].asObject.let { inner ->
                    inner.size shouldBe 1
                    inner["inner"].asString shouldBe "value2"
                }
            }
        }
    }

    @Test fun `should accept escaped newline in double-quoted scalar`() {
        val file = File("src/test/resources/multilinedoublequoted.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            obj.size shouldBe 2
            obj["first"].asString shouldBe "alphabet"
            obj["second"].asString shouldBe "alpha bet"
        }
    }

    @Test fun `should process combination of explicit and conventional block mapping`() {
        val file = File("src/test/resources/combinedblockmapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            obj.size shouldBe 2
            obj["first"].asObject.let { first ->
                first.size shouldBe 2
                first["key1"].asString shouldBe "value1"
                first["key2"].asString shouldBe "value2"
            }
            obj["second"].asObject.let { second ->
                second.size shouldBe 2
                second["key1"].asString shouldBe "value1"
                second["key2"].asString shouldBe "value2"
            }
        }
    }

    @Test fun `should process anchor and alias`() {
        val file = File("src/test/resources/anchor.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            obj.size shouldBe 2
            obj["abc"].asArray.let { abc ->
                abc.size shouldBe 3
                abc[0].asString shouldBe "first"
                abc[1].asString shouldBe "second"
                abc[2].asString shouldBe "third"
            }
            obj["def"] shouldBeSameInstance obj["abc"]
        }
    }

    @Test fun `should process anchor and alias in array`() {
        val file = File("src/test/resources/anchorArray.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let { arr ->
            arr.size shouldBe 3
            arr[0].asString shouldBe "abc"
            arr[1].asString shouldBe "def"
            arr[2] shouldBeSameInstance arr[0]
        }
    }

    @Test fun `should process anchor and alias in flow sequence`() {
        val file = File("src/test/resources/anchorFlowSequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            obj.size shouldBe 2
            obj["aaa"].asString shouldBe "a very long string"
            obj["bbb"].asArray.let { arr ->
                obj["aaa"] shouldBeSameInstance arr[0]
                obj["aaa"] shouldBeSameInstance arr[1]
            }
        }
    }

    @Test fun `should process anchor and alias in flow mapping`() {
        val file = File("src/test/resources/anchorFlowMapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            obj.size shouldBe 2
            obj["aaa"].asString shouldBe "a very long string"
            obj["bbb"].asObject.let { innerObject ->
                obj["aaa"] shouldBeSameInstance innerObject["first"]
                obj["aaa"] shouldBeSameInstance innerObject["second"]
            }
        }
    }

    @Test fun `should throw exception on unknown anchor`() {
        val file = File("src/test/resources/anchorError1.yaml")
        shouldThrow<YAMLParseException>("Can't locate alias \"unknown\", at 2:14") {
            Parser().parse(file)
        }
    }

    @Test fun `should throw exception on recursive anchor`() {
        val file = File("src/test/resources/anchorError2.yaml")
        shouldThrow<YAMLParseException>("Can't locate alias \"aaa\", at 2:12") {
            Parser().parse(file)
        }
    }

    @Test fun `should parse nested block scalar`() {
        val reader = StringReader(test1)
        val result = Parser().parse(reader)
        log.info { result.rootNode?.toJSON() }
        val rootNode = result.rootNode ?: fail("Result is null")
        JSONPointer("/aaa/bbb").find(rootNode).asString shouldBe "ccc:\n\nddd:"
    }

    @Test fun `should process shorthand tag`() {
        val file = File("src/test/resources/tag2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asString shouldBe "abc"
        result.getTag(JSONPointer.root) shouldBe "tag:kjson.io,2022:aaa"
    }

    @Test fun `should process shorthand tags in array`() {
        val file = File("src/test/resources/tag3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray[0].asString shouldBe "abc"
        result.getTag(JSONPointer("/0")) shouldBe "tag:kjson.io,2022:aaa"
        result.rootNode.asArray[1].asString shouldBe "def"
        result.getTag(JSONPointer("/1")) shouldBe "tag:kjson.io,2022:bbb"
        result.rootNode.asArray[2].asString shouldBe "ghi"
        result.getTag(JSONPointer("/2")) shouldBe "tag:kjson.io,2022:ccc"
    }

    @Test fun `should process shorthand tags with secondary handle`() {
        val file = File("src/test/resources/tag4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray[0].asString shouldBe "abc"
        result.getTag(JSONPointer("/0")) shouldBe "tag:kjson.io,2022:aaa"
        result.rootNode.asArray[1].asString shouldBe "def"
        result.getTag(JSONPointer("/1")) shouldBe "tag:kjson.io,2022:bbb"
        result.rootNode.asArray[2].asString shouldBe "ghi"
        result.getTag(JSONPointer("/2")) shouldBe "tag:kjson.io,2022:ccc"
    }

    @Test fun `should process shorthand tags with primary handle`() {
        val file = File("src/test/resources/tag5.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray[0].asString shouldBe "abc"
        result.getTag(JSONPointer("/0")) shouldBe "tag:kjson.io,2022:aaa"
        result.rootNode.asArray[1].asString shouldBe "def"
        result.getTag(JSONPointer("/1")) shouldBe "tag:kjson.io,2022:bbb"
        result.rootNode.asArray[2].asString shouldBe "ghi"
        result.getTag(JSONPointer("/2")) shouldBe "tag:kjson.io,2022:ccc"
    }

    @Test fun `should process shorthand tags with default secondary handle`() {
        val file = File("src/test/resources/tag6.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0].asString shouldBe "abc"
        result.getTag(JSONPointer("/0")) shouldBe "tag:yaml.org,2002:aaa"
        result.rootNode.asArray[1].asString shouldBe "def"
        result.getTag(JSONPointer("/1")) shouldBe "tag:yaml.org,2002:bbb"
        result.rootNode.asArray[2].asString shouldBe "ghi"
        result.getTag(JSONPointer("/2")) shouldBe "tag:yaml.org,2002:ccc"
    }

    @Test fun `should process shorthand tags with default primary handle`() {
        val file = File("src/test/resources/tag6a.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0].asString shouldBe "abc"
        result.getTag(JSONPointer("/0")) shouldBe "!aaa"
        result.rootNode.asArray[1].asString shouldBe "def"
        result.getTag(JSONPointer("/1")) shouldBe "!bbb"
        result.rootNode.asArray[2].asString shouldBe "ghi"
        result.getTag(JSONPointer("/2")) shouldBe "!ccc"
    }

    @Test fun `should process verbatim tags`() {
        val file = File("src/test/resources/tag7.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0].asString shouldBe "abc"
        result.getTag(JSONPointer("/0")) shouldBe "tag:kjson.io,2023:extra"
        result.rootNode.asArray[1].asString shouldBe "def"
        result.getTag(JSONPointer("/1")) shouldBe "!local"
        result.rootNode.asArray[2].asString shouldBe "ghi"
        result.getTag(JSONPointer("/2")) shouldBe strTag
    }

    @Test fun `should use tag to determine string data type`() {
        val file = File("src/test/resources/tag8.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0] shouldBe JSONInt(123)
        result.rootNode.asArray[1] shouldBe JSONString("456")
        result.rootNode.asArray[2] shouldBe JSONInt(789)
    }

    @Test fun `should use tag to determine int or float data type`() {
        val file = File("src/test/resources/tag9.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0] shouldBe JSONInt(123)
        result.rootNode.asArray[1] shouldBe JSONDecimal("456")
        result.rootNode.asArray[2] shouldBe JSONDecimal("789")
        result.rootNode.asArray[3] shouldBe JSONInt(987)
    }

    @Test fun `should add float tag for floating point constants`() {
        val file = File("src/test/resources/tag10.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0] shouldBe JSONString("abc")
        result.getTag(JSONPointer("/0")) shouldBe strTag
        result.rootNode.asArray[1] shouldBe JSONString(".nan")
        result.getTag(JSONPointer("/1")) shouldBe floatTag
        result.rootNode.asArray[2] shouldBe JSONString("-.Inf")
        result.getTag(JSONPointer("/2")) shouldBe floatTag
        result.rootNode.asArray[3] shouldBe JSONString(".nan")
        result.getTag(JSONPointer("/3")) shouldBe "tag:yaml.org,2002:xxx"
    }

    @Test fun `should add decode percent encoding in tag suffix`() {
        val file = File("src/test/resources/tag11.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.getTag(JSONPointer.root) shouldBe seqTag
        result.rootNode.asArray[0] shouldBe JSONString("abc")
        result.getTag(JSONPointer("/0")) shouldBe strTag
        result.rootNode.asArray[1] shouldBe JSONString("def")
        result.getTag(JSONPointer("/1")) shouldBe "tag:yaml.org,2002:a!"
    }

    @Test fun `should process object with multiple tags`() {
        val file = File("src/test/resources/tag20.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        for ((ptr, tag) in result.tagMap)
            log.debug { "Tag: [$ptr] -> $tag" }
        result.getTag(JSONPointer.root) shouldBe "tag:kjson.io,2023:aaa.bbb.ccc"
        with(result.rootNode.asObject) {
            size shouldBe 1
            with(this["outer"]) {
                shouldBeType<JSONObject>()
                size shouldBe 4
                with(this["array1"]) {
                    shouldBeType<JSONArray>()
                    size shouldBe 2
                    this[0] shouldBe JSONString("first")
                    result.getTag(JSONPointer("/outer/array1/0")) shouldBe "tag:example.com,2023:no"
                    this[1] shouldBe JSONString("second")
                    result.getTag(JSONPointer("/outer/array1/1")) shouldBe "tag:kjson.io,2023:xxx"
                }
                with(this["array2"]) {
                    shouldBeType<JSONArray>()
                    size shouldBe 3
                    this[0] shouldBe JSONString("alpha")
                    result.getTag(JSONPointer("/outer/array2/0")) shouldBe "tag:kjson.io,2023:aaa"
                    this[1] shouldBe JSONString("beta")
                    result.getTag(JSONPointer("/outer/array2/1")) shouldBe "tag:example.com,2023:bbb"
                    this[2] shouldBe JSONString("gamma")
                    result.getTag(JSONPointer("/outer/array2/2")) shouldBe strTag
                }
                result.getTag(JSONPointer("/outer/array2")) shouldBe "!temp"
                this["inner"] shouldBe JSONString("abc")
                result.getTag(JSONPointer("/outer/inner")) shouldBe "tag:nowhere.io,2023:extra"
                with(this["obj1"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 2
                    this["f1"] shouldBe JSONInt(123)
                    result.getTag(JSONPointer("/outer/obj1/f1")) shouldBe "tag:kjson.io,2023:yyy"
                    this["f2"] shouldBe JSONInt(456)
                    result.getTag(JSONPointer("/outer/obj1/f2")) shouldBe intTag
                }
                result.getTag(JSONPointer("/outer/obj1")) shouldBe "tag:kjson.io,2023:zzz"
            }
        }
    }

    @Test fun `should process multi-document stream`() {
        val file = File("src/test/resources/multi1.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 2
        log.debug { result[0].rootNode?.toJSON() }
        result[0].rootNode.asString shouldBe "abc"
        log.debug { result[1].rootNode?.toJSON() }
        result[1].rootNode.asString shouldBe "def"
    }

    @Test fun `should process multi-document stream using terminator`() {
        val file = File("src/test/resources/multi2.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 2
        log.debug { result[0].rootNode?.toJSON() }
        result[0].rootNode.asString shouldBe "abc"
        log.debug { result[1].rootNode?.toJSON() }
        result[1].rootNode.asString shouldBe "def"
    }

    @Test fun `should process multi-document stream using terminator and directives`() {
        val file = File("src/test/resources/multi3.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 2
        log.debug { result[0].rootNode?.toJSON() }
        result[0].rootNode.asString shouldBe "abc"
        log.debug { result[1].rootNode?.toJSON() }
        result[1].rootNode.asString shouldBe "def"
    }

    @Test fun `should throw exception when using tag from previous document`() {
        val file = File("src/test/resources/multi4.yaml")
        shouldThrow<YAMLParseException>("Tag handle !t1! not declared, at 8:5") {
            Parser().parseStream(file)
        }
    }

    @Test fun `should process mixed flow sequence content`() {
        val file = File("src/test/resources/mixedFlowSequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let {
            it.size shouldBe 1
            it["enum"].asArray.let { seq ->
                seq.size shouldBe 3
                seq[0].asString shouldBe "ABC"
                seq[1].asString shouldBe "123"
                seq[2].asString shouldBe "XYZ"
            }
        }
    }

    @Test fun `should process mixed flow sequence with flow mapping`() {
        val file = File("src/test/resources/mixedFlowSequenceWithMapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let {
            it.size shouldBe 1
            it["example"].asArray.let { seq ->
                seq.size shouldBe 1
                seq[0].asObject.let { mapping ->
                    mapping.size shouldBe 1
                    mapping["prop1"].asString shouldBe "1.5"
                }
            }
        }
    }

    @Test fun `should process example JSON schema`() {
        val file = File("src/test/resources/example.schema.yaml")
        val result = Parser().parse(file)
        log.debug { JSONSimple.format(result.rootNode) }
        result.rootNode.asObject.let {
            it.size shouldBe 6
            it["\$schema"].asString shouldBe "http://json-schema.org/draft/2019-09/schema"
            it["\$id"].asString shouldBe "http://pwall.net/test"
            it["title"].asString shouldBe "Product"
            it["type"].asString shouldBe "object"
            it["required"].asArray.let { required ->
                required.size shouldBe 3
                required[0].asString shouldBe "id"
                required[1].asString shouldBe "name"
                required[2].asString shouldBe "price"
            }
            it["properties"].asObject.let { properties ->
                properties.size shouldBe 5
                properties["id"].asObject.let { id ->
                    id.size shouldBe 2
                    id["type"].asString shouldBe "number"
                    id["description"].asString shouldBe "Product identifier"
                }
                properties["name"].asObject.let { name ->
                    name.size shouldBe 2
                    name["type"].asString shouldBe "string"
                    name["description"].asString shouldBe "Name of the product"
                }
                properties["tags"].asObject.let { tags ->
                    tags.size shouldBe 2
                    tags["type"].asString shouldBe "array"
                    tags["items"].asObject.let { items ->
                        items.size shouldBe 1
                        items["type"].asString shouldBe "string"
                    }
                }
                properties["stock"].asObject.let { stock ->
                    stock.size shouldBe 2
                    stock["type"].asString shouldBe "object"
                    stock["properties"].asObject.let { properties2 ->
                        properties2.size shouldBe 2
                        properties2["warehouse"].asObject.let { warehouse ->
                            warehouse.size shouldBe 1
                            warehouse["type"].asString shouldBe "number"
                        }
                        properties2["retail"].asObject.let { retail ->
                            retail.size shouldBe 1
                            retail["type"].asString shouldBe "number"
                        }
                    }
                }
            }
        }
    }

    companion object {

        val log = getLogger()

        @Suppress("ConstPropertyName")
        const val test1 = """
aaa:
  bbb: |-
    ccc:

    ddd:
"""

    }

}
