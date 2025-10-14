package com.uniquindio.automation.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Suite de pruebas de aceptación con Cucumber para el sistema de gestión de usuarios
 * usando servicios reales del entorno Docker Compose.
 * 
 * Esta clase es el punto de entrada principal para ejecutar todas las pruebas de
 * aceptación escritas en Gherkin. Actúa como un "coordinador" que:
 * - Configura el motor de Cucumber
 * - Especifica dónde encontrar los archivos de características (.feature)
 * - Define dónde buscar las implementaciones de pasos (step definitions)
 * - Configura los reportes de resultados
 * - Permite filtrar escenarios por tags
 * 
 * Flujo de ejecución:
 * 1. JUnit 5 ejecuta esta suite
 * 2. Cucumber busca archivos .feature en src/test/resources/features
 * 3. Para cada escenario, busca las implementaciones en los paquetes especificados
 * 4. Ejecuta los pasos usando Spring Boot como contexto
 * 5. Genera reportes en múltiples formatos
 * 
 * Diferencias con CucumberAcceptanceTest:
 * - Usa CucumberRealServicesConfig en lugar de CucumberSpringConfig
 * - Se conecta a servicios reales en lugar de TestContainers
 * - Más rápido y confiable para CI/CD
 * 
 * @author Sistema de Pruebas
 * @version 2.0
 * @since 2024
 */
@Suite  // Marca esta clase como una suite de pruebas de JUnit 5
@IncludeEngines("cucumber")  // Usa el motor de Cucumber para ejecutar las pruebas
@SelectClasspathResource("features")  // Busca archivos .feature en el classpath
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.uniquindio.automation.steps,com.uniquindio.automation.config")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, summary, html:target/cucumber-report.html, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")  // Plugins de reporte
@ConfigurationParameter(key = "cucumber.filter.tags", value = "")  // Filtro de tags (vacío = todos)
public class CucumberRealServicesTest {
    
    // Esta clase no necesita métodos porque:
    // 1. @Suite la convierte en una suite de pruebas
    // 2. @IncludeEngines("cucumber") activa el motor de Cucumber
    // 3. @SelectClasspathResource("features") especifica dónde buscar archivos .feature
    // 4. @ConfigurationParameter configura el comportamiento de Cucumber
    // 5. Los step definitions se encuentran automáticamente por las anotaciones @Dado, @Cuando, @Entonces
}
