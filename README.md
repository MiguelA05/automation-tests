# Automation Tests

Proyecto de pruebas funcionales/E2E desacoplado del microservicio. Ejecuta escenarios Cucumber contra la API expuesta.

## Requisitos
- Java 21
- Maven 3.9+

## Ejecutar
```bash
mvn clean test -DbaseUrl=http://localhost:8081 -DbasePath=/v1
mvn allure:report
```

Reportes:
- HTML Cucumber: `target/cucumber-report.html`
- Allure: `target/site/allure-maven-plugin/index.html`

## Integración Jenkins
- Define `AUT0_TESTS_REPO_URL` en el job del microservicio.
- El pipeline clonará este repo y publicará Allure HTML.

## Estructura
- `src/test/resources/features`: archivos `.feature`
- `src/test/java/com/uniquindio/automation/steps`: step definitions (HTTP caja negra)
- `src/test/resources/schemas`: JSON Schemas para validación

## Notas
- No depende de clases internas del servicio. Configura la URL vía `-DbaseUrl`.
