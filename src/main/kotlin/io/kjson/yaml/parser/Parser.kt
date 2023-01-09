/*
 * @(#) Parser.kt
 *
 * kjson-yaml  Kotlin YAML processor
 * Copyright (c) 2020, 2021, 2022, 2023 Peter Wall
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

import java.io.File
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.nio.charset.Charset

import io.kjson.JSONArray
import io.kjson.JSONBoolean
import io.kjson.JSONDecimal
import io.kjson.JSONInt
import io.kjson.JSONLong
import io.kjson.JSONObject
import io.kjson.JSONString
import io.kjson.JSONValue
import io.kjson.pointer.JSONPointer
import io.kjson.yaml.YAML.floatTag
import io.kjson.yaml.YAML.intTag
import io.kjson.yaml.YAML.strTag
import io.kjson.yaml.YAML.tagPrefix
import io.kjson.yaml.YAMLDocument
import net.pwall.log.getLogger
import net.pwall.pipeline.codec.DynamicReader
import net.pwall.text.StringMapper.buildResult
import net.pwall.text.StringMapper.fromHexDigit
import net.pwall.text.StringMapper.mapSubstrings
import net.pwall.text.TextMatcher

/**
 * YAML Parser.
 *
 * @author  Peter Wall
 */
class Parser {

    // Implementation note - there is currently no state associated with the Parser so it could be implemented as an
    // object.  The use of a class leaves open the possibility of adding further functionality in future.

    enum class State { INITIAL, DIRECTIVE, MAIN, ENDED }

    /**
     * Parse a [File] as YAML.
     *
     * @param   file        the input [File]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [YAMLDocument]
     */
    fun parse(file: File, charset: Charset? = null) = parse(file.inputStream(), charset)

    /**
     * Parse an [InputStream] as YAML.
     *
     * @param   inputStream the input [InputStream]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [YAMLDocument]
     */
    fun parse(inputStream: InputStream, charset: Charset? = null) =
            parse(DynamicReader(inputStream).apply { charset?.let { this.switchTo(it) } })

    /**
     * Parse a [Reader] as YAML.
     *
     * @param   reader      the input [Reader]
     * @return              a [YAMLDocument]
     */
    fun parse(reader: Reader): YAMLDocument {
        var state = State.INITIAL
        val context = Context()
        val outerBlock = InitialBlock(context, 0)
        var lineNumber = 0
        reader.forEachLine { text ->
            val line = Line(++lineNumber, text)
            when (state) {
                State.INITIAL -> {
                    when {
                        text.startsWith("%YAML") -> {
                            context.version = processYAMLDirective(line)
                            state = State.DIRECTIVE
                        }
                        text.startsWith("%TAG") -> {
                            processTAGDirective(context, line)
                            state = State.DIRECTIVE
                        }
                        text.startsWith('%') -> {
                            warn("Unrecognised directive ignored - $text")
                            state = State.DIRECTIVE
                        }
                        text.startsWith("---") -> state = State.MAIN
                        text.startsWith("...") -> state = State.ENDED
                        !line.atEnd() -> {
                            state = State.MAIN
                            outerBlock.processLine(line)
                        }
                    }
                }
                State.DIRECTIVE -> {
                    when {
                        text.startsWith("%YAML") -> fatal("Duplicate or misplaced %YAML directive", line)
                        text.startsWith("%TAG") -> processTAGDirective(context, line)
                        text.startsWith('%') -> warn("Unrecognised directive ignored - $text")
                        text.startsWith("---") -> state = State.MAIN
                        text.startsWith("...") -> state = State.ENDED
                        !line.atEnd() -> fatal("Illegal data following directive(s)", line)
                    }
                }
                State.MAIN -> {
                    when {
                        text.startsWith("...") -> state = State.ENDED
                        text.startsWith("---") -> fatal("Multiple documents not allowed", line)
                        line.atEnd() -> outerBlock.processBlankLine(line)
                        else -> outerBlock.processLine(line)
                    }
                }
                State.ENDED -> {
                    if (text.trim().isNotEmpty())
                        fatal("Non-blank lines after end of document", line)
                }
            }
        }
        val rootNode = outerBlock.conclude(Line(lineNumber, ""))
        return createYAMLDocument(rootNode, context)
    }

    /**
     * Parse a [File] as a multi-document YAML stream.
     *
     * @param   file        the input [File]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [List] of [YAMLDocument]s
     */
    fun parseStream(file: File, charset: Charset? = null) = parseStream(file.inputStream(), charset)

    /**
     * Parse an [InputStream] as a multi-document YAML stream.
     *
     * @param   inputStream the input [InputStream]
     * @param   charset     the [Charset], or `null` to specify that the charset is to be determined dynamically
     * @return              a [List] of [YAMLDocument]s
     */
    fun parseStream(inputStream: InputStream, charset: Charset? = null) =
            parseStream(DynamicReader(inputStream).apply { charset?.let { this.switchTo(it) } })

