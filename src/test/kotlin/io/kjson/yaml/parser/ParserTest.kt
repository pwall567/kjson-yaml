/*
 * @(#) ParserTest.kt
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

package io.kjson.yaml.parser

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.expect
import kotlin.test.fail

import java.io.File
import java.io.StringReader
import java.math.BigDecimal

import io.kjson.JSON.asArray
import io.kjson.JSON.asDecimal
import io.kjson.JSON.asInt
import io.kjson.JSON.asObject
import io.kjson.JSON.asString
import io.kjson.pointer.JSONPointer
import io.kjson.yaml.YAMLException
import net.pwall.json.JSONSimple
import net.pwall.log.getLogger

class ParserTest {

    @Test fun `should return null document for empty file`() {
        val emptyFile = File("src/test/resources/empty.yaml")
        val result = Parser().parse(emptyFile)
        expect(null) { result.rootNode }
    }

    @Test fun `should return null document for empty file as InputStream`() {
        val inputStream = File("src/test/resources/empty.yaml").inputStream()
        val result = Parser().parse(inputStream)
        expect(null) { result.rootNode }
    }

    @Test fun `should return null document for empty file as Reader`() {
        val reader = File("src/test/resources/empty.yaml").reader()
        val result = Parser().parse(reader)
        expect(null) { result.rootNode }
    }

    @Test fun `should process file starting with separator`() {
        val file = File("src/test/resources/separator1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asString }
    }

    @Test fun `should process file starting with separator and ending with terminator`() {
        val file = File("src/test/resources/separator2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asString }
        expect(1) { result.majorVersion }
        expect(2) { result.minorVersion }
    }

    @Test fun `should process file starting with separator with comment`() {
        val file = File("src/test/resources/separator3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("Hello") { result.rootNode.asString }
        expect(1) { result.majorVersion }
        expect(2) { result.minorVersion }
    }

    @Test fun `should process file starting with YAML directive`() {
        val file = File("src/test/resources/directive1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asString }
        expect(1) { result.majorVersion }
        expect(2) { result.minorVersion }
    }

    @Test fun `should process file starting with YAML 1 1 directive`() {
        val file = File("src/test/resources/directive2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asString }
        expect(1) { result.majorVersion }
        expect(1) { result.minorVersion }
    }

    @Test fun `should process file starting with YAML directive with comment`() {
        val file = File("src/test/resources/directive4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abcdef") { result.rootNode.asString }
        expect(1) { result.majorVersion }
        expect(2) { result.minorVersion }
    }

    @Test fun `should process file starting with YAML and TAG directives`() {
        val file = File("src/test/resources/tag1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asString }
    }

    @Test fun `should fail on YAML directive not 1 x`() {
        val file = File("src/test/resources/directive3.yaml")
        assertFailsWith<YAMLException> { Parser().parse(file) }.let {
            expect("%YAML version must be 1.x at 1:10") { it.message }
        }
    }

    @Test fun `should fail on invalid TAG handle`() {
        val file = File("src/test/resources/tag99.yaml")
        assertFailsWith<YAMLException> { Parser().parse(file) }.let {
            expect("Illegal tag handle on %TAG directive at 2:6") { it.message }
        }
    }

    @Test fun `should process plain scalar`() {
        val file = File("src/test/resources/plainscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("http://pwall.net/schema.json#/aaa") { result.rootNode.asString }
    }

    @Test fun `should process double quoted scalar`() {
        val file = File("src/test/resources/doublequotedscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("a b \n \r \" A A \u2014 A \uD83D\uDE02") { result.rootNode.asString }
    }

    @Test fun `should process multi-line scalar`() {
        val file = File("src/test/resources/multilinescalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc def ghi") { result.rootNode.asString }
    }

    @Test fun `should process integer scalar`() {
        val file = File("src/test/resources/integerscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect(123) { result.rootNode.asInt }
    }

    @Test fun `should process decimal scalar`() {
        val file = File("src/test/resources/decimalscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect(BigDecimal("12345.67")) { result.rootNode.asDecimal }
    }

    @Test fun `should process simple key-value`() {
        val file = File("src/test/resources/keyvalue.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("value") { result.rootNode.asObject["key"].asString }
    }

    @Test fun `should process simple key-integer`() {
        val file = File("src/test/resources/keyinteger.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect(123) { result.rootNode.asObject["key"].asInt }
    }

    @Test fun `should process simple block property`() {
        val file = File("src/test/resources/keyblock.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("data") { result.rootNode.asObject["key"].asString }
    }

    @Test fun `should process nested block property`() {
        val file = File("src/test/resources/nestedblock.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let {
            expect(1) { it.size }
            it["key"].asObject.let {
                expect(1) { it.size }
                expect("inner") { it["nested"].asString }
            }
        }
    }

    @Test fun `should process multiple properties`() {
        val file = File("src/test/resources/multipleproperties.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let {
            expect(3) { it.size }
            expect("abc") { it["prop1"].asString }
            expect(" X ") { it["prop2"].asString }
            assertNull(it["prop3"])
        }
    }

    @Test fun `should process array with single item`() {
        val file = File("src/test/resources/array1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asArray[0].asString }
    }

    @Test fun `should process array with two items`() {
        val file = File("src/test/resources/array2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let {
            expect(2) { it.size }
            expect("abc") { it[0].asString }
            expect("def") { it[1].asString }
        }
    }

    @Test fun `should process literal block scalar`() {
        val file = File("src/test/resources/literalblockscalar.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("hello\nworld\n") { result.rootNode.asObject["abc"].asString }
    }

    @Test fun `should process flow sequence`() {
        val file = File("src/test/resources/flowsequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let {
            expect(2) { it.size }
            expect("abc") { it[0].asString }
            expect("def") { it[1].asString }
        }
    }

    @Test fun `should process more complex flow sequence`() {
        val file = File("src/test/resources/flowsequence2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let {
            expect(3) { it.size }
            expect("abc def") { it[0].asString }
            expect("ghi") { it[1].asString }
            expect("jkl") { it[2].asString }
        }
    }

    @Test fun `should process flow sequence of mappings`() {
        val file = File("src/test/resources/flowsequence3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let { sequence ->
            expect(3) { sequence.size }
            sequence[0].asObject.let {
                expect(1) { it.size }
                expect(123) { it["abc"].asInt }
            }
            sequence[1].asObject.let {
                expect(1) { it.size }
                expect(456) { it["abc"].asInt }
            }
            sequence[2].asObject.let {
                expect(1) { it.size }
                expect(789) { it["def"].asInt }
            }
        }
    }

    @Test fun `should process nested flow sequences`() {
        val file = File("src/test/resources/flowsequence4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let { sequence ->
            expect(3) { sequence.size }
            sequence[0].asArray.let {
                expect(1) { it.size }
                expect("abc") { it[0].asString }
            }
            sequence[1].asArray.let {
                expect(2) { it.size }
                expect("def") { it[0].asString }
                expect(888) { it[1].asInt }
            }
            sequence[2].asArray.let {
                expect(0) { it.size }
            }
        }
    }

    @Test fun `should process flow mapping`() {
        val file = File("src/test/resources/flowmapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let {
            expect(2) { it.size }
            expect(1234) { it["abcde"].asInt }
            expect("World!") { it["hello"].asString }
        }
    }

    @Test fun `should process array as property of mapping`() {
        val file = File("src/test/resources/arrayProperty1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["alpha"].asObject.let { inner ->
                expect(1) { inner.size }
                inner["beta"].asArray.let { array ->
                    expect(2) { array.size }
                    expect(123) { array[0].asInt }
                    expect(456) { array[1].asInt }
                }
            }
        }
    }

    @Test fun `should process array as property of mapping with comment line`() {
        val file = File("src/test/resources/arrayProperty2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["alpha"].asObject.let { inner ->
                expect(1) { inner.size }
                inner["beta"].asArray.let { array ->
                    expect(2) { array.size }
                    expect(123) { array[0].asInt }
                    expect(789) { array[1].asInt }
                }
            }
        }
    }

    @Test fun `should process nested properties`() {
        val file = File("src/test/resources/propertyProperty1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["alpha"].asObject.let { inner ->
                expect(1) { inner.size }
                inner["beta"].asObject.let { third ->
                    expect(2) { third.size }
                    expect(123) { third["gamma"].asInt }
                    expect(456) { third["delta"].asInt }
                }
            }
        }
    }

    @Test fun `should process nested properties with comment line`() {
        val file = File("src/test/resources/propertyProperty2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["alpha"].asObject.let { inner ->
                expect(1) { inner.size }
                inner["beta"].asObject.let { third ->
                    expect(2) { third.size }
                    expect(123) { third["gamma"].asInt }
                    expect(789) { third["epsilon"].asInt }
                }
            }
        }
    }

    @Test fun `should process comments in block scalars`() {
        val file = File("src/test/resources/blockwithcomments.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["outer"].asArray.let { array ->
                expect(3) { array.size }
                array[0].asObject.let { inner ->
                    expect(2) { inner.size }
                    expect("xxx") { inner["inner1"].asString }
                    expect("alpha beta") { inner["inner2"].asString.trim() }
                }
                array[1].asObject.let { inner ->
                    expect(2) { inner.size }
                    expect("yyy") { inner["inner1"].asString }
                    expect("gamma delta") { inner["inner2"].asString.trim() }
                }
                array[2].asObject.let { inner ->
                    expect(2) { inner.size }
                    expect("zzz") { inner["inner1"].asString }
                    expect("epsilon") { inner["inner2"].asString.trim() }
                }
            }
        }
    }

    @Test fun `should process explicit block mapping`() {
        val file = File("src/test/resources/explicitblockmapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["outer"].asObject.let { nested ->
                expect(1) { nested.size }
                nested["key1"].asObject.let { inner->
                    expect(1) { inner.size }
                    expect("value1") { inner["inner1"].asString }
                }
            }
        }
    }

    @Test fun `should process block sequence with empty first line`() {
        val file = File("src/test/resources/blocksequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { outer ->
            expect(1) { outer.size }
            outer["outer"].asArray.let { array ->
                expect(2) { array.size }
                array[0].asObject.let { inner ->
                    expect(1) { inner.size }
                    expect("value1") { inner["inner"].asString }
                }
                array[1].asObject.let { inner ->
                    expect(1) { inner.size }
                    expect("value2") { inner["inner"].asString }
                }
            }
        }
    }

    @Test fun `should accept escaped newline in double-quoted scalar`() {
        val file = File("src/test/resources/multilinedoublequoted.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            expect(2) { obj.size }
            expect("alphabet") { obj["first"].asString }
            expect("alpha bet") { obj["second"].asString }
        }
    }

    @Test fun `should process combination of explicit and conventional block mapping`() {
        val file = File("src/test/resources/combinedblockmapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            expect(2) { obj.size }
            obj["first"].asObject.let { first ->
                expect(2) { first.size }
                expect("value1") { first["key1"].asString }
                expect("value2") { first["key2"].asString }
            }
            obj["second"].asObject.let { second ->
                expect(2) { second.size }
                expect("value1") { second["key1"].asString }
                expect("value2") { second["key2"].asString }
            }
        }
    }

    @Test fun `should process anchor and alias`() {
        val file = File("src/test/resources/anchor.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            expect(2) { obj.size }
            obj["abc"].asArray.let { abc ->
                expect(3) { abc.size }
                expect("first") { abc[0].asString }
                expect("second") { abc[1].asString }
                expect("third") { abc[2].asString }
            }
            assertSame(obj["abc"], obj["def"])
        }
    }

    @Test fun `should process anchor and alias in array`() {
        val file = File("src/test/resources/anchorArray.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asArray.let { arr ->
            expect(3) { arr.size }
            expect("abc") { arr[0].asString }
            expect("def") { arr[1].asString }
            assertSame(arr[0], arr[2])
        }
    }

    @Test fun `should process anchor and alias in flow sequence`() {
        val file = File("src/test/resources/anchorFlowSequence.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            expect(2) { obj.size }
            expect("a very long string") { obj["aaa"].asString }
            obj["bbb"].asArray.let { arr ->
                assertSame(arr[0], obj["aaa"])
                assertSame(arr[1], obj["aaa"])
            }
        }
    }

    @Test fun `should process anchor and alias in flow mapping`() {
        val file = File("src/test/resources/anchorFlowMapping.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode.asObject.let { obj ->
            expect(2) { obj.size }
            expect("a very long string") { obj["aaa"].asString }
            obj["bbb"].asObject.let { innerObject ->
                assertSame(innerObject["first"], obj["aaa"])
                assertSame(innerObject["second"], obj["aaa"])
            }
        }
    }

    @Test fun `should throw exception on unknown anchor`() {
        val file = File("src/test/resources/anchorError1.yaml")
        assertFailsWith<YAMLParseException> { Parser().parse(file) }.let {
            expect("Can't locate alias \"unknown\" at 2:14") { it.message }
        }
    }

    @Test fun `should throw exception on recursive anchor`() {
        val file = File("src/test/resources/anchorError2.yaml")
        assertFailsWith<YAMLParseException> { Parser().parse(file) }.let {
            expect("Can't locate alias \"aaa\" at 2:12") { it.message }
        }
    }

    @Test fun `should parse nested block scalar`() {
        val reader = StringReader(test1)
        val result = Parser().parse(reader)
        log.info { result.rootNode?.toJSON() }
        val rootNode = result.rootNode ?: fail("Result is null")
        expect("ccc:\n\nddd:") { JSONPointer.find("/aaa/bbb", rootNode).asString }
    }

    @Test fun `should process shorthand tag`() {
        val file = File("src/test/resources/tag2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asString }
        expect("tag:kjson.io,2022:aaa") { result.getTag(JSONPointer.root) }
    }

    @Test fun `should process shorthand tags in array`() {
        val file = File("src/test/resources/tag3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asArray[0].asString }
        expect("tag:kjson.io,2022:aaa") { result.getTag(JSONPointer("/0")) }
        expect("def") { result.rootNode.asArray[1].asString }
        expect("tag:kjson.io,2022:bbb") { result.getTag(JSONPointer("/1")) }
        expect("ghi") { result.rootNode.asArray[2].asString }
        expect("tag:kjson.io,2022:ccc") { result.getTag(JSONPointer("/2")) }
    }

    @Test fun `should process shorthand tags with secondary handle`() {
        val file = File("src/test/resources/tag4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asArray[0].asString }
        expect("tag:kjson.io,2022:aaa") { result.getTag(JSONPointer("/0")) }
        expect("def") { result.rootNode.asArray[1].asString }
        expect("tag:kjson.io,2022:bbb") { result.getTag(JSONPointer("/1")) }
        expect("ghi") { result.rootNode.asArray[2].asString }
        expect("tag:kjson.io,2022:ccc") { result.getTag(JSONPointer("/2")) }
    }

    @Test fun `should process shorthand tags with primary handle`() {
        val file = File("src/test/resources/tag5.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("abc") { result.rootNode.asArray[0].asString }
        expect("tag:kjson.io,2022:aaa") { result.getTag(JSONPointer("/0")) }
        expect("def") { result.rootNode.asArray[1].asString }
        expect("tag:kjson.io,2022:bbb") { result.getTag(JSONPointer("/1")) }
        expect("ghi") { result.rootNode.asArray[2].asString }
        expect("tag:kjson.io,2022:ccc") { result.getTag(JSONPointer("/2")) }
    }

    @Test fun `should process shorthand tags with default secondary handle`() {
        val file = File("src/test/resources/tag6.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("tag:yaml.org,2002:seq") { result.getTag(JSONPointer.root) }
        expect("abc") { result.rootNode.asArray[0].asString }
        expect("tag:yaml.org,2002:aaa") { result.getTag(JSONPointer("/0")) }
        expect("def") { result.rootNode.asArray[1].asString }
        expect("tag:yaml.org,2002:bbb") { result.getTag(JSONPointer("/1")) }
        expect("ghi") { result.rootNode.asArray[2].asString }
        expect("tag:yaml.org,2002:ccc") { result.getTag(JSONPointer("/2")) }
    }

    @Test fun `should process verbatim tags`() {
        val file = File("src/test/resources/tag7.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect("tag:yaml.org,2002:seq") { result.getTag(JSONPointer.root) }
        expect("abc") { result.rootNode.asArray[0].asString }
        expect("tag:kjson.io,2023:extra") { result.getTag(JSONPointer("/0")) }
        expect("def") { result.rootNode.asArray[1].asString }
        expect("!local") { result.getTag(JSONPointer("/1")) }
        expect("ghi") { result.rootNode.asArray[2].asString }
        expect("tag:yaml.org,2002:str") { result.getTag(JSONPointer("/2")) }
    }

    @Test fun `should process example JSON schema`() {
        val file = File("src/test/resources/example.schema.yaml")
        val result = Parser().parse(file)
        log.debug { JSONSimple.format(result.rootNode) }
        result.rootNode.asObject.let {
            expect(6) { it.size }
            expect("http://json-schema.org/draft/2019-09/schema") { it["\$schema"].asString }
            expect("http://pwall.net/test") { it["\$id"].asString }
            expect("Product") { it["title"].asString }
            expect("object") { it["type"].asString }
            it["required"].asArray.let { required ->
                expect(3) { required.size }
                expect("id") { required[0].asString}
                expect("name") { required[1].asString }
                expect("price") { required[2].asString }
            }
            it["properties"].asObject.let { properties ->
                expect(5) { properties.size }
                properties["id"].asObject.let { id ->
                    expect(2) { id.size }
                    expect("number") { id["type"].asString }
                    expect("Product identifier") { id["description"].asString }
                }
                properties["name"].asObject.let { name ->
                    expect(2) { name.size }
                    expect("string") { name["type"].asString }
                    expect("Name of the product") { name["description"].asString }
                }
                properties["tags"].asObject.let { tags ->
                    expect(2) { tags.size }
                    expect("array") { tags["type"].asString }
                    tags["items"].asObject.let { items ->
                        expect(1) { items.size }
                        expect("string") { items["type"].asString }
                    }
                }
                properties["stock"].asObject.let { stock ->
                    expect(2) { stock.size }
                    expect("object") { stock["type"].asString }
                    stock["properties"].asObject.let { properties2 ->
                        expect(2) { properties2.size }
                        properties2["warehouse"].asObject.let { warehouse ->
                            expect(1) { warehouse.size }
                            expect("number") { warehouse["type"].asString }
                        }
                        properties2["retail"].asObject.let { retail ->
                            expect(1) { retail.size }
                            expect("number") { retail["type"].asString }
                        }
                    }
                }
            }
        }
    }

    companion object {

        val log = getLogger()

        const val test1 = """
aaa:
  bbb: |-
    ccc:

    ddd:
"""

    }

}
