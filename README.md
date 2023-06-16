# Bonita UI Designer Artifact Builder ![Build](https://github.com/bonitasoft/bonita-ui-designer-artifact-builder/workflows/Build/badge.svg)

Build pages designed with the UI Designer for your Bonita application or your own project.

## Quick start

### Pre-requisite

* [Maven][maven]
* [Java 11][java] for compilation

### Build

#### Using Maven

* Build it using maven `mvn clean package`

## To go further

### How does it work

The UI-designer is composed of a Java backend application and an AngularJs frontend.
It is packaged in a war file and provided by default in the [Bonita Studio][studio-repo]

It produces standalone AngularJs pages that are compatible with Bonita platform.

## Contribute


### Report issues

If you want to report an issue or a bug use our [official bugtracker](https://bonita.atlassian.net/projects/BBPMC)

### How to contribute
Before contributing, read the [guidelines][contributing.md]

### Build and Test

#### Build

You can build entire project using maven.
    
    mvn clean package   
    

## Resources

* [Documentation][documentation]


[maven]: https://maven.apache.org/
[java]: https://www.java.com/fr/download/
[uid-repo]: https://github.com/bonitasoft/bonita-ui-designer
[download]: https://www.bonitasoft.com/downloads
[documentation]: https://documentation.bonitasoft.com
[contributing.md]: https://github.com/bonitasoft/bonita-developer-resources/blob/master/CONTRIBUTING.MD

    