    /**
     * Parse a [Reader] as a multi-document YAML stream.
     *
     * @param   reader      the input [Reader]
     * @return              a [List] of [YAMLDocument]s
     */
    fun parseStream(reader: Reader): List<YAMLDocument> {
        var state = State.INITIAL
        var context = Context()
        var outerBlock = InitialBlock(context, 0)
        var lineNumber = 0
        val result = mutableListOf<YAMLDocument>()
        reader.forEachLine { text ->
            val line = Line(++lineNumber, text)
            when (state) {
                State.INITIAL -> {
                    when {
                        text.startsWith("%YAML") -> {
                            context.version = processYAMLDirective(line)
                            state = State.DIRECTIVE
                        }
                        text.startsWith("%TAG") -> {
                            processTAGDirective(context, line)
                            state = State.DIRECTIVE
                        }
                        text.startsWith('%') -> {
                            warn("Unrecognised directive ignored - $text")
                            state = State.DIRECTIVE
                        }
                        text.startsWith("---") -> state = State.MAIN
                        text.startsWith("...") -> result.add(YAMLDocument(null))
                        !line.atEnd() -> {
                            state = State.MAIN
                            outerBlock.processLine(line)
                        }
                    }
                }
                State.DIRECTIVE -> {
                    when {
                        text.startsWith("%YAML") -> fatal("Duplicate or misplaced %YAML directive", line)
                        text.startsWith("%TAG") -> processTAGDirective(context, line)
                        text.startsWith('%') -> warn("Unrecognised directive ignored - $text")
                        text.startsWith("---") -> state = State.MAIN
                        text.startsWith("...") -> {
                            result.add(createYAMLDocument(null, context))
                            state = State.INITIAL
                        }
                        !line.atEnd() -> fatal("Illegal data following directive(s)", line)
                    }
                }
                State.MAIN -> {
                    when {
                        text.startsWith("...") -> {
                            result.add(createYAMLDocument(outerBlock.conclude(line), context))
                            context = Context()
                            outerBlock = InitialBlock(context, 0)
                            state = State.INITIAL
                        }
                        text.startsWith("---") -> {
                            result.add(createYAMLDocument(outerBlock.conclude(line), context))
                            context = Context()
                            outerBlock = InitialBlock(context, 0)
                            state = State.MAIN
                        }
                        line.atEnd() -> outerBlock.processBlankLine(line)
                        else -> outerBlock.processLine(line)
                    }
                }
                State.ENDED -> {
                    if (text.trim().isNotEmpty())
                        fatal("Non-blank lines after end of document", line)
                }
            }
        }
        if (state != State.INITIAL || result.isEmpty())
            result.add(createYAMLDocument(outerBlock.conclude(Line(lineNumber, "")), context))
        return result
    }

    private fun createYAMLDocument(rootNode: JSONValue?, context: Context): YAMLDocument {
        log.debug {
            val type = when (rootNode) {
                null -> "null"
                else -> rootNode::class.simpleName
            }
            "Parse complete; result is $type"
        }
        context.version?.let {
            return YAMLDocument(rootNode, context.tagMap, it.first, it.second)
        }
        return YAMLDocument(rootNode, context.tagMap)
    }

    private fun processYAMLDirective(line: Line): Pair<Int, Int> {
        line.skipFixed(5)
        if (!line.matchSpaces())
            fatal("Illegal %YAML directive", line)
        if (!line.matchDec())
            fatal("Illegal version number on %YAML directive", line)
        val majorVersion = line.resultInt
        if (!line.match('.') || !line.matchDec())
            fatal("Illegal version number on %YAML directive", line)
        val minorVersion = line.resultInt
        if (majorVersion != 1)
            fatal("%YAML version must be 1.x", line)
        line.skipSpaces()
        if (!line.atEnd())
            fatal("Illegal data on %YAML directive", line)
        if (minorVersion !in 1..2)
            warn("Unexpected YAML version - $majorVersion.$minorVersion")
        return majorVersion to minorVersion
    }

    private fun processTAGDirective(context: Context, line: Line) {
        line.skipFixed(4)
        if (!line.matchSpaces())
            fatal("Illegal %TAG directive", line)
        if (!line.match('!'))
            fatal("Illegal tag handle on %TAG directive", line)
        val handleStart = line.start
        val handle = when {
            line.matchSpace() -> {
                line.revert()
                "!"
            }
            line.match('!') -> "!!"
            else -> {
                if (!line.matchSeq { it.isTagHandleChar() } || !line.match('!'))
                    fatal("Illegal tag handle on %TAG directive", line)
                line.getString(handleStart, line.index)
            }
        }
        if (context.tagHandles.containsKey(handle))
            fatal("Duplicate tag handle", line)
        if (!line.matchSpaces())
            fatal("Illegal %TAG directive", line)
        line.skipToSpace()
        val prefix = line.result
        if (!prefix.startsWith('!')) {
            val tm = TextMatcher(prefix)
            if (!tm.matchURI() || !tm.isAtEnd)
                fatal("Illegal prefix on %TAG directive", line)
        }
        line.skipSpaces()
        if (!line.atEnd())
            fatal("Illegal data on %TAG directive", line)
        context.tagHandles[handle] = prefix
    }

