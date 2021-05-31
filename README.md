# YAML to Properties Maven Plugin



This Maven plugin simply converts more human-readable [**YAML**](https://yaml.org/) (`.yml`, `.yaml`) files to Java's [**Properties**](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html) (`.properties`) files.



### Purpose

This can be particularly useful when using a framework requiring `.properties` files but you want to benefit of the [**YAML syntax's advantages**](https://www.baeldung.com/spring-yaml-vs-properties) (more human-friendly, less repetitive, more concise, etc…).



### Encoding support

The plugin supports [**encoding/charset**](https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html) configuration, both for input files reading and output files writing.

Which can be particularly useful when defining resources files for internationalization framework, which can contains special/encoded characters.

