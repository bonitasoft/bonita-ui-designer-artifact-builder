# Bonita UI Designer Artifact Builder

[![Build](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/workflows/Build/badge.svg)](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/actions/workflows/build.yml)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=bonitasoft_bonita-ui-designer-artifact-builder&metric=alert_status)](https://sonarcloud.io/dashboard?id=bonitasoft_bonita-ui-designer-artifact-builder)
[![GitHub release](https://img.shields.io/github/v/release/bonitasoft/bonita-ui-designer-artifact-builder?color=blue&label=Release)](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.bonitasoft.web/ui-designer-artifact-builder.svg?label=Maven%20Central&color=orange&logo=apachemaven)](https://central.sonatype.com/artifact/org.bonitasoft.web/ui-designer-artifact-builder/)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

Build pages designed with the [UI Designer][uid-repo] for your Bonita application or your own project.

## Quick start

### Pre-requisite

* [Java 17][java] for compilation

### Build

#### Using Maven

* Build it using maven `./mvnw clean verify`

**Note**: if an error occurs at phantomjs startup (especially when running on Ubuntu 24), disable openssl by setting an environment variable `OPENSSL_CONF=/dev/null`.

## Upgrading to 2.0

Version 2.0 migrates the library from the `javax` to the `jakarta` namespace. For consumers this means:

* **Java 17 or later** is required (was Java 11).
* The library now uses the jakarta stack: Bean Validation constraints are `jakarta.validation.*` (Bean Validation 3.x, implemented by Hibernate Validator 8), including in public signatures such as `BeanValidator(jakarta.validation.Validator)` and `ConstraintValidationException` - code constructing these directly must migrate its own imports.
* The `ui-designer-artifact-builder-dependencies` BOM imports Spring Boot 3.5.x dependency management instead of 2.7.x. In particular `org.glassfish:jakarta.el` is no longer managed there: the library ships `org.glassfish.expressly:expressly` as its EL implementation instead.
* The `model` and `common` artifacts no longer bring Hibernate Validator transitively - it moved to test scope there (it was compile scope in 1.x). If you depend on either of them (or on their test-jars) without `ui-designer-artifact-builder` and your own code calls `Validation.buildDefaultValidatorFactory()`, declare `org.hibernate.validator:hibernate-validator` and `org.glassfish.expressly:expressly` yourself, in the scope where you need them. Projects depending on `ui-designer-artifact-builder` still get the implementation transitively (runtime scope, hence on the test classpath too).

## Contribute

### Report issues

If you want to report an issue or a bug use our [official bugtracker](https://bonita.atlassian.net/projects/BBPMC)

### How to contribute

Before contributing, read the [guidelines](CONTRIBUTING.md)

### Branching strategy

This repository follows the [gitflow branching strategy](https://gitversion.net/docs/learn/branching-strategies/gitflow/examples).

### Release

To release a new version, maintainers may use the Release and Publication GitHub actions.

* Release action will invoke the `gitflow-maven-plugin` to perform all required merges, version updates and tag creation.
* Publication action will build and deploy a given tag to Maven Central
* A Github release should be created and associated to the tag.

## Resources

* [Documentation][documentation]


[java]: https://adoptium.net/temurin/releases/?version=17
[uid-repo]: https://github.com/bonitasoft/bonita-ui-designer
[documentation]: https://documentation.ofelia.com

    
