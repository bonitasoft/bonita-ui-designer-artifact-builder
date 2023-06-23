# Bonita UI Designer Artifact Builder

[![Build](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/workflows/Build/badge.svg)](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/actions/workflows/build.yml)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=bonitasoft_bonita-ui-designer-artifact-builder&metric=alert_status)](https://sonarcloud.io/dashboard?id=bonitasoft_bonita-ui-designer-artifact-builder)
[![GitHub release](https://img.shields.io/github/v/release/bonitasoft/bonita-ui-designer-artifact-builder?color=blue&label=Release)](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/releases)
[![Maven Central](https://img.shields.io/maven-central/v/org.bonitasoft.web/ui-designer-artifact-builder.svg?label=Maven%20Central&color=orange&logo=apachemaven)](https://central.sonatype.com/artifact/org.bonitasoft.web/ui-designer-artifact-builder/)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

Build pages designed with the [UI Designer][uid-repo] for your Bonita application or your own project.

## Quick start

### Pre-requisite

* [Java 11][java] for compilation

### Build

#### Using Maven

* Build it using maven `./mvnw clean verify`

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


[java]: https://adoptium.net/temurin/releases/?version=11
[uid-repo]: https://github.com/bonitasoft/bonita-ui-designer
[documentation]: https://documentation.bonitasoft.com

    
