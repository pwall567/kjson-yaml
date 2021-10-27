/*
 * @(#) YAMLDocument.kt
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

import io.kjson.JSONArray
import io.kjson.JSONBoolean
import io.kjson.JSONDecimal
import io.kjson.JSONInt
import io.kjson.JSONLong
import io.kjson.JSONObject
import io.kjson.JSONString
import io.kjson.JSONValue
import io.kjson.pointer.JSONPointer

/**
 * A YAML document - the result of a YAML parsing operation.
 *
 * @author  Peter Wall
 */
class YAMLDocument(
    val rootNode: JSONValue?,
    private val tagMap: Map<JSONPointer, String>,
    val majorVersion: Int,
    val minorVersion: Int
) {

    constructor(rootNode: JSONValue?, tagMap: Map<JSONPointer, String> = emptyMap()) :
            this(rootNode, tagMap, defaultMajorVersion, defaultMinorVersion)

    /**
     * Get the tag associated with the specified node.
     *
     * @param   pointer     a [JSONPointer] specifying the node
     * @return              the tag
     */
    fun getTag(pointer: JSONPointer): String {
        tagMap[pointer]?.let { return it }
        if (!pointer.existsIn(rootNode))
            throw YAMLException("Node does not exist")
        return when(pointer.find(rootNode)) {
            null -> "tag:yaml.org,2002:null"
            is JSONObject -> "tag:yaml.org,2002:map"
            is JSONArray -> "tag:yaml.org,2002:seq"
            is JSONString -> "tag:yaml.org,2002:str"
            is JSONInt -> "tag:yaml.org,2002:int"
            is JSONLong -> "tag:yaml.org,2002:int"
            is JSONDecimal -> "tag:yaml.org,2002:float"
            is JSONBoolean -> "tag:yaml.org,2002:bool"
        }
    }

    companion object {

        const val defaultMajorVersion = 1
        const val defaultMinorVersion = 2

    }

}
