# kjson-yaml

[![Build Status](https://travis-ci.com/pwall567/kjson-yaml.svg?branch=main)](https://app.travis-ci.com/github/pwall567/kjson-yaml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.6.10&color=7f52ff&logo=kotlin&logoColor=7f52ff)](https://github.com/JetBrains/kotlin/releases/tag/v1.6.10)
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
The tree may be navigated as if it were a JSON structure, using the [kjson-core](https://github.com/pwall567/kjson-core)
or [kjson-pointer](https://github.com/pwall567/kjson-pointer) libraries or others.

For example, to retrieve the `description` property of the `info` section of an OpenAPI 3.0 YAML file:
```kotlin
    val file = File("path.to.swagger.file")
    val yamlDocument = YAML.parse(file)
    val pointer = JSONPointer("/info/description")
    val description = yamlDocument.rootNode[pointer]
```

## Tags

Because YAML tags do not form part of the JSON structure, any tags present in the YAML are stored in a separate map,
accessed by the `JSONPointer` pointing to the node.
The function `getTag()` on the `YAMLDocument` will return the tag for the nominated node, or the default tag for the
node type if no tag was specified.

For example, following the above code to get the `description` node of an OpenAPI file, the tag for that node may be
retrieved using:
```kotlin
    val tag = yamlDocument.getTag(pointer)
```

Unless the tag was explicitly overridden, this will return `tag:yaml.org,2002:str` (the default tag for a string node).

## Implemented Subset

This parser does not implement the full [YAML specification](https://yaml.org/spec/1.2/spec.html).
The currently implemented subset includes:

- Block Mappings
- Block Sequences
- Block Scalars (literal and folded)
- Flow Scalars (plain, single quoted and double quoted)
- Flow Sequences
- Flow Mappings
- Comments
- Tags
- Anchors and Aliases
- `%YAML` directive
- `%TAG` directive

Not yet implemented:

- Multiple documents in a single file

Also, the parser may not yet meet the specification in all respects, even for the constructs that it does handle.

## Dependency Specification

The latest version of the library is 1.10, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>io.kjson</groupId>
      <artifactId>kjson-yaml</artifactId>
      <version>1.10</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'io.kjson:kjson-yaml:1.10'
```
### Gradle (kts)
```kotlin
    implementation("io.kjson:kjson-yaml:1.10")
```

Peter Wall

2022-11-17
