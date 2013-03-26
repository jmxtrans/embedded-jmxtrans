#!/usr/bin/env groovy
println 'GENERATE DASHBOARD FILE'

import groovy.json.JsonSlurper
import groovy.text.GStringTemplateEngine

def outputFile, infrastructureFile, dashboardTemplateFile, version // , logger

// gmaven injects a $project variable
if (binding.variables.containsKey("project")) {
    logger = log
    logger.debug("MAVEN: binding $binding.variables")
    outputDir = "$project.build.directory/dashboard/"

    infrastructureFile = new FileReader(project.properties.infrastructureFile)
    dashboardTemplateFile = new FileReader(project.properties.dashboardTemplateFile)
    outputFile = new File(outputDir, project.properties.generatedDashboardFile)

    version = project.version
} else {
    logger = new SysoutLogger()

    logger.debug("STANDALONE: $binding.variables")

    // standalone - debug mode
    outputDir = "../../../target/dashboard"
    infrastructureFile = new FileReader("../../../etc/prod/cocktail-app-infrastructure-prod.json")
    dashboardTemplateFile = new FileReader("../../../../embedded-jmxtrans-webapp-coktail/src/main/graphite/dashboard.template.md")
    outputFile = new File(outputDir, "Dashboard-PROD.md")
    version = "UNDEFINED"
}

logger.debug("Base folder: " + new File(".").getAbsolutePath())

def slurper = new JsonSlurper()

def infrastructure = slurper.parse(infrastructureFile)

// overwrite version
infrastructure["version"] = version

logger.debug("infrastructure: $infrastructure")

def graphiteBaseUrl = infrastructure.graphite.baseUrl

new File(outputDir).mkdirs()

def engine = new GStringTemplateEngine()

def template = engine.createTemplate(dashboardTemplateFile).make(infrastructure)
// println template.toString()

outputFile.withWriter('utf-8') { w ->
    w << template.toString()
}

logger.info("Generated $outputFile.name")
logger.debug("Generated $outputFile.canonicalPath")

class SysoutLogger {
    def debug(msg) {
        println("DEBUG: $msg")
    }

    def info(msg) {
        println("INFO $msg")
    }
}