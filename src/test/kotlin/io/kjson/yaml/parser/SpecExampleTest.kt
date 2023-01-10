/*
 * @(#) SpecExampleTest.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2023 Peter Wall
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.expect

import java.io.File

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
            assertIs<JSONArray>(this)
            expect(3) { size }
            expect(JSONString("Mark McGwire")) { this[0] }
            expect(JSONString("Sammy Sosa")) { this[1] }
            expect(JSONString("Ken Griffey")) { this[2] }
        }
    }

    @Test fun `should parse example 2_2 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.2.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONInt(65)) { this["hr"] }
            expect(JSONDecimal("0.278")) { this["avg"] }
            expect(JSONInt(147)) { this["rbi"] }
        }
    }

    @Test fun `should parse example 2_3 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.3.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(2) { size }
            with(this["american"]) {
                assertIs<JSONArray>(this)
                expect(3) { size }
                expect(JSONString("Boston Red Sox")) { this[0] }
                expect(JSONString("Detroit Tigers")) { this[1] }
                expect(JSONString("New York Yankees")) { this[2] }
            }
            with(this["national"]) {
                assertIs<JSONArray>(this)
                expect(3) { size }
                expect(JSONString("New York Mets")) { this[0] }
                expect(JSONString("Chicago Cubs")) { this[1] }
                expect(JSONString("Atlanta Braves")) { this[2] }
            }
        }
    }

    @Test fun `should parse example 2_4 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.4.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONArray>(this)
            expect(2) { size }
            with(this[0]) {
                assertIs<JSONObject>(this)
                expect(3) { size }
                expect(JSONString("Mark McGwire")) { this["name"] }
                expect(JSONInt(65)) { this["hr"] }
                expect(JSONDecimal("0.278")) { this["avg"] }
            }
            with(this[1]) {
                assertIs<JSONObject>(this)
                expect(3) { size }
                expect(JSONString("Sammy Sosa")) { this["name"] }
                expect(JSONInt(63)) { this["hr"] }
                expect(JSONDecimal("0.288")) { this["avg"] }
            }
        }
    }

    @Test fun `should parse example 2_5 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.5.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONArray>(this)
            expect(3) { size }
            with(this[0]) {
                assertIs<JSONArray>(this)
                expect(3) { size }
                expect(JSONString("name")) { this[0] }
                expect(JSONString("hr")) { this[1] }
                expect(JSONString("avg")) { this[2] }
            }
            with(this[1]) {
                assertIs<JSONArray>(this)
                expect(3) { size }
                expect(JSONString("Mark McGwire")) { this[0] }
                expect(JSONInt(65)) { this[1] }
                expect(JSONDecimal("0.278")) { this[2] }
            }
            with(this[2]) {
                assertIs<JSONArray>(this)
                expect(3) { size }
                expect(JSONString("Sammy Sosa")) { this[0] }
                expect(JSONInt(63)) { this[1] }
                expect(JSONDecimal("0.288")) { this[2] }
            }
        }
    }

    @Test fun `should parse example 2_6 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.6.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(2) { size }
            with(this["Mark McGwire"]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                expect(JSONInt(65)) { this["hr"] }
                expect(JSONDecimal("0.278")) { this["avg"] }
            }
            with(this["Sammy Sosa"]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                expect(JSONInt(63)) { this["hr"] }
                expect(JSONDecimal("0.288")) { this["avg"] }
            }
        }
    }

    @Test fun `should parse example 2_7 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.7.yaml")
        val result = Parser().parseStream(file)
        expect(2) { result.size }
        with(result[0].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONArray>(this)
            expect(3) { size }
            expect(JSONString("Mark McGwire")) { this[0] }
            expect(JSONString("Sammy Sosa")) { this[1] }
            expect(JSONString("Ken Griffey")) { this[2] }
        }
        with(result[1].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONArray>(this)
            expect(2) { size }
            expect(JSONString("Chicago Cubs")) { this[0] }
            expect(JSONString("St Louis Cardinals")) { this[1] }
        }
    }

    @Test fun `should parse example 2_8 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.8.yaml")
        val result = Parser().parseStream(file)
        expect(2) { result.size }
        with(result[0].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONString("20:03:20")) { this["time"] }
            expect(JSONString("Sammy Sosa")) { this["player"] }
            expect(JSONString("strike (miss)")) { this["action"] }
        }
        with(result[1].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONString("20:03:47")) { this["time"] }
            expect(JSONString("Sammy Sosa")) { this["player"] }
            expect(JSONString("grand slam")) { this["action"] }
        }
    }

    @Test fun `should parse example 2_9 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.9.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(2) { size }
            with(this["hr"]) {
                assertIs<JSONArray>(this)
                expect(2) { size }
                expect(JSONString("Mark McGwire")) { this[0] }
                expect(JSONString("Sammy Sosa")) { this[1] }
            }
            with(this["rbi"]) {
                assertIs<JSONArray>(this)
                expect(2) { size }
                expect(JSONString("Sammy Sosa")) { this[0] }
                expect(JSONString("Ken Griffey")) { this[1] }
            }
        }
    }

    @Test fun `should parse example 2_10 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.10.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(2) { size }
            with(this["hr"]) {
                assertIs<JSONArray>(this)
                expect(2) { size }
                expect(JSONString("Mark McGwire")) { this[0] }
                expect(JSONString("Sammy Sosa")) { this[1] }
            }
            with(this["rbi"]) {
                assertIs<JSONArray>(this)
                expect(2) { size }
                expect(JSONString("Sammy Sosa")) { this[0] }
                expect(JSONString("Ken Griffey")) { this[1] }
            }
        }
    }

    @Test fun `should parse example 2_11 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.11.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(2) { size }
            val key1 = JSONArray.build {
                add("Detroit Tigers")
                add("Chicago cubs")
            }
            with(this[key1.toJSON()]) {
                assertIs<JSONArray>(this)
                expect(1) { size }
                expect(JSONString("2001-07-23")) { this[0] }
            }
            val key2 = JSONArray.build {
                add("New York Yankees")
                add("Atlanta Braves")
            }
            with(this[key2.toJSON()]) {
                assertIs<JSONArray>(this)
                expect(3) { size }
                expect(JSONString("2001-07-02")) { this[0] }
                expect(JSONString("2001-08-12")) { this[1] }
                expect(JSONString("2001-08-14")) { this[2] }
            }
        }
    }

    @Test fun `should parse example 2_12 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.12.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONArray>(this)
            expect(3) { size }
            with(this[0]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                expect(JSONString("Super Hoop")) { this["item"] }
                expect(JSONInt(1)) { this["quantity"] }
            }
            with(this[1]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                expect(JSONString("Basketball")) { this["item"] }
                expect(JSONInt(4)) { this["quantity"] }
            }
            with(this[2]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                expect(JSONString("Big Shoes")) { this["item"] }
                expect(JSONInt(1)) { this["quantity"] }
            }
        }
    }

    @Test fun `should parse example 2_13 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.13.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect(JSONString("\\//||\\/||\n// ||  ||__\n")) { result.rootNode }
    }

    @Test fun `should parse example 2_14 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.14.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        expect(JSONString("Mark McGwire's year was crippled by a knee injury.\n")) { result.rootNode }
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
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONString("Mark McGwire")) { this["name"] }
            expect(JSONString("Mark set a major league home run record in 1998.\n")) { this["accomplishment"] }
            expect(JSONString("65 Home Runs\n0.278 Batting Average\n")) { this["stats"] }
        }
    }

    @Test fun `should parse example 2_17 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.17.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(6) { size }
            expect(JSONString("Sosa did fine.\u263A")) { this["unicode"] }
            expect(JSONString("\b1998\t1999\t2000\n")) { this["control"] }
            expect(JSONString("\r\n is \r\n")) { this["hex esc"] }
            expect(JSONString("\"Howdy!\" he cried.")) { this["single"] }
            expect(JSONString(" # Not a 'comment'.")) { this["quoted"] }
            expect(JSONString("|\\-*-/|")) { this["tie-fighter"] }
        }
    }

    @Test fun `should parse example 2_18 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.18.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(2) { size }
            expect(JSONString("This unquoted scalar spans many lines.")) { this["plain"] }
            expect(JSONString("So does this quoted scalar.\n")) { this["quoted"] }
        }
    }

    @Test fun `should parse example 2_19 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.19.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(4) { size }
            expect(JSONInt(12345)) { this["canonical"] }
            expect(JSONInt(12345)) { this["decimal"] }
            expect(JSONInt(12)) { this["octal"] }
            expect(JSONInt(12)) { this["hexadecimal"] }
        }
    }

    @Test fun `should parse example 2_20 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.20.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(5) { size }
            expect(JSONDecimal("1.23015e+3")) { this["canonical"] }
            expect(JSONDecimal("12.3015e+02")) { this["exponential"] }
            expect(JSONDecimal("1230.15")) { this["fixed"] }
            expect(JSONString("-.inf")) { this["negative infinity"] }
            expect(floatTag) { result.getTag(JSONPointer("/negative infinity"))}
            expect(JSONString(".nan")) { this["not a number"] }
            expect(floatTag) { result.getTag(JSONPointer("/not a number"))}
        }
    }

    @Test fun `should parse example 2_21 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.21.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(3) { size }
            assertNull(this["null"])
            with(this["booleans"]) {
                assertIs<JSONArray>(this)
                expect(JSONBoolean.TRUE) { this[0] }
                expect(JSONBoolean.FALSE) { this[1] }
            }
            expect(JSONString("012345")) { this["string"] }
        }
    }

    @Test fun `should parse example 2_22 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.22.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(4) { size }
            expect(JSONString("2001-12-15T02:59:43.1Z")) { this["canonical"] }
            expect(JSONString("2001-12-14t21:59:43.10-05:00")) { this["iso8601"] }
            expect(JSONString("2001-12-14 21:59:43.10 -5")) { this["spaced"] }
            expect(JSONString("2002-12-14")) { this["date"] }
        }
    }

    @Test fun `should parse example 2_23 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.23.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONString("2002-04-28")) { this["not-date"] }
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
            assertIs<JSONArray>(this)
            expect(3) { size }
            with(this[0]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                with(this["center"]) {
                    assertIs<JSONObject>(this)
                    expect(2) { size }
                    expect(JSONInt(73)) { this["x"]}
                    expect(JSONInt(129)) { this["y"]}
                }
                expect(JSONInt(7)) { this["radius"] }
            }
            expect("tag:clarkevans.com,2002:circle") { result.getTag(JSONPointer("/0"))}
            with(this[1]) {
                assertIs<JSONObject>(this)
                expect(2) { size }
                with(this["start"]) {
                    assertIs<JSONObject>(this)
                    expect(2) { size }
                    expect(JSONInt(73)) { this["x"]}
                    expect(JSONInt(129)) { this["y"]}
                }
                with(this["finish"]) {
                    assertIs<JSONObject>(this)
                    expect(2) { size }
                    expect(JSONInt(89)) { this["x"]}
                    expect(JSONInt(102)) { this["y"]}
                }
            }
            expect("tag:clarkevans.com,2002:line") { result.getTag(JSONPointer("/1"))}
            with(this[2]) {
                assertIs<JSONObject>(this)
                expect(3) { size }
                with(this["start"]) {
                    assertIs<JSONObject>(this)
                    expect(2) { size }
                    expect(JSONInt(73)) { this["x"]}
                    expect(JSONInt(129)) { this["y"]}
                }
                expect(JSONInt(0xFFEEBB)) { this["color"] }
                expect(JSONString("Pretty vector drawing.")) { this["text"] }
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
            assertIs<JSONObject>(this)
            expect(3) { size }
            assertTrue(containsKey("Mark McGwire"))
            assertNull(this["Mark McGwire"])
            assertTrue(containsKey("Sammy Sosa"))
            assertNull(this["Sammy Sosa"])
            assertTrue(containsKey("Ken Griffey"))
            assertNull(this["Ken Griffey"])
        }
        expect("tag:yaml.org,2002:set") { result.getTag(JSONPointer.root)}
    }

    @Test fun `should parse example 2_26 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.26.yaml")
        val result = Parser().parse(file)
        log.debug { result.rootNode?.toJSON() }
        with(result.rootNode) {
            assertIs<JSONArray>(this)
            expect(3) { size }
            with(this[0]) {
                assertIs<JSONObject>(this)
                expect(1) { size }
                expect(JSONInt(65)) { this["Mark McGwire"]}
            }
            with(this[1]) {
                assertIs<JSONObject>(this)
                expect(1) { size }
                expect(JSONInt(63)) { this["Sammy Sosa"]}
            }
            with(this[2]) {
                assertIs<JSONObject>(this)
                expect(1) { size }
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
            assertIs<JSONObject>(this)
            expect(8) { size }
            expect(JSONInt(34843)) { this["invoice"] }
            with(this["bill-to"]) {
                assertIs<JSONObject>(this)
                expect(3) { size }
                expect(JSONString("Chris")) { this["given"] }
                expect(JSONString("Dumars")) { this["family"] }
                with(this["address"]) {
                    assertIs<JSONObject>(this)
                    expect(4) { size }
                    expect(JSONString("458 Walkman Dr.\nSuite #292\n")) { this["lines"] }
                    expect(JSONString("Royal Oak")) { this["city"] }
                    expect(JSONString("MI")) { this["state"] }
                    expect(JSONInt(48046)) { this["postal"] }
                }
            }
            with(this["ship-to"]) {
                assertIs<JSONObject>(this)
                expect(3) { size }
                expect(JSONString("Chris")) { this["given"] }
                expect(JSONString("Dumars")) { this["family"] }
                with(this["address"]) {
                    assertIs<JSONObject>(this)
                    expect(4) { size }
                    expect(JSONString("458 Walkman Dr.\nSuite #292\n")) { this["lines"] }
                    expect(JSONString("Royal Oak")) { this["city"] }
                    expect(JSONString("MI")) { this["state"] }
                    expect(JSONInt(48046)) { this["postal"] }
                }
            }
            with(this["product"]) {
                assertIs<JSONArray>(this)
                expect(2) { size }
                with(this[0]) {
                    assertIs<JSONObject>(this)
                    expect(4) { size }
                    expect(JSONString("BL394D")) { this["sku"] }
                    expect(JSONInt(4)) { this["quantity"] }
                    expect(JSONString("Basketball")) { this["description"] }
                    expect(JSONDecimal("450.00")) { this["price"] }
                }
                with(this[1]) {
                    assertIs<JSONObject>(this)
                    expect(4) { size }
                    expect(JSONString("BL4438H")) { this["sku"] }
                    expect(JSONInt(1)) { this["quantity"] }
                    expect(JSONString("Super Hoop")) { this["description"] }
                    expect(JSONDecimal("2392.00")) { this["price"] }
                }
            }
            expect(JSONDecimal("251.42")) { this["tax"] }
            expect(JSONDecimal("4443.52")) { this["total"] }
            expect(JSONString("Late afternoon is best. Backup contact is Nancy Billsmer" +
                    " @ 338-4338.")) { this["comments"] }
        }
        expect("tag:clarkevans.com,2002:invoice") { result.getTag(JSONPointer.root)}
    }

    @Test fun `should parse example 2_28 correctly`() {
        val file = File("src/test/resources/spec_examples/spec_example_2.28.yaml")
        val result = Parser().parseStream(file)
        expect(3) { result.size }
        with(result[0].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONString("2001-11-23 15:01:42 -5")) { this["Time"] }
            expect(JSONString("ed")) { this["User"] }
            expect(JSONString("This is an error message for the log file")) { this["Warning"] }
        }
        with(result[1].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONObject>(this)
            expect(3) { size }
            expect(JSONString("2001-11-23 15:02:31 -5")) { this["Time"] }
            expect(JSONString("ed")) { this["User"] }
            expect(JSONString("A slightly different error message.")) { this["Warning"] }
        }
        with(result[2].rootNode) {
            log.debug { this?.toJSON() }
            assertIs<JSONObject>(this)
            expect(4) { size }
            expect(JSONString("2001-11-23 15:03:17 -5")) { this["Date"] }
            expect(JSONString("ed")) { this["User"] }
            expect(JSONString("Unknown variable \"bar\"")) { this["Fatal"] }
            with(this["Stack"]) {
                assertIs<JSONArray>(this)
                expect(2) { size }
                with(this[0]) {
                    assertIs<JSONObject>(this)
                    expect(3) { size }
                    expect(JSONString("TopClass.py")) { this["file"]}
                    expect(JSONInt(23)) { this["line"]}
                    expect(JSONString("x = MoreObject(\"345\\n\")\n")) { this["code"]}
                }
                with(this[1]) {
                    assertIs<JSONObject>(this)
                    expect(3) { size }
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
