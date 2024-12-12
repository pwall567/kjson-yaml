/*
 * @(#) SpecExampleTest.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2023, 2024 Peter Wall
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

import java.io.File

import io.kstuff.test.shouldBe
import io.kstuff.test.shouldBeType

import io.kjson.JSONArray
import io.kjson.JSONBoolean
import io.kjson.JSONDecimal
import io.kjson.JSONInt
import io.kjson.JSONObject
import io.kjson.JSONString
import io.kjson.pointer.JSONPointer
import io.kjson.yaml.YAML.floatTag
import io.kjson.yaml.YAML.strTag
import net.pwall.log.getLogger

class SpecExampleTest {

    @Test fun `should parse example 2_1 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.1.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 3
            this[0] shouldBe JSONString("Mark McGwire")
            this[1] shouldBe JSONString("Sammy Sosa")
            this[2] shouldBe JSONString("Ken Griffey")
        }
    }

    @Test fun `should parse example 2_2 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["hr"] shouldBe JSONInt(65)
            this["avg"] shouldBe JSONDecimal("0.278")
            this["rbi"] shouldBe JSONInt(147)
        }
    }

    @Test fun `should parse example 2_3 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 2
            with(this["american"]) {
                shouldBeType<JSONArray>()
                size shouldBe 3
                this[0] shouldBe JSONString("Boston Red Sox")
                this[1] shouldBe JSONString("Detroit Tigers")
                this[2] shouldBe JSONString("New York Yankees")
            }
            with(this["national"]) {
                shouldBeType<JSONArray>()
                size shouldBe 3
                this[0] shouldBe JSONString("New York Mets")
                this[1] shouldBe JSONString("Chicago Cubs")
                this[2] shouldBe JSONString("Atlanta Braves")
            }
        }
    }

    @Test fun `should parse example 2_4 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 2
            with(this[0]) {
                shouldBeType<JSONObject>()
                size shouldBe 3
                this["name"] shouldBe JSONString("Mark McGwire")
                this["hr"] shouldBe JSONInt(65)
                this["avg"] shouldBe JSONDecimal("0.278")
            }
            with(this[1]) {
                shouldBeType<JSONObject>()
                size shouldBe 3
                this["name"] shouldBe JSONString("Sammy Sosa")
                this["hr"] shouldBe JSONInt(63)
                this["avg"] shouldBe JSONDecimal("0.288")
            }
        }
    }

    @Test fun `should parse example 2_5 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.5.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 3
            with(this[0]) {
                shouldBeType<JSONArray>()
                size shouldBe 3
                this[0] shouldBe JSONString("name")
                this[1] shouldBe JSONString("hr")
                this[2] shouldBe JSONString("avg")
            }
            with(this[1]) {
                shouldBeType<JSONArray>()
                size shouldBe 3
                this[0] shouldBe JSONString("Mark McGwire")
                this[1] shouldBe JSONInt(65)
                this[2] shouldBe JSONDecimal("0.278")
            }
            with(this[2]) {
                shouldBeType<JSONArray>()
                size shouldBe 3
                this[0] shouldBe JSONString("Sammy Sosa")
                this[1] shouldBe JSONInt(63)
                this[2] shouldBe JSONDecimal("0.288")
            }
        }
    }

    @Test fun `should parse example 2_6 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.6.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 2
            with(this["Mark McGwire"]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                this["hr"] shouldBe JSONInt(65)
                this["avg"] shouldBe JSONDecimal("0.278")
            }
            with(this["Sammy Sosa"]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                this["hr"] shouldBe JSONInt(63)
                this["avg"] shouldBe JSONDecimal("0.288")
            }
        }
    }

    @Test fun `should parse example 2_7 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.7.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 2
        with(result[0].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONArray>()
            size shouldBe 3
            this[0] shouldBe JSONString("Mark McGwire")
            this[1] shouldBe JSONString("Sammy Sosa")
            this[2] shouldBe JSONString("Ken Griffey")
        }
        with(result[1].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONArray>()
            size shouldBe 2
            this[0] shouldBe JSONString("Chicago Cubs")
            this[1] shouldBe JSONString("St Louis Cardinals")
        }
    }

    @Test fun `should parse example 2_8 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.8.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 2
        with(result[0].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["time"] shouldBe JSONString("20:03:20")
            this["player"] shouldBe JSONString("Sammy Sosa")
            this["action"] shouldBe JSONString("strike (miss)")
        }
        with(result[1].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["time"] shouldBe JSONString("20:03:47")
            this["player"] shouldBe JSONString("Sammy Sosa")
            this["action"] shouldBe JSONString("grand slam")
        }
    }

    @Test fun `should parse example 2_9 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.9.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 2
            with(this["hr"]) {
                shouldBeType<JSONArray>()
                size shouldBe 2
                this[0] shouldBe JSONString("Mark McGwire")
                this[1] shouldBe JSONString("Sammy Sosa")
            }
            with(this["rbi"]) {
                shouldBeType<JSONArray>()
                size shouldBe 2
                this[0] shouldBe JSONString("Sammy Sosa")
                this[1] shouldBe JSONString("Ken Griffey")
            }
        }
    }

    @Test fun `should parse example 2_10 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.10.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 2
            with(this["hr"]) {
                shouldBeType<JSONArray>()
                size shouldBe 2
                this[0] shouldBe JSONString("Mark McGwire")
                this[1] shouldBe JSONString("Sammy Sosa")
            }
            with(this["rbi"]) {
                shouldBeType<JSONArray>()
                size shouldBe 2
                this[0] shouldBe JSONString("Sammy Sosa")
                this[1] shouldBe JSONString("Ken Griffey")
            }
        }
    }

    @Test fun `should parse example 2_11 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.11.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 2
            val key1 = JSONArray.build {
                add("Detroit Tigers")
                add("Chicago cubs")
            }
            with(this[key1.toJSON()]) {
                shouldBeType<JSONArray>()
                size shouldBe 1
                this[0] shouldBe JSONString("2001-07-23")
            }
            val key2 = JSONArray.build {
                add("New York Yankees")
                add("Atlanta Braves")
            }
            with(this[key2.toJSON()]) {
                shouldBeType<JSONArray>()
                size shouldBe 3
                this[0] shouldBe JSONString("2001-07-02")
                this[1] shouldBe JSONString("2001-08-12")
                this[2] shouldBe JSONString("2001-08-14")
            }
        }
    }

    @Test fun `should parse example 2_12 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.12.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 3
            with(this[0]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                this["item"] shouldBe JSONString("Super Hoop")
                this["quantity"] shouldBe JSONInt(1)
            }
            with(this[1]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                this["item"] shouldBe JSONString("Basketball")
                this["quantity"] shouldBe JSONInt(4)
            }
            with(this[2]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                this["item"] shouldBe JSONString("Big Shoes")
                this["quantity"] shouldBe JSONInt(1)
            }
        }
    }

    @Test fun `should parse example 2_13 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.13.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode shouldBe JSONString("\\//||\\/||\n// ||  ||__\n")
    }

    @Test fun `should parse example 2_14 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.14.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        result.rootNode shouldBe JSONString("Mark McGwire's year was crippled by a knee injury.\n")
    }

    @Test fun `should parse example 2_15 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.15.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect(JSONString("Sammy Sosa completed another fine season with great stats.\n\n  63 Home Runs\n" +
                "  0.288 Batting Average\n\nWhat a year!\n")) { result.rootNode }
    }

    @Test fun `should parse example 2_16 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.16.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["name"] shouldBe JSONString("Mark McGwire")
            this["accomplishment"] shouldBe JSONString("Mark set a major league home run record in 1998.\n")
            this["stats"] shouldBe JSONString("65 Home Runs\n0.278 Batting Average\n")
        }
    }

    @Test fun `should parse example 2_17 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.17.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 6
            this["unicode"] shouldBe JSONString("Sosa did fine.\u263A")
            this["control"] shouldBe JSONString("\b1998\t1999\t2000\n")
            this["hex esc"] shouldBe JSONString("\r\n is \r\n")
            this["single"] shouldBe JSONString("\"Howdy!\" he cried.")
            this["quoted"] shouldBe JSONString(" # Not a 'comment'.")
            this["tie-fighter"] shouldBe JSONString("|\\-*-/|")
        }
    }

    @Test fun `should parse example 2_18 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.18.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 2
            this["plain"] shouldBe JSONString("This unquoted scalar spans many lines.")
            this["quoted"] shouldBe JSONString("So does this quoted scalar.\n")
        }
    }

    @Test fun `should parse example 2_19 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.19.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 4
            this["canonical"] shouldBe JSONInt(12345)
            this["decimal"] shouldBe JSONInt(12345)
            this["octal"] shouldBe JSONInt(12)
            this["hexadecimal"] shouldBe JSONInt(12)
        }
    }

    @Test fun `should parse example 2_20 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.20.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 5
            this["canonical"] shouldBe JSONDecimal("1.23015e+3")
            this["exponential"] shouldBe JSONDecimal("12.3015e+02")
            this["fixed"] shouldBe JSONDecimal("1230.15")
            this["negative infinity"] shouldBe JSONString("-.inf")
            expect(floatTag) { result.getTag(JSONPointer("/negative infinity"))}
            this["not a number"] shouldBe JSONString(".nan")
            expect(floatTag) { result.getTag(JSONPointer("/not a number"))}
        }
    }

    @Test fun `should parse example 2_21 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.21.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["null"] shouldBe null
            with(this["booleans"]) {
                shouldBeType<JSONArray>()
                this[0] shouldBe JSONBoolean.TRUE
                this[1] shouldBe JSONBoolean.FALSE
            }
            this["string"] shouldBe JSONString("012345")
        }
    }

    @Test fun `should parse example 2_22 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.22.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 4
            this["canonical"] shouldBe JSONString("2001-12-15T02:59:43.1Z")
            this["iso8601"] shouldBe JSONString("2001-12-14t21:59:43.10-05:00")
            this["spaced"] shouldBe JSONString("2001-12-14 21:59:43.10 -5")
            this["date"] shouldBe JSONString("2002-12-14")
        }
    }

    @Test fun `should parse example 2_23 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.23.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["not-date"] shouldBe JSONString("2002-04-28")
            expect(strTag) { result.getTag(JSONPointer("/not-date"))}
            expect(JSONString("R0lGODlhDAAMAIQAAP//9/X\n17unp5WZmZgAAAOfn515eXv\n" +
                    "Pz7Y6OjuDg4J+fn5OTk6enp\n56enmleECcgggoBADs=\n")) { this["picture"] }
            expect("tag:yaml.org,2002:binary") { result.getTag(JSONPointer("/picture"))}
            expect(JSONString("The semantics of the tag\nabove may be different for\n" +
                    "different documents.\n")) { this["application specific tag"] }
            expect("!something") { result.getTag(JSONPointer("/application specific tag"))}
        }
    }

    @Test fun `should parse example 2_24 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.24.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 3
            with(this[0]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                with(this["center"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 2
                    expect(JSONInt(73)) { this["x"]}
                    expect(JSONInt(129)) { this["y"]}
                }
                this["radius"] shouldBe JSONInt(7)
            }
            expect("tag:clarkevans.com,2002:circle") { result.getTag(JSONPointer("/0"))}
            with(this[1]) {
                shouldBeType<JSONObject>()
                size shouldBe 2
                with(this["start"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 2
                    expect(JSONInt(73)) { this["x"]}
                    expect(JSONInt(129)) { this["y"]}
                }
                with(this["finish"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 2
                    expect(JSONInt(89)) { this["x"]}
                    expect(JSONInt(102)) { this["y"]}
                }
            }
            expect("tag:clarkevans.com,2002:line") { result.getTag(JSONPointer("/1"))}
            with(this[2]) {
                shouldBeType<JSONObject>()
                size shouldBe 3
                with(this["start"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 2
                    expect(JSONInt(73)) { this["x"]}
                    expect(JSONInt(129)) { this["y"]}
                }
                this["color"] shouldBe JSONInt(0xFFEEBB)
                this["text"] shouldBe JSONString("Pretty vector drawing.")
            }
            expect("tag:clarkevans.com,2002:label") { result.getTag(JSONPointer("/2"))}
        }
        expect("tag:clarkevans.com,2002:shape") { result.getTag(JSONPointer.root)}
    }

    @Test fun `should parse example 2_25 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.25.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 3
            containsKey("Mark McGwire") shouldBe true
            this["Mark McGwire"] shouldBe null
            containsKey("Sammy Sosa") shouldBe true
            this["Sammy Sosa"] shouldBe null
            containsKey("Ken Griffey") shouldBe true
            this["Ken Griffey"] shouldBe null
        }
        expect("tag:yaml.org,2002:set") { result.getTag(JSONPointer.root)}
    }

    @Test fun `should parse example 2_26 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.26.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONArray>()
            size shouldBe 3
            with(this[0]) {
                shouldBeType<JSONObject>()
                size shouldBe 1
                expect(JSONInt(65)) { this["Mark McGwire"]}
            }
            with(this[1]) {
                shouldBeType<JSONObject>()
                size shouldBe 1
                expect(JSONInt(63)) { this["Sammy Sosa"]}
            }
            with(this[2]) {
                shouldBeType<JSONObject>()
                size shouldBe 1
                expect(JSONInt(58)) { this["Ken Griffey"]}
            }
        }
        expect("tag:yaml.org,2002:omap") { result.getTag(JSONPointer.root)}
    }

    @Test fun `should parse example 2_27 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.27.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            shouldBeType<JSONObject>()
            size shouldBe 8
            this["invoice"] shouldBe JSONInt(34843)
            with(this["bill-to"]) {
                shouldBeType<JSONObject>()
                size shouldBe 3
                this["given"] shouldBe JSONString("Chris")
                this["family"] shouldBe JSONString("Dumars")
                with(this["address"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 4
                    this["lines"] shouldBe JSONString("458 Walkman Dr.\nSuite #292\n")
                    this["city"] shouldBe JSONString("Royal Oak")
                    this["state"] shouldBe JSONString("MI")
                    this["postal"] shouldBe JSONInt(48046)
                }
            }
            with(this["ship-to"]) {
                shouldBeType<JSONObject>()
                size shouldBe 3
                this["given"] shouldBe JSONString("Chris")
                this["family"] shouldBe JSONString("Dumars")
                with(this["address"]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 4
                    this["lines"] shouldBe JSONString("458 Walkman Dr.\nSuite #292\n")
                    this["city"] shouldBe JSONString("Royal Oak")
                    this["state"] shouldBe JSONString("MI")
                    this["postal"] shouldBe JSONInt(48046)
                }
            }
            with(this["product"]) {
                shouldBeType<JSONArray>()
                size shouldBe 2
                with(this[0]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 4
                    this["sku"] shouldBe JSONString("BL394D")
                    this["quantity"] shouldBe JSONInt(4)
                    this["description"] shouldBe JSONString("Basketball")
                    this["price"] shouldBe JSONDecimal("450.00")
                }
                with(this[1]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 4
                    this["sku"] shouldBe JSONString("BL4438H")
                    this["quantity"] shouldBe JSONInt(1)
                    this["description"] shouldBe JSONString("Super Hoop")
                    this["price"] shouldBe JSONDecimal("2392.00")
                }
            }
            this["tax"] shouldBe JSONDecimal("251.42")
            this["total"] shouldBe JSONDecimal("4443.52")
            expect(JSONString("Late afternoon is best. Backup contact is Nancy Billsmer" +
                    " @ 338-4338.")) { this["comments"] }
        }
        expect("tag:clarkevans.com,2002:invoice") { result.getTag(JSONPointer.root)}
    }

    @Test fun `should parse example 2_28 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.28.yaml")
        val result = Parser().parseStream(file)
        result.size shouldBe 3
        with(result[0].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["Time"] shouldBe JSONString("2001-11-23 15:01:42 -5")
            this["User"] shouldBe JSONString("ed")
            this["Warning"] shouldBe JSONString("This is an error message for the log file")
        }
        with(result[1].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONObject>()
            size shouldBe 3
            this["Time"] shouldBe JSONString("2001-11-23 15:02:31 -5")
            this["User"] shouldBe JSONString("ed")
            this["Warning"] shouldBe JSONString("A slightly different error message.")
        }
        with(result[2].rootNode) {
            log.debug { this?.toJSON() }
            shouldBeType<JSONObject>()
            size shouldBe 4
            this["Date"] shouldBe JSONString("2001-11-23 15:03:17 -5")
            this["User"] shouldBe JSONString("ed")
            this["Fatal"] shouldBe JSONString("Unknown variable \"bar\"")
            with(this["Stack"]) {
                shouldBeType<JSONArray>()
                size shouldBe 2
                with(this[0]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 3
                    expect(JSONString("TopClass.py")) { this["file"]}
                    expect(JSONInt(23)) { this["line"]}
                    expect(JSONString("x = MoreObject(\"345\\n\")\n")) { this["code"]}
                }
                with(this[1]) {
                    shouldBeType<JSONObject>()
                    size shouldBe 3
                    expect(JSONString("MoreClass.py")) { this["file"]}
                    expect(JSONInt(58)) { this["line"]}
                    expect(JSONString("foo = bar")) { this["code"]}
                }
            }
        }
    }

    companion object {
        val log = getLogger()
    }

}
