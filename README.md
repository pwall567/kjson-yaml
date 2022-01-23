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
    val description = pointer.find(yamlDocument.rootNode)
```

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
- `%YAML` directive

Not yet implemented:

- Anchors and Aliases
- Directives other than `%YAML`
- Tags
- Multiple documents in a single file
- Named floating-point pseudo-values (`.inf`, `.nan`)

Also, the parser may not yet meet the specification in all respects, even for the constructs that it does handle.

## Dependency Specification

The latest version of the library is 1.1, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>io.kjson</groupId>
      <artifactId>kjson-yaml</artifactId>
      <version>1.1</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'io.kjson:kjson-yaml:1.1'
```
### Gradle (kts)
```kotlin
    implementation("io.kjson:kjson-yaml:1.1")
```

Peter Wall

2022-01-24
