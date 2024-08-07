/*
 * @(#) YAML.kt
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

import java.io.File
import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset

import io.kjson.yaml.parser.Parser

/**
 * YAML library - a set of YAML parsing functions.  The functions in this object delegate to the [Parser] class.
 *
 * @author  Peter Wall
 */
object YAML {

    @Suppress("ConstPropertyName")
    const val tagPrefix = "tag:yaml.org,2002:"
    @Suppress("ConstPropertyName")
    const val nullTag = "tag:yaml.org,2002:null"
    @Suppress("ConstPropertyName")
    const val mapTag = "tag:yaml.org,2002:map"
    @Suppress("ConstPropertyName")
    const val seqTag = "tag:yaml.org,2002:seq"
    @Suppress("ConstPropertyName")
    const val strTag = "tag:yaml.org,2002:str"
    @Suppress("ConstPropertyName")
    const val intTag = "tag:yaml.org,2002:int"
    @Suppress("ConstPropertyName")
    const val floatTag = "tag:yaml.org,2002:float"
    @Suppress("ConstPropertyName")
    const val boolTag = "tag:yaml.org,2002:bool"

    val parser = Parser()

    /**
     * Parse a [File] as YAML.
     *
     * @param   file        the input [File]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [YAMLDocument]
     */
    fun parse(file: File, charset: Charset? = null): YAMLDocument = parser.parse(file, charset)

    /**
     * Parse an [InputStream] as YAML.
     *
     * @param   inputStream the input [InputStream]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [YAMLDocument]
     */
    fun parse(inputStream: InputStream, charset: Charset? = null): YAMLDocument = parser.parse(inputStream, charset)

    /**
     * Parse a [Reader] as YAML.
     *
     * @param   reader      the input [Reader]
     * @return              a [YAMLDocument]
     */
    fun parse(reader: Reader): YAMLDocument = parser.parse(reader)

    /**
     * Parse a [File] as a multi-document YAML stream.
     *
     * @param   file        the input [File]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [List] of [YAMLDocument]s
     */
    fun parseStream(file: File, charset: Charset? = null): List<YAMLDocument> = parser.parseStream(file, charset)

    /**
     * Parse an [InputStream] as a multi-document YAML stream.
     *
     * @param   inputStream the input [InputStream]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [List] of [YAMLDocument]s
     */
    fun parseStream(inputStream: InputStream, charset: Charset? = null): List<YAMLDocument> =
            parser.parseStream(inputStream, charset)

    /**
     * Parse a [Reader] as a multi-document YAML stream.
     *
     * @param   reader      the input [Reader]
     * @return              a [List] of [YAMLDocument]s
     */
    fun parseStream(reader: Reader): List<YAMLDocument> = parser.parseStream(reader)

}
