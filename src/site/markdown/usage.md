# Usage

As also specified in the "Goals" section, the simplest usage of this plugin within your project is to add in your ``pom.xml`` this way:

```xml
<project>
  ...
  <build>
    ...
    <!-- To use the plugin goals in your POM or parent POM -->
    <plugins>
      ...
      <plugin>
        <groupId>io.github.cernier</groupId>
        <artifactId>yml2props-maven-plugin</artifactId>
        <version>1.0.0-SNAPSHOT</version>
      </plugin>
      ...
    </plugins>
    ...
  </build>
  ...
</project>
```

By default, this will call the single `convert` goal of this plugin, during the [`generate-resources` phase](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference), to which is mapped.



### Custom execution

As any other plugin, you can [customize execution](https://maven.apache.org/guides/mini/guide-configuring-plugins.html#Using_the_executions_Tag) to map it to another [Maven lifecycle's phase](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference),
e.g. mapping `convert` goal to `generate-test-resources` instead:

```xml
<plugin>
  <groupId>io.github.cernier</groupId>
  <artifactId>yml2props-maven-plugin</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <executions>
    <execution>
      <id>convert-yml2props-for-tests</id>
      <phase>generate-test-resources</phase>
      <goals>
        <goal>convert</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```


### Manual execution

As long plugin's `groupId`, `artifactId` and `version` are specified either through `<plugins>` or `<pluginManagement>` POM's sections, its goal can be manually called any time with this shortcut command:

```
mvn yml2props:convert
```

### Configuration

Like for any Maven plugin, `<configuration>` element specifying the [`convert` goal's parameters](convert-mojo.html) can be declared either at whole `<plugin>`'s level or at a specific `<execution>`'s level ; or, in case of manual execution, with `-Dparam=value` command-line parameter(s).

