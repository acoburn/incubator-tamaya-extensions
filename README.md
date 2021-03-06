# Apache Tamaya (incubating)

Tamaya is a very powerful yet flexible configuration solution. Its core is built based on a few simple concepts.
There are at least two main usage scenarios for Tamaya, which are synergetic:

- In an enterprise context you can easily implement a configuration architecture for your whole company and deploy the
  logic as an extension module. All application development teams in your company can then depend on this module (and the
  basic Tamaya core implementation). As a result all applications/modules in your company follow the same configuration
  policy, which makes it much more simpler to move people between your teams. Similarly additional tooling functionality
  can help you to manage configuration on application as well as on enterprise level, e.g. providing command line or
  REST support to access the supported configuration entries, types and values, configuration configModel and more.
- If you are writing an application, application component or library you can support configuration using Tamaya by
  adding it as an optional dependency. If done so your users/customers can use Tamaya to connect their current enterprise
  configuration infrastructure transparently to your code. As an example you can use Tamaya to read your default
  configuration files, but since Tamaya is so easily extendable, customers can deploy an additional jar, which then
  allows them to add their own configuration mechanisms such as databases, datagrids or REST services.

More information on Tamaya can be found on the [homepage of Apache Tamaya](https://tamaya.incubator.apache.org/).

## Tamaya Extensions

The extensions module contains all currently available and supported extension modules to enhance and use Tamaya more broadly.

## Building Apache Tamaya Extensions

The Apache Tamaya project is built with [Maven 3](https://maven.apache.org/) and [Java 8](https://java.sun.com/), so you need JDK >=1.8 and a reasonable version of maven
installed on your computer.


Then you can build Tamaya extensions via:
```
$ export MAVEN_OPTS="-Xmx512m -XX:PermGenSpace=200m"
$ mvn
```

### Travis / CI

Apart from integration into ASF CI there's a travis build:

[![Build Status](https://travis-ci.org/apache/incubator-tamaya-extensions.svg?branch=master)](https://travis-ci.org/apache/incubator-tamaya-extensions/branches)