    class Context(
        var version: Pair<Int, Int>? = null,
        val tagHandles: MutableMap<String, String> = mutableMapOf(),
        val anchorMap: MutableMap<String, JSONValue> = mutableMapOf(),
        val tagMap: MutableMap<JSONPointer, String> = mutableMapOf(),
        private val pointer: JSONPointer = JSONPointer.root,
        var anchor: String? = null,
        var tag: String? = null,
    ) {

        fun getTagHandle(shortcut: String): String? {
            return tagHandles[shortcut] ?: when (shortcut) {
                "!" -> "!"
                "!!" -> tagPrefix
                else -> null
            }
        }

        fun saveNodeProperties(node: JSONValue?) {
            anchor?.let {
                if (node != null)
                    anchorMap[it] = node
            }
            tag?.let {
                tagMap[pointer] = it
            }
        }

        fun child(name: String) = Context(
            version = version,
            tagHandles = tagHandles,
            anchorMap = anchorMap,
            tagMap = tagMap,
            pointer = pointer.child(name),
        )

        fun child(index: Int) = Context(
            version = version,
            tagHandles = tagHandles,
            anchorMap = anchorMap,
            tagMap = tagMap,
            pointer = pointer.child(index),
        )

    }

    abstract class Block(val context: Context, val indent: Int) {
        abstract fun processLine(line: Line)
        open fun processBlankLine(line: Line) {}
        abstract fun conclude(line: Line): JSONValue?
    }

    object ErrorBlock : Block(Context(), 0) {

        override fun processLine(line: Line) = fatal("Should not happen", line)

        override fun conclude(line: Line) = fatal("Should not happen", line)

    }

    class InitialBlock(context: Context, indent: Int) : Block(context, indent) {

        enum class State { INITIAL, CHILD, CLOSED }

        private var state: State = State.INITIAL
        private var node: JSONValue? = null
        private var child: Block = ErrorBlock

        override fun processLine(line: Line) {
            when (state) {
                State.INITIAL -> processFirstLine(line)
                State.CHILD -> child.processLine(line)
                State.CLOSED -> fatal("Unexpected state in YAML processing", line)
            }
        }

        private fun processFirstLine(line: Line) {
            val initialIndex = line.index
            line.processNodeProperties(context)
            val scalar = when {
                line.atEnd() -> return
                line.match('*') -> line.getAnchorName().let {
                    AliasChild(context.anchorMap[it] ?: fatal("Can't locate alias \"$it\"", line))
                }
                line.matchDash() -> {
                    child = SequenceBlock(context, initialIndex)
                    if (!line.atEnd())
                        child.processLine(line)
                    state = State.CHILD
                    return
                }
                line.match('"') -> line.processDoubleQuotedScalar()
                line.match('\'') -> line.processSingleQuotedScalar()
                line.match('[') -> FlowSequence(context, false).also { it.continuation(line) }
                line.match('{') -> FlowMapping(context, false).also { it.continuation(line) }
                line.match('?') -> {
                    line.skipSpaces()
                    child = MappingBlock(context, initialIndex)
                    if (!line.atEnd())
                        child.processLine(line)
                    state = State.CHILD
                    return
                }
                line.match(':') -> fatal("Can't handle standalone mapping values", line)
                line.match('|') -> {
                    val chomping = line.determineChomping()
                    line.skipSpaces()
                    if (!line.atEnd())
                        fatal("Illegal literal block header", line)
                    LiteralBlockScalar(indent, chomping)
                }
                line.match('>') -> {
                    val chomping = line.determineChomping()
                    line.skipSpaces()
                    if (!line.atEnd())
                        fatal("Illegal folded block header", line)
                    FoldedBlockScalar(indent, chomping)
                }
                else -> line.processPlainScalar(context = context)
            }
            line.skipSpaces()
            if (line.matchColon()) {
                if (line.atEnd()) {
                    child = MappingBlock(context, initialIndex, scalar.text.toString())
                    state = State.CHILD
                }
                else {
                    child = MappingBlock(context, initialIndex, scalar.text.toString(), line)
                    state = State.CHILD
                }
            }
            else { // single line scalar?
                if (scalar.terminated) {
                    node = scalar.getYAMLNode()
                    state = State.CLOSED
                }
                else {
                    child = ChildBlock(context, initialIndex, scalar)
                    state = State.CHILD
                }
            }
        }

        override fun processBlankLine(line: Line) {
            when (state) {
                State.INITIAL -> {}
                State.CHILD -> child.processBlankLine(line)
                State.CLOSED -> {}
            }
        }

        override fun conclude(line: Line): JSONValue? {
            when (state) {
                State.INITIAL -> {}
                State.CHILD -> node = child.conclude(line)
                State.CLOSED -> {}
            }
            state = State.CLOSED
            context.saveNodeProperties(node)
            return node
        }

    }

    class MappingBlock(context: Context, indent: Int) : Block(context, indent) {

        enum class State { KEY, CHILD, QM_CHILD, COLON, CLOSED }

        private var state: State = State.QM_CHILD
        private var child: Block = InitialBlock(context, indent + 1)
        private val properties = JSONObject.Builder()
        private var key: String = ""

        constructor(context: Context, indent: Int, key: String) : this(context, indent) {
            this.key = key
            child = InitialBlock(context.child(key), indent + 1)
            state = State.CHILD
        }

        constructor(context: Context, indent: Int, key: String, line: Line) : this(context, indent) {
            this.key = key
            child = InitialBlock(context.child(key), indent + 1)
            child.processLine(line)
            state = State.CHILD
        }

