# jmxexporter-demo


JMX Exporter Demo Web Application.

## Configuration

### Queries

This sample configuration collects a mix metrics

* Application specific queries
 * `classpath:jmxexporter.json` see [src/main/resources/jmxexporter.json](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/jmxexporter-webapp-coktail/src/main/resources/jmxexporter.json)
* Jmx Exporter internals queries
 * `classpath:org/jmxexporter/config/jmxexporter-internals.json` provided par jmxexporter jar. See [jmxexporter-internals.json](https://github.com/cyrille-leclerc/jmxexporter/blob/master/src/main/resources/org/jmxexporter/config/jmxexporter-internals.json)
* [Bundled templates](https://github.com/cyrille-leclerc/jmxexporter/wiki/Configuration-Templates) for Tomcat and Hotspot JVM
 * `classpath:org/jmxexporter/config/jvm-sun-hotspot.json` provided par jmxexporter jar. See [jvm-sun-hotspot.json](https://github.com/cyrille-leclerc/jmxexporter/blob/master/src/main/resources/org/jmxexporter/config/jvm-sun-hotspot.json)
 * `classpath:org/jmxexporter/config/tomcat-6.json` provided par jmxexporter jar. See [tomcat-6.json](https://github.com/cyrille-leclerc/jmxexporter/blob/master/src/main/resources/org/jmxexporter/config/tomcat-6.json)

### Output Writers

This sample applicaiton outputs the metrics to 2 writers: [Slf4jWriter](https://github.com/cyrille-leclerc/jmxexporter/wiki/Slf4j-Writer) and [GraphiteWriter](https://github.com/cyrille-leclerc/jmxexporter/wiki/Graphite-Writer).


```json
{
  "@class": "org.jmxexporter.output.Slf4jWriter"
},
{
  "@class": "org.jmxexporter.output.GraphiteWriter",
  "settings": {
    "host": "${graphite.host:localhost}",
    "port": "${graphite.port:2003}"
  }
}
```

By default, graphite writer connects to a graphite server on localhost:2003. An alternate configuration can be defined using Java system properties (ie "-D" command line parameters) who can be defined in the "catalina.properties" file of the underlying Tomcat server.

## Spring Integration

In [src/main/webapp/WEB-INF/spring-mvc-servlet.xml](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/jmxexporter-webapp-coktail/src/main/webapp/WEB-INF/spring-mvc-servlet.xml#L45):
```xml
<bean id="jmxExporter" class="org.jmxexporter.JmxExporterFactory">
  <property name="configurationUrls">
    <list>
      <value>classpath:jmxexporter.json</value>
      <value>classpath:org/jmxexporter/config/jmxexporter-internals.json</value>
      <value>classpath:org/jmxexporter/config/jvm-sun-hotspot.json</value>
      <value>classpath:org/jmxexporter/config/tomcat-6.json</value>
    </list>
  </property>
</bean>
```

## Maven Setup

[pom.xml](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/jmxexporter-webapp-coktail/pom.xml#L114)

```xml
<dependency>
    <groupId>org.jmxexporter</groupId>
    <artifactId>jmxexporter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
