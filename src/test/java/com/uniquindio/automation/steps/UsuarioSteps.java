package com.uniquindio.automation.steps;

import io.cucumber.java.es.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.hamcrest.MatcherAssert;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class UsuarioSteps {

    private final Faker faker = new Faker();
    private Response lastResponse;
    private String ultimoUsuario = "user_" + System.currentTimeMillis();
    private String ultimoPassword = "Passw0rd*" + faker.number().digits(3);
    private String ultimoTelefono = "3" + faker.number().digits(9);
    private String adminToken;
    private String ultimoToken;

    private String baseUrl() {
        String url = System.getProperty("baseUrl", "http://localhost:8081");
        String basePath = System.getProperty("basePath", "/v1");
        return url + basePath;
    }

    @Dado("que el servicio está disponible")
    public void servicioDisponible() { /* no-op */ }

    @Cuando("registro un usuario con datos válidos")
    public void registroUsuarioValido() {
        ultimoUsuario = "user_" + faker.number().digits(8);
        String correo = faker.internet().emailAddress();
        ultimoTelefono = "3" + faker.number().digits(9);
        ultimoPassword = "Passw0rd*" + faker.number().digits(3);

        var body = """
        {
          "usuario":"%s",
          "correo":"%s",
          "numeroTelefono":"%s",
          "clave":"%s",
          "nombres":"%s",
          "apellidos":"%s"
        }
        """.formatted(ultimoUsuario, correo, ultimoTelefono, ultimoPassword,
                faker.name().firstName(), faker.name().lastName());

        lastResponse = given().contentType(ContentType.JSON).body(body).post(baseUrl() + "/usuarios");
    }

    @Entonces("la respuesta debe tener estado {int}")
    public void validarEstado(int status) { lastResponse.then().statusCode(status); }

    @Y("el cuerpo cumple el esquema {string}")
    public void cuerpoCumpleEsquema(String schemaPath) { lastResponse.then().body(matchesJsonSchemaInClasspath(schemaPath)); }

    @Y("el cuerpo debe indicar éxito")
    public void cuerpoIndicaExito() {
        String raw = lastResponse.getBody() != null ? lastResponse.getBody().asString() : null;
        MatcherAssert.assertThat(raw, allOf(notNullValue(), not(blankOrNullString())));
    }

    @Dado("que existe un usuario registrado válido")
    public void existeUsuarioValido() {
        registroUsuarioValido();
        lastResponse.then().statusCode(anyOf(is(201), is(200)));
    }

    @Y("existe un usuario registrado válido")
    public void existeUsuarioValidoAlias() { existeUsuarioValido(); }

    @Cuando("inicio sesión con credenciales correctas")
    public void loginConCredencialesCorrectas() {
        var body = """
        {
          "usuario":"%s",
          "clave":"%s"
        }
        """.formatted(ultimoUsuario, ultimoPassword);

        lastResponse = given().contentType(ContentType.JSON).body(body).post(baseUrl() + "/sesiones");

        if (lastResponse.statusCode() == 200) {
            ultimoToken = lastResponse.jsonPath().getString("respuesta.token");
            if (ultimoToken == null) {
                ultimoToken = lastResponse.jsonPath().getString("token");
            }
        }
    }

    @Entonces("debo obtener un token JWT válido")
    public void deboObtenerTokenValido() { MatcherAssert.assertThat(ultimoToken, allOf(notNullValue(), not(blankOrNullString()))); }

    @Cuando("solicito código de recuperación para ese usuario")
    public void solicitarCodigoRecuperacion() {
        var body = """
        {
          "usuario":"%s"
        }
        """.formatted(ultimoUsuario);

        lastResponse = given().contentType(ContentType.JSON).body(body).post(baseUrl() + "/codigos");
    }

    @Dado("que inicio sesión como admin")
    public void inicioSesionComoAdmin() {
        var body = """
        {
          "usuario":"admin",
          "clave":"admin123"
        }
        """;
        var resp = given().contentType(ContentType.JSON).body(body).post(baseUrl() + "/sesiones");
        resp.then().statusCode(200);
        adminToken = resp.jsonPath().getString("respuesta.token");
        if (adminToken == null) { adminToken = resp.jsonPath().getString("token"); }
        MatcherAssert.assertThat(adminToken, not(blankOrNullString()));
    }

    @Cuando("consulto la lista de usuarios en la página {int}")
    public void consultarListaUsuarios(int pagina) {
        lastResponse = given().header("Authorization", "Bearer " + adminToken).get(baseUrl() + "/usuarios?page=" + pagina);
    }

    @Y("el cuerpo debe contener una lista de usuarios")
    public void cuerpoContieneListaUsuarios() { lastResponse.then().body("respuesta", notNullValue()); }

    @Cuando("elimino ese usuario")
    public void eliminarEseUsuario() {
        lastResponse = given().header("Authorization", "Bearer " + adminToken).delete(baseUrl() + "/usuarios/" + ultimoUsuario);
    }

    @Dado("que no he iniciado sesion")
    public void noTengoToken() { adminToken = ""; }

    @Dado("que he inciado sesion como un usuario que no es admin")
    public void noTengoTokenAdmin() {
        var body = """
        {
          "usuario":"%s",
          "clave":"%s"
        }
        """.formatted(ultimoUsuario, ultimoPassword);
        var resp = given().contentType(ContentType.JSON).body(body).post(baseUrl() + "/sesiones");
        resp.then().statusCode(200);
        adminToken = resp.jsonPath().getString("respuesta.token");
        if (adminToken == null) { adminToken = resp.jsonPath().getString("token"); }
        MatcherAssert.assertThat(adminToken, not(blankOrNullString()));
    }

    @Cuando("elimino un usuario que no existe")
    public void eliminarUsuarioInexistente() {
        lastResponse = given().header("Authorization", "Bearer " + adminToken).delete(baseUrl() + "/usuarios/" + "usuario_inexistente_12345");
    }
}


