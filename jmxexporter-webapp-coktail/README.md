# jmxexporter-demo


JMX Exporter Demo Web Application.

## Configuration

Default configuration files are used :
* 
* `classpath:jmxexporter.json` see [src/main/resources/jmxexporter.json](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/jmxexporter-webapp-coktail/src/main/resources/jmxexporter.json)
* `classpath:org/jmxexporter/jmxexporter-internals.json` provided par jmxexporter jar. See [jmxexporter-internals.json](https://github.com/cyrille-leclerc/jmxexporter/blob/master/src/main/resources/org/jmxexporter/jmxexporter-internals.json)

## Spring Integration

In [src/main/webapp/WEB-INF/spring-mvc-servlet.xml](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/jmxexporter-webapp-coktail/src/main/webapp/WEB-INF/spring-mvc-servlet.xml#L45):
```xml
<bean id="jmxExporter" class="org.jmxexporter.JmxExporterFactory" />
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