        override fun processLine(line: Line) {
            var effectiveIndent = line.index
            if (line.matchDash()) {
                effectiveIndent = line.index
                line.revert()
            }
            when (state) {
                State.KEY -> processQM(line)
                State.QM_CHILD -> {
                    if (line.index >= child.indent)
                        child.processLine(line)
                    else {
                        key = child.conclude(line)?.let { if (it is JSONString) it.value else it.toString() } ?: "null"
                        if (properties.containsKey(key))
                            fatal("Duplicate key in mapping - $key", line)
                        state = State.COLON
                        processColon(line)
                    }
                }
                State.COLON -> processColon(line)
                State.CHILD -> {
                    if (effectiveIndent >= child.indent)
                        child.processLine(line)
                    else {
                        properties.add(key, child.conclude(line))
                        state = State.KEY
                        processQM(line)
                    }
                }
                State.CLOSED -> fatal("Unexpected state in YAML processing", line)
            }
        }

        override fun processBlankLine(line: Line) {
            when (state) {
                State.KEY,
                State.COLON,
                State.CLOSED -> {}
                State.CHILD,
                State.QM_CHILD -> child.processBlankLine(line)
            }
        }

        private fun processQM(line: Line) {
            when {
                line.match('?') -> {
                    line.skipSpaces()
                    if (line.atEnd())
                        child = InitialBlock(context, indent + 1)
                    else {
                        child = InitialBlock(context, line.index)
                        child.processLine(line)
                    }
                    state = State.QM_CHILD
                }
                line.atEnd() -> return
                else -> {
                    val scalar = when {
                        line.match('"') -> line.processDoubleQuotedScalar()
                        line.match('\'') -> line.processSingleQuotedScalar()
                        else -> line.processPlainScalar(context = context)
                    }
                    line.skipSpaces()
                    if (line.matchColon()) {
                        key = scalar.text
                        if (properties.containsKey(key))
                            fatal("Duplicate key in mapping - $key", line)
                        line.skipSpaces()
                        if (line.atEnd()) {
                            child = InitialBlock(context.child(key), indent + 1)
                            state = State.CHILD
                        }
                        else {
                            child = InitialBlock(context.child(key), indent + 1)
                            child.processLine(line)
                            state = State.CHILD
                        }
                    }
                    else
                        fatal("Illegal key in mapping", line)
                }
            }
        }

        private fun processColon(line: Line) {
            if (line.match(':')) {
                line.skipSpaces()
                if (line.atEnd())
                    child = InitialBlock(context.child(key), indent + 1)
                else {
                    child = InitialBlock(context.child(key), line.index)
                    child.processLine(line)
                }
                state = State.CHILD
            }
            else
                fatal("Unexpected content in block mapping", line)
        }

        override fun conclude(line: Line): JSONObject {
            when (state) {
                State.KEY -> {}
                State.QM_CHILD,
                State.COLON -> fatal("Block mapping value missing", line)
                State.CHILD -> properties.add(key, child.conclude(line))
                State.CLOSED -> {}
            }
            state = State.CLOSED
            return properties.build()
        }

    }

    class SequenceBlock(context: Context, indent: Int) : Block(context, indent) {

        enum class State { DASH, CHILD, CLOSED }

        private var state: State = State.CHILD
        private val items = JSONArray.Builder()
        private var child: Block = InitialBlock(context.child(0), indent + 2)

        override fun processLine(line: Line) {
            when (state) {
                State.DASH -> processDash(line)
                State.CHILD -> {
                    if (line.index >= child.indent)
                        child.processLine(line)
                    else {
                        items.add(child.conclude(line))
                        state = State.DASH
                        processDash(line)
                    }
                }
                State.CLOSED -> fatal("Unexpected state in YAML processing", line)
            }
        }

        override fun processBlankLine(line: Line) {
            when (state) {
                State.DASH -> {}
                State.CHILD -> child.processBlankLine(line)
                State.CLOSED -> {}
            }
        }

        private fun processDash(line: Line) {
            if (line.matchDash()) {
                if (line.atEnd())
                    child = InitialBlock(context.child(items.size), indent + 2)
                else {
                    child = InitialBlock(context.child(items.size), line.index)
                    child.processLine(line)
                }
                state = State.CHILD
            }
            else
                fatal("Unexpected content in block sequence", line)
        }

        override fun conclude(line: Line): JSONArray {
            when (state) {
                State.DASH -> {}
                State.CHILD -> items.add(child.conclude(line))
                State.CLOSED -> {}
            }
            state = State.CLOSED
            return items.build()
        }

    }

    class ChildBlock(context: Context, indent: Int) : Block(context, indent) {

        enum class State { INITIAL, CONTINUATION, CLOSED }

        private var state: State = State.INITIAL
        private var child: Child = PlainScalar("", context)
        private var node: JSONValue? = null

        constructor(context: Context, indent: Int, scalar: Child) : this(context, indent) {
            this.child = scalar
            state = State.CONTINUATION
        }

