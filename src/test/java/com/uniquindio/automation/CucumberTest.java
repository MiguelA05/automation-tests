package com.uniquindio.automation;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Suite principal de pruebas de aceptación con Cucumber para el sistema completo.
 * 
 * Esta clase es el punto de entrada principal para ejecutar todas las pruebas de
 * aceptación escritas en Gherkin usando servicios reales del entorno Docker Compose.
 * 
 * @author Sistema de Pruebas
 * @version 2.0
 * @since 2024
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.uniquindio.automation.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, 
        value = "pretty, summary, html:target/cucumber-report.html, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "")
public class CucumberTest {}


