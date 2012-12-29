# jmxexporter-demo


JMX Exporter Demo Web Application.

## Configuration

Default jmxexporter file naming convention `classpath:jmxexporter.json` see [src/main/resources/jmxexporter.json](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/src/main/resources/jmxexporter.json)

## Spring Integration

In [src/main/webapp/WEB-INF/spring-mvc-servlet.xml](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/src/main/webapp/WEB-INF/spring-mvc-servlet.xml#L45):
```xml
<bean id="jmxExporter" class="org.jmxexporter.JmxExporterFactory" />
```

## Maven Setup

[pom.xml](https://github.com/cyrille-leclerc/jmxexporter-demo/blob/master/pom.xml#L125)

```xml
<dependency>
    <groupId>org.jmxexporter</groupId>
    <artifactId>jmxexporter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