        override fun processLine(line: Line) {
            child = when (state) {
                State.INITIAL -> {
                    line.processNodeProperties(context)
                    when {
                        line.isAtEnd -> return
                        line.match('#') -> return
                        line.match('*') -> line.getAnchorName().let {
                            AliasChild(context.anchorMap[it] ?: fatal("Can't locate alias \"$it\"", line))
                        }
                        line.match('"') -> line.processDoubleQuotedScalar()
                        line.match('\'') -> line.processSingleQuotedScalar()
                        line.match('|') -> {
                            val chomping = line.determineChomping()
                            line.skipSpaces()
                            if (!line.atEnd())
                                fatal("Illegal literal block header", line)
                            LiteralBlockScalar(indent, chomping)
                        }
                        line.match('>') -> {
                            val chomping = line.determineChomping()
                            line.skipSpaces()
                            if (!line.atEnd())
                                fatal("Illegal folded block header", line)
                            FoldedBlockScalar(indent, chomping)
                        }
                        line.match('[') -> FlowSequence(context, false).also { it.continuation(line) }
                        line.match('{') -> FlowMapping(context, false).also { it.continuation(line) }
                        line.match('?') -> fatal("Can't handle standalone mapping keys", line)
                        line.match(':') -> fatal("Can't handle standalone mapping values", line)
                        else -> line.processPlainScalar(context = context)
                    }
                }
                State.CONTINUATION -> child.continuation(line)
                State.CLOSED -> fatal("Unexpected state in YAML processing", line)
            }
            line.skipSpaces()
            if (line.atEnd()) {
                if (child.terminated) {
                    node = child.getYAMLNode()
                    state = State.CLOSED
                }
                else
                    state = State.CONTINUATION
            }
            else
                fatal("Illegal data following scalar", line)
        }

        override fun processBlankLine(line: Line) {
            when (state) {
                State.INITIAL -> {}
                State.CONTINUATION -> child.continuation(line)
                State.CLOSED -> {}
            }
        }

        override fun conclude(line: Line): JSONValue? {
            when (state) {
                State.INITIAL -> {}
                State.CONTINUATION -> {
                    if (child.complete)
                        node = child.getYAMLNode()
                    else
                        fatal("Incomplete scalar", line)
                }
                State.CLOSED -> {}
            }
            state = State.CLOSED
            context.saveNodeProperties(node)
            return node
        }

    }

    abstract class Child(var terminated: Boolean) { // rename Nested ??? or Flow ???

        open val complete: Boolean
            get() = terminated

        abstract val text: CharSequence

        abstract fun getYAMLNode(): JSONValue?

        abstract fun continuation(line: Line): Child

    }

    class AliasChild(private val node: JSONValue): Child(true) {

        override val text: CharSequence
            get() = getYAMLNode().toString()

        override fun continuation(line: Line): Child {
            fatal("Illegal data after alias", line)
        }

        override fun getYAMLNode(): JSONValue = node

    }

    class FlowSequence(private val context: Context, terminated: Boolean) : Child(terminated) {

        enum class State { ITEM, CONTINUATION, COMMA, CLOSED }

        override val text: CharSequence
            get() = getYAMLNode().toString()

        private var state: State = State.ITEM
        private var seqContext = context.child(0)
        private var child: Child = FlowNode("", seqContext)
        private var key: String? = null
        private val items = JSONArray.Builder()

        private fun processLine(line: Line) {
            while (!line.atEnd()) {
                if (state != State.COMMA) {
                    when (state) {
                        State.ITEM -> {
                            line.skipSpaces()
                            line.processNodeProperties(seqContext)
                            child = when {
                                line.match('"') -> line.processDoubleQuotedScalar()
                                line.match('\'') -> line.processSingleQuotedScalar()
                                line.match('[') -> FlowSequence(seqContext, false).also { it.continuation(line) }
                                line.match('{') -> FlowMapping(seqContext, false).also { it.continuation(line) }
                                line.match('*') -> line.getAnchorName().let {
                                    AliasChild(seqContext.anchorMap[it] ?: fatal("Can't locate alias \"$it\"", line))
                                }
                                else -> line.processFlowNode(context = seqContext)
                            }
                        }
                        State.CONTINUATION -> child = child.continuation(line)
                        else -> {}
                    }
                    line.skipSpaces()
                    if (line.atEnd()) {
                        state = when {
                            child.terminated -> State.COMMA
                            else -> State.CONTINUATION
                        }
                        break
                    }
                }
                when {
                    line.match(']') -> {
                        val item = key?.let { JSONObject.of(it to child.getYAMLNode()) } ?: child.getYAMLNode()
                        item?.let { items.add(it) }
                        seqContext.saveNodeProperties(item)
                        terminated = true
                        state = State.CLOSED
                        break
                    }
                    line.matchColon() -> {
                        key = child.getYAMLNode().toString()
                        state = State.ITEM
                    }
                    line.match(',') -> {
                        val item = key?.let { JSONObject.of(it to child.getYAMLNode()) } ?: child.getYAMLNode()
                        items.add(item)
                        seqContext.saveNodeProperties(item)
                        key = null
                        seqContext = context.child(items.size)
                        state = State.ITEM
                    }
                    else -> fatal("Unexpected character in flow sequence", line)
                }
            }
        }

        override fun continuation(line: Line): Child {
            if (state != State.CLOSED)
                processLine(line)
            return this
        }

        override fun getYAMLNode() = items.build()

    }

    class FlowMapping(private val context: Context, terminated: Boolean) : Child(terminated) {

        enum class State { ITEM, CONTINUATION, COMMA, CLOSED }

