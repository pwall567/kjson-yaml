# kjson-yaml

[![Build Status](https://github.com/pwall567/kjson-yaml/actions/workflows/build.yml/badge.svg)](https://github.com/pwall567/kjson-yaml/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.9.24&color=7f52ff&logo=kotlin&logoColor=7f52ff)](https://github.com/JetBrains/kotlin/releases/tag/v1.9.24)
[![Maven Central](https://img.shields.io/maven-central/v/io.kjson/kjson-yaml?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.kjson%22%20AND%20a:%22kjson-yaml%22)

Kotlin YAML processor.

This library is derived from the earlier [`yaml-simple`](https://github.com/pwall567/yaml-simple) library.
It has been adapted to work with the [`kjson-core`](https://github.com/pwall567/kjson-core) JSON Kotlin library.

## Quick Start

To parse a YAML file:
```kotlin
    val file = File("path.to.file")
    val yamlDocument = YAML.parse(file)
```

The result is a `YAMLDocument`, and the `rootNode` property contains the root (or only) node of the tree of YAML nodes.
The tree may be navigated as if it were a JSON structure, using the
[`kjson-core`](https://github.com/pwall567/kjson-core) or [`kjson-pointer`](https://github.com/pwall567/kjson-pointer)
libraries or others.

For example, to retrieve the `description` property of the `info` section of an OpenAPI 3.0 YAML file:
```kotlin
    val file = File("path.to.swagger.file")
    val yamlDocument = YAML.parse(file)
    val pointer = JSONPointer("/info/description")
    val description = yamlDocument.rootNode[pointer].asString
```

## Background

[Version 1.2 of YAML](https://yaml.org/spec/1.2.2/) had as one of its main design goals making YAML a valid superset of
JSON.
That means that many uses of JSON (for example, configuration files, or even JSON Schema files) may now be coded in YAML
if that is the preferred representation, and the code to interpret those files may be written to be independent of the
form used.

The `kjson-yaml` library provides functions to parse a YAML file into a structure that may be navigated as if it had
been parsed from JSON.
It uses the internal JSON representations from the [`kjson-core`](https://github.com/pwall567/kjson-core) library, and
the YAML node types map to `kjson-core` classes as follows:

- scalars containing integer values that can be represented in a Kotlin `Int`: `JSONInt`
- scalars containing integer values bigger than an `Int` but that will fit in a `Long`: `JSONLong`
- all other numeric scalars: `JSONDecimal`
- boolean scalars: `JSONBoolean`
- all other scalars: `JSONString`
- sequences (both block and flow sequences): `JSONArray`
- mappings (both block and flow mappings): `JSONObject`

The documentation for the [`kjson-core`](https://github.com/pwall567/kjson-core) library includes comprehensive details
on accessing the data in the result classes, but the following hints may be all that many users need:

1. The value of a `JSONString`, `JSONInt`, `JSONLong`, `JSONDecimal` or `JSONBoolean` may be obtained using the `value`
   property (`String`, `Int`, `Long`, `BigDecimal` or `Boolean` respectively).
2. `JSONString` also implements `CharSequence`, so it may be used directly in cases where `CharSequence` is required,
   and the indexing, iteration and `subSequence` functions are all available.
3. `JSONArray` implements `List<JSONValue?>`, so it may be iterated over using the standard Kotlin functions.
4. `JSONObject` implements `Map<String, JSONValue?>`, so all of the standard Kotlin lookup and iteration functions are
   available.

## Problematic Constructs

Just because all JSON can be parsed as YAML, that doesn&rsquo;t mean that all YAML may be parsed as JSON.
There are three main areas where a workaround is required.

### Tags

Tags are a form of metadata that may be applied to any YAML node.
There is no equivalent in JSON, so `kjson-yaml` stores tags in a separate structure, allowing tag-aware applications to
interrogate the tags of a YAML document, without impacting on JSON compatibility for those who don&rsquo;t use them.

The function `getTag()` on the `YAMLDocument` will return the tag for the nominated node (specified by means of a
`JSONPointer`), or the default tag for the node type if no tag was specified.

For example, following the above code to get the `description` node of an OpenAPI file, the tag for that node may be
retrieved using:
```kotlin
    val tag = yamlDocument.getTag(pointer)
```

Unless the tag was explicitly overridden, this will return `tag:yaml.org,2002:str` (the default tag for a string node).

### Floating-Point Special Values

The constants `.nan`, `.inf` and `-.inf` have no representation in `kjson-core`, which uses `BigDecimal` for
floating-point numbers.
Any occurrence of these values as unquoted flow scalars will result in `JSONString` nodes containing the text of the
constant, but the tag for the node will be set (unless it has been set explicitly) to `tag:yaml.org,2002:float`, the
default tag for floating-point numbers.

### Complex Nodes as Mapping Keys

YAML allows the key of a mapping to be a complex node, such as a sequence or another mapping.
JSON requires that object property names be strings, so any non-string mapping keys are converted to JSON, and the JSON
form is then used as the string key for the mapping.

(This is a very obscure case that almost never occurs in practice, so if you didn&rsquo;t understand this section, you
are very unlikely to be affected by it.  But at least you can be assured that it is covered.)

## Anchors and Aliases

Anchors and aliases provide a means of nominating a YAML node (which may be a complex structure) for later re-use in the
same document.
For example:
```yaml
physical-address: &ADDR
  street: 21 Wonder St
  location: Anytown
  state: XY
postal-address: *ADDR
```

Because `kjson-core` values are immutable, the same internal representation will be used for all alias references to the
anchor node, meaning that this technique will save both memory and processing time, as well as coding effort.

## Multiple Documents

The YAML specification allows for multiple YAML documents to be concatenated in a single file, known as a stream.
The `kjson-yaml` library allows this usage, but it requires the use of the `parseStream()` function, which returns a
`List` instead of a single document:
```kotlin
    val file = File("path.to.swagger.file")
    val yamlDocuments: List<YAMLDocument> = YAML.parseStream(file)
```
The documents are independent of each other, but error message line numbers will start from the beginning of the file,
not the document.

## Examples from Specification

To confirm the ability of the library to handle all forms of YAML, the tests for the library include all of the examples
from [Chapter 2](https://yaml.org/spec/1.2.2/#language-overview) of the specification.
To examine the code used to inspect the results (and for guidance on the JSON equivalents of the YAML), see the class
[`SpecExampleTest`](https://github.com/pwall567/kjson-yaml/blob/main/src/test/kotlin/io/kjson/yaml/SpecExampleTest.kt)
in the test section of this project.

## Dependency Specification

The latest version of the library is 3.6, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>io.kjson</groupId>
      <artifactId>kjson-yaml</artifactId>
      <version>3.6</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'io.kjson:kjson-yaml:3.6'
```
### Gradle (kts)
```kotlin
    implementation("io.kjson:kjson-yaml:3.6")
```

Peter Wall

2024-12-15
