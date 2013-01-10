# embedded-jmxtrans-demo


JMX Exporter Demo Web Application.

## Configuration

### Queries

This sample configuration collects a mix metrics

* Application specific queries
 * `classpath:jmxtrans.json` see [src/main/resources/jmxtrans.json](https://github.com/jmxtrans/embedded-jmxtrans-samples/blob/master/embedded-jmxtrans-webapp-coktail/src/main/resources/jmxtrans.json)
* Jmx Exporter internals queries
 * `classpath:org/embedded-jmxtrans/config/jmxtrans-internals.json` provided par embedded-jmxtrans jar. See [embedded-jmxtrans-internals.json](https://github.com/jmxtrans/embedded-jmxtrans/blob/master/src/main/resources/org/embedded-jmxtrans/config/jmxtrans-internals.json)
* [Bundled templates](https://github.com/jmxtrans/embedded-jmxtrans/wiki/Configuration-Templates) for Tomcat and Hotspot JVM
 * `classpath:org/embedded-jmxtrans/config/jvm-sun-hotspot.json` provided par embedded-jmxtrans jar. See [jvm-sun-hotspot.json](https://github.com/jmxtrans/embedded-jmxtrans/blob/master/src/main/resources/org/embedded-jmxtrans/config/jvm-sun-hotspot.json)
 * `classpath:org/embedded-jmxtrans/config/tomcat-6.json` provided par embedded-jmxtrans jar. See [tomcat-6.json](https://github.com/jmxtrans/embedded-jmxtrans/blob/master/src/main/resources/org/embedded-jmxtrans/config/tomcat-6.json)

### Output Writers

This sample application outputs the metrics to 2 writers: [Slf4jWriter](https://github.com/jmxtrans/embedded-jmxtrans/wiki/Slf4j-Writer) and [GraphiteWriter](https://github.com/jmxtrans/embedded-jmxtrans/wiki/Graphite-Writer).


```json
{
  "@class": "org.embedded-jmxtrans.output.Slf4jWriter"
},
{
  "@class": "org.embedded-jmxtrans.output.GraphiteWriter",
  "settings": {
    "host": "${graphite.host:localhost}",
    "port": "${graphite.port:2003}"
  }
}
```

By default, graphite writer connects to a graphite server on localhost:2003. An alternate configuration can be defined using Java system properties (ie "-D" command line parameters) who can be defined in the "catalina.properties" file of the underlying Tomcat server.

## Spring Integration

In [src/main/webapp/WEB-INF/spring-mvc-servlet.xml](https://github.com/jmxtrans/embedded-jmxtrans-samples/blob/master/embedded-jmxtrans-webapp-coktail/src/main/webapp/WEB-INF/spring-mvc-servlet.xml#L45):
```xml
<beans ...
       xmlns:jmxtrans="http://www.jmxtrans.org/schema/embedded"
       xsi:schemaLocation="...
		http://www.jmxtrans.org/schema/embedded http://www.jmxtrans.org/schema/embedded/jmxtrans-1.0.xsd">

   <jmxtrans:jmxtrans id="embedded-jmxtrans" configuration="classpath:jmxtrans.json, ...">

</beans>
```

## Maven Setup

[pom.xml](https://github.com/jmxtrans/embedded-jmxtrans-samples/blob/master/embedded-jmxtrans-webapp-coktail/pom.xml#L114)


```xml
<dependency>
    <groupId>org.jmxtrans.embedded</groupId>
    <artifactId>embedded-jmxtrans</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
**Note:** embedded-jmxtrans is not yet available in Maven Central Repo, please download artifact from [oss.sonatype.org](https://oss.sonatype.org) snapshosts repo .

```xml
<repositories>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```