        private var state: State = State.ITEM
        private var mapContext = context.child("")
        private var child: Child = FlowNode("", mapContext)
        private var key: String? = null
        private val properties = JSONObject.Builder()

        override val text: CharSequence
            get() = getYAMLNode().toString()

        override fun continuation(line: Line): Child {
            if (state != State.CLOSED)
                processLine(line)
            return this
        }

        override fun getYAMLNode() = properties.build()

        private fun processLine(line: Line) {
            while (!line.atEnd()) {
                if (state != State.COMMA) {
                    when (state) {
                        State.ITEM -> {
                            line.skipSpaces()
                            line.processNodeProperties(mapContext)
                            child = when {
                                line.match('"') -> line.processDoubleQuotedScalar()
                                line.match('\'') -> line.processSingleQuotedScalar()
                                line.match('[') -> FlowSequence(mapContext, false).also { it.continuation(line) }
                                line.match('{') -> FlowMapping(mapContext, false).also { it.continuation(line) }
                                line.match('*') -> line.getAnchorName().let {
                                    AliasChild(mapContext.anchorMap[it] ?: fatal("Can't locate alias \"$it\"", line))
                                }
                                else -> line.processFlowNode(context = mapContext)
                            }
                        }
                        State.CONTINUATION -> child = child.continuation(line)
                        else -> {}
                    }
                    line.skipSpaces()
                    if (line.atEnd()) {
                        state = when {
                            child.terminated -> State.COMMA
                            else -> State.CONTINUATION
                        }
                        break
                    }
                }
                when {
                    line.match('}') -> {
                        key?.let { addProperty(it, child.getYAMLNode()) } ?: run {
                            if (child.getYAMLNode() != null)
                                fatal("Unexpected end of flow mapping", line)
                        }
                        terminated = true
                        state = State.CLOSED
                        break
                    }
                    line.matchColon() || child is DoubleQuotedScalar && line.match(':') -> {
                        child.getYAMLNode().toString().let {
                            key = it
                            mapContext = context.child(it)
                        }
                        state = State.ITEM
                    }
                    line.match(',') -> {
                        key?.let { addProperty(it, child.getYAMLNode()) } ?: fatal("Key missing in flow mapping", line)
                        key = null
                        mapContext = context.child("")
                        state = State.ITEM
                    }
                    else -> fatal("Unexpected character in flow mapping", line)
                }
            }
        }

        private fun addProperty(key: String, value: JSONValue?) {
            properties.add(key, value)
            mapContext.saveNodeProperties(value)
        }

    }

    abstract class FlowScalar(override val text: String, terminated: Boolean) : Child(terminated) {

        override fun getYAMLNode(): JSONValue? = JSONString(text)

    }

    open class PlainScalar(text: String, val context: Context) : FlowScalar(text, false) {

        override val complete: Boolean
            get() = true

        override fun continuation(line: Line): Child {
            return line.processPlainScalar("$text ", context)
        }

        override fun getYAMLNode(): JSONValue? {
            context.tag?.let {
                if (it == strTag)
                    return JSONString(text)
                if (it == floatTag && text.matchesInteger())
                    return JSONDecimal(text)
                if (it == intTag && !text.matchesInteger() && text.matchesDecimal() && JSONDecimal(text).isIntegral())
                    return intOrLong(BigDecimal(text).toLong())
            }
            context.version?.let {
                if (it.second < 2) {
                    if (text == "yes" || text == "Yes" || text == "YES" || text == "on" || text == "On" || text == "ON")
                        return JSONBoolean.TRUE
                    if (text == "no" || text == "No" || text == "NO" || text == "off" || text == "Off" || text == "OFF")
                        return JSONBoolean.FALSE
                    if (text.length > 1 && text.startsWith('0') && text.drop(1).all { d -> d in '0'..'7' })
                        return intOrLong(text.toLong(8))
                }
            }
            if (text.isEmpty() || text == "null" || text == "Null" || text == "NULL" || text == "~")
                return null
            if (text == "true" || text == "True" || text == "TRUE")
                return JSONBoolean.TRUE
            if (text == "false" || text == "False" || text == "FALSE")
                return JSONBoolean.FALSE
            if (text.length > 2) {
                if (text.startsWith("0o") && text.drop(2).all { it in '0'..'7' })
                    return intOrLong(text.drop(2).toLong(8))
                if (text.startsWith("0x") && text.drop(2).all { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' })
                    return intOrLong(text.drop(2).toLong(16))
            }
            if (text.matchesInteger())
                return intOrLong(text.toLong())
            if (text.matchesDecimal())
                return JSONDecimal(text)
            if (context.tag == null && text in floatNamedConstants)
                context.tag = floatTag
            return JSONString(text)
        }

        private fun intOrLong(value: Long): JSONValue {
            return if (value in Int.MIN_VALUE..Int.MAX_VALUE)
                JSONInt(value.toInt())
            else
                JSONLong(value)
        }

        private fun String.matchesInteger(): Boolean {
            return TextMatcher(this).let {
                it.match('+') || it.match('-')
                it.matchDec() && it.isAtEnd
            }
        }

        private fun String.matchesDecimal(): Boolean {
            val pt = TextMatcher(this)
            pt.match('+') || pt.match('-')
            if (pt.match('.')) {
                if (!pt.matchDec())
                    return false
            }
            else {
                if (!pt.matchDec())
                    return false
                if (pt.match('.'))
                    pt.matchDec()
            }
            if (pt.match('e') || pt.match('E')) {
                pt.match('+') || pt.match('-')
                if (!pt.matchDec())
                    return false
            }
            return pt.isAtEnd
        }

    }

    class FlowNode(text: String, context: Context) : PlainScalar(text, context) {

        override fun continuation(line: Line): Child {
            return line.processFlowNode("$text ", context)
        }

    }

    class DoubleQuotedScalar(text: String, terminated: Boolean, private val escapedNewline: Boolean = false) :
            FlowScalar(text, terminated) {

        override fun continuation(line: Line) =
                line.processDoubleQuotedScalar(if (escapedNewline || text.endsWith(' ')) text else "$text ")

    }

    class SingleQuotedScalar(text: String, terminated: Boolean) : FlowScalar(text, terminated) {

        override fun continuation(line: Line) =
            line.processSingleQuotedScalar(if (text.endsWith(' ')) text else "$text ")

    }

    abstract class BlockScalar(private var indent: Int, private val chomping: Chomping) : Child(false) {

        enum class Chomping { STRIP, KEEP, CLIP }

        enum class State { INITIAL, CONTINUATION }

        private var state: State = State.INITIAL
        override val text = StringBuilder()

        override val complete: Boolean
            get() = true

        abstract fun appendText(string: String)

        override fun continuation(line: Line): BlockScalar {
            when (state) {
                State.INITIAL -> {
                    if (line.index > indent)
                        indent = line.index
                    if (!line.isAtEnd) {
                        state = State.CONTINUATION
                        line.skipToEnd()
                        appendText(line.result)
                    }
                }
                State.CONTINUATION -> {
                    if (line.isAtEnd) {
                        if (line.index > indent) {
                            line.start = indent
                            appendText(line.result)
                        }
                        else
                            appendText("")
                    }
                    else {
                        if (line.index < indent) {
                            if (!line.isComment())
                                fatal("Bad indentation in block scalar", line)
                        }
                        else {
                            if (line.index > indent)
                                line.index = indent
                            line.skipToEnd()
                            appendText(line.result)
                        }
                    }
                }
            }
            return this
        }

        override fun getYAMLNode(): JSONString {
            val sb = StringBuilder(text)
            when (chomping) {
                Chomping.STRIP -> sb.strip()
                Chomping.CLIP -> {
                    sb.strip()
                    sb.append("\n")
                }
                Chomping.KEEP -> {}
            }
            return JSONString(sb.toString())
        }

        private fun StringBuilder.strip() {
            while (endsWith('\n'))
                setLength(length - 1)
        }

    }

    class LiteralBlockScalar(indent: Int, chomping: Chomping) : BlockScalar(indent, chomping) {

        override fun appendText(string: String) {
            text.append(string)
            text.append('\n')
        }

    }

    class FoldedBlockScalar(indent: Int, chomping: Chomping) : BlockScalar(indent, chomping) {

        override fun appendText(string: String) {
            if (text.endsWith('\n')) {
                text.setLength(text.length - 1)
                while (text.endsWith(' '))
                    text.setLength(text.length - 1)
                text.append(' ')
            }
            text.append(string)
            text.append('\n')
        }

    }

    companion object {

        val log = getLogger()

        val floatNamedConstants = setOf(".nan", ".NaN", ".NAN", ".inf", ".Inf", ".INF", "+.inf", "+.Inf", "+.INF",
                "-.inf", "-.Inf", "-.INF", )

        private fun warn(message: String) {
            log.warn { "YAML Warning: $message" }
        }

        private fun fatal(text: String, line: Line): Nothing {
            val exception = YAMLParseException(text, line)
            log.error { exception.message }
            throw exception
        }

        fun Line.processPlainScalar(initial: String = "", context: Context): PlainScalar {
            val sb = StringBuilder(initial)
            while (!isAtEnd) {
                when {
                    matchColon() -> {
                        revert()
                        skipBackSpaces() // ???
                        break
                    }
                    isComment() -> {
                        skipBackSpaces() // ???
                        break
                    }
                    else -> sb.append(nextChar())
                }
            }
            return PlainScalar(sb.toString().trim(), context)
        }

        fun Line.processFlowNode(initial: String = "", context: Context): FlowNode {
            val sb = StringBuilder(initial)
            while (!isAtEnd) {
                when {
                    matchAny("[]{},") -> {
                        revert()
                        return FlowNode(sb.toString().trim(), context).also { it.terminated = true }
                    }
                    matchColon() -> {
                        revert()
                        skipBackSpaces() // ???
                        break
                    }
                    isComment() -> {
                        skipBackSpaces() // ???
                        break
                    }
                    else -> sb.append(nextChar())
                }
            }
            return FlowNode(sb.toString().trim(), context)
        }

        fun Line.processSingleQuotedScalar(initial: String = ""): SingleQuotedScalar {
            val sb = StringBuilder(initial)
            while (!isAtEnd) {
                when {
                    match('\'') -> {
                        if (match('\''))
                            sb.append('\'')
                        else
                            return SingleQuotedScalar(sb.toString(), true)
                    }
                    else -> sb.append(nextChar())
                }
            }
            return SingleQuotedScalar(sb.toString(), false)
        }

        fun Line.processDoubleQuotedScalar(initial: String = ""): DoubleQuotedScalar {
            val sb = StringBuilder(initial)
            while (!isAtEnd) {
                when {
                    match('"') -> return DoubleQuotedScalar(sb.toString(), true)
                    match('\\') -> {
                        if (isAtEnd)
                            return DoubleQuotedScalar(sb.toString(), false, escapedNewline = true)
                        processBackslash(sb)
                    }
                    else -> sb.append(nextChar())
                }
            }
            return DoubleQuotedScalar(sb.toString(), false)
        }

        private fun Line.processBackslash(sb: StringBuilder) {
            when {
                match('0') -> sb.append('\u0000')
                match('a') -> sb.append('\u0007')
                match('b') -> sb.append('\b')
                match('t') -> sb.append('\t')
                match('\t') -> sb.append('\t')
                match('n') -> sb.append('\n')
                match('v') -> sb.append('\u000B')
                match('f') -> sb.append('\u000C')
                match('r') -> sb.append('\r')
                match('e') -> sb.append('\u001B')
                match(' ') -> sb.append(' ')
                match('"') -> sb.append('"')
                match('/') -> sb.append('/')
                match('\\') -> sb.append('\\')
                match('N') -> sb.append('\u0085')
                match('_') -> sb.append('\u00A0')
                match('L') -> sb.append('\u2028')
                match('P') -> sb.append('\u2029')
                match('x') -> {
                    if (!matchHex(2, 2))
                        fatal("Illegal hex value in double quoted scalar", this)
                    sb.append(resultHexInt.toChar())
                }
                match('u') -> {
                    if (!matchHex(4, 4))
                        fatal("Illegal unicode value in double quoted scalar", this)
                    sb.append(resultHexInt.toChar())
                }
                match('U') -> {
                    if (!matchHex(8, 8))
                        fatal("Illegal unicode value in double quoted scalar", this)
                    val codePoint = resultHexInt
                    when {
                        Character.isSupplementaryCodePoint(codePoint) -> {
                            sb.append(Character.highSurrogate(codePoint))
                            sb.append(Character.lowSurrogate(codePoint))
                        }
                        Character.isBmpCodePoint(codePoint) -> sb.append(codePoint.toChar())
                        else -> fatal("Illegal 32-bit unicode value in double quoted scalar", this)
                    }
                }
                else -> fatal("Illegal escape sequence in double quoted scalar", this)
            }
        }

        fun Line.processNodeProperties(context: Context) {
            while (true) {
                when {
                    match('&') -> {
                        if (context.anchor != null)
                            fatal("Duplicate anchor", this)
                        context.anchor = getAnchorName()
                    }
                    match('!') -> {
                        if (context.tag != null)
                            fatal("Duplicate tag", this)
                        if (match('<')) { // verbatim tag
                            val startTag = index
                            match('!')
                            if (!matchURI())
                                fatal("Illegal verbatim local tag", this)
                            val endTag = index
                            if (!match('>'))
                                fatal("Illegal verbatim local tag", this)
                            context.tag = getString(startTag, endTag)
                        }
                        else { // tag shorthand
                            val handle: String = when {
                                match('!') -> "!!"
                                matchSeq { it.isTagHandleChar() } && matchContinue(1, 1) { it == '!'} -> {
                                    getString(start - 1, index)
                                }
                                else -> "!"
                            }
                            val prefix = context.getTagHandle(handle) ?: fatal("Tag handle $handle not declared", this)
                            skip { it !in " \t[]{},!" }
                            val suffix = result
                            context.tag = "$prefix${suffix.decodePercentEncoding(this)}"
                        }
                        skipSpaces()
                    }
                    else -> break
                }
            }
        }

        private fun String.decodePercentEncoding(line: Line): String = mapSubstrings {
            if (this[it] == '%') {
                buildResult(this, it, 3, "Incomplete percent sequence") {
                    try {
                        (this[it + 1].fromHexDigit() shl 4) or this[it + 2].fromHexDigit()
                    } catch (_: NumberFormatException) {
                        fatal("Illegal percent sequence", line)
                    }
                }
            }
            else
                null
        }

        fun Line.getAnchorName(): String {
            if (!matchSeq { it !in " \t[]{}," })
                fatal("Anchor name missing", this)
            return result.also { skipSpaces() }
        }

        private fun Line.determineChomping(): BlockScalar.Chomping = when {
            match('-') -> BlockScalar.Chomping.STRIP
            match('+') -> BlockScalar.Chomping.KEEP
            else -> BlockScalar.Chomping.CLIP
        }

        private fun Char.isTagHandleChar(): Boolean =
                this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9' || this == '-'

        private fun TextMatcher.matchURI(): Boolean {
            val uriStart = index
            while (true) {
                when {
                    matchSeq { it.isTagHandleChar() || it in "#;/?:@&=+\$,_.~*'()[]" } -> {}
                    match('%') && matchContinue(TextMatcher::isHexDigit) -> {
                        if (!matchHex(2, 2)) {
                            index--
                            break
                        }
                    }
                    else -> break
                }
            }
            if (index > uriStart) {
                start = uriStart
                return true
            }
            return false
        }

    }

}
