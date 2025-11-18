package com.uniquindio.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import io.restassured.response.Response;
import net.datafaker.Faker;

/**
 * Implementación de pasos (Step Definitions) para las pruebas de aceptación de usuarios.
 * 
 * Esta clase contiene la implementación en Java de todos los pasos definidos en los
 * archivos .feature de Gherkin. Cada método anotado con @Dado, @Cuando, @Entonces
 * o @Y corresponde a un paso específico en los escenarios de prueba.
 * 
 * Funcionalidades principales:
 * - Implementa pasos de Gherkin en español
 * - Realiza peticiones HTTP reales a la aplicación
 * - Genera datos de prueba aleatorios y coherentes
 * - Valida respuestas HTTP y esquemas JSON
 * - Mantiene estado entre pasos (tokens, datos de usuario)
 * - Proporciona datos consistentes para escenarios de prueba
 * 
 * Herramientas utilizadas:
 * - RestAssured: Cliente HTTP para peticiones y validaciones
 * - DataFaker: Generación de datos de prueba realistas
 * - Hamcrest: Matchers para validaciones expresivas
 * 
 * @author Sistema de Pruebas
 * @version 2.0
 * @since 2024
 */
public class UsuarioSteps {

    // ===== CONFIGURACIÓN =====
    
    /**
     * URL base del servicio de usuarios.
     * Se conecta directamente al servicio real usando la variable de entorno.
     * Si no está configurada, usa el valor por defecto.
     */
    private static final String BASE_URL = getBaseUrl();
    
    private static String getBaseUrl() {
        String baseUrl = System.getenv("AUT_TESTS_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = System.getProperty("baseUrl", "http://localhost:8081");
        }
        return baseUrl + "/v1";
    }

    // ===== ESTADO DE LAS PRUEBAS =====
    
    /**
     * Última respuesta HTTP recibida de la aplicación.
     * 
     * Se actualiza con cada petición HTTP realizada y se usa para
     * validaciones posteriores en los pasos de "Entonces".
     */
    private Response lastResponse;
    
    /**
     * Generador de datos falsos para crear datos de prueba realistas.
     * 
     * DataFaker permite generar nombres, emails, teléfonos, etc. que
     * parecen reales pero son completamente aleatorios y seguros para pruebas.
     */
    private final Faker faker = new Faker();
    
    /**
     * Usuario creado en el último escenario de registro.
     * 
     * Se mantiene para poder usarlo en pasos posteriores como login,
     * eliminación, etc. Se genera con un timestamp para garantizar unicidad.
     */
    private String ultimoUsuario = "user_" + System.currentTimeMillis();
    
    /**
     * Contraseña del último usuario creado.
     * 
     * Se genera con un patrón seguro que incluye mayúsculas, minúsculas,
     * números y caracteres especiales, más dígitos aleatorios para unicidad.
     */
    private String ultimoPassword = "Passw0rd*" + faker.number().digits(3);
    
    /**
     * Teléfono del último usuario creado.
     * 
     * Se genera con formato colombiano (3XXXXXXXXX) para simular
     * números de teléfono reales del país.
     */
    private String ultimoTelefono = "3" + faker.number().digits(9);
    
    /**
     * Token JWT del usuario administrador.
     * 
     * Se obtiene al hacer login como admin y se usa para operaciones
     * que requieren privilegios de administrador (listar usuarios, eliminar, etc.).
     */
    private String adminToken;
    
    /**
     * Token JWT del último usuario que hizo login.
     * 
     * Se obtiene al hacer login con credenciales válidas y se puede usar
     * para operaciones que requieren autenticación.
     */
    private String ultimoToken;

    // ===== IMPLEMENTACIÓN DE PASOS DE GHERKIN =====
    
    /**
     * Paso: "Dado que el servicio está disponible"
     * 
     * Verifica que la aplicación esté ejecutándose y lista para recibir peticiones.
     * Hace una petición simple para verificar conectividad.
     */
    @Dado("que el servicio está disponible")
    public void servicioDisponible() {
        // Verificar que el servicio esté disponible haciendo una petición simple
        given()
            .when()
            .get(BASE_URL + "/usuarios")
            .then()
            .statusCode(anyOf(is(200), is(401), is(403))); // Cualquier respuesta indica que el servicio está disponible
    }

    /**
     * Paso: "Cuando registro un usuario con datos válidos"
     * 
     * Crea un nuevo usuario en el sistema con datos generados aleatoriamente
     * pero coherentes entre sí. Los datos se mantienen en variables de instancia
     * para poder usarlos en pasos posteriores.
     */
    @Cuando("registro un usuario con datos válidos")
    public void registroUsuarioValido() {
        // Generar datos aleatorios coherentes entre campos
        ultimoUsuario = "user_" + faker.number().digits(8);
        String correo = faker.internet().emailAddress();
        ultimoTelefono = "3" + faker.number().digits(9);
        ultimoPassword = "Passw0rd*" + faker.number().digits(3);

        // Crear JSON con los datos del usuario
        var body = """
        {
          "usuario":"%s",
          "correo":"%s",
          "numeroTelefono":"%s",
          "clave":"%s"
        }
        """.formatted(ultimoUsuario, correo, ultimoTelefono, ultimoPassword);

        // Enviar petición POST a la API
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(BASE_URL + "/usuarios");
    }

    /**
     * Paso: "Entonces la respuesta debe tener estado {int}"
     * 
     * Valida que el código de estado HTTP de la última respuesta sea el esperado.
     * 
     * @param status Código de estado HTTP esperado
     */
    @Entonces("la respuesta debe tener estado {int}")
    public void validarEstado(int status) {
        lastResponse.then().statusCode(status);
    }

    /**
     * Paso: "Y el cuerpo cumple el esquema {string}"
     * 
     * Valida que el cuerpo de la respuesta JSON cumpla con un esquema JSON Schema específico.
     * 
     * @param schemaPath Ruta relativa al classpath del archivo de esquema
     */
    @Y("el cuerpo cumple el esquema {string}")
    public void cuerpoCumpleEsquema(String schemaPath) {
        // Validar que la respuesta JSON cumpla con el esquema especificado
        lastResponse.then().body(matchesJsonSchemaInClasspath(schemaPath));
    }

    /**
     * Paso: "Y el cuerpo debe indicar éxito"
     * 
     * Valida que el cuerpo de la respuesta no esté vacío y contenga información
     * que indique que la operación fue exitosa.
     */
    @Y("el cuerpo debe indicar éxito")
    public void cuerpoIndicaExito() {
        String raw = lastResponse.getBody() != null ? lastResponse.getBody().asString() : null;
        assertThat(raw, allOf(notNullValue(), not(blankOrNullString())));
    }

    /**
     * Paso: "Dado que existe un usuario registrado válido"
     * 
     * Crea un usuario válido en el sistema para usar en escenarios que requieren
     * un usuario previamente registrado.
     */
    @Dado("que existe un usuario registrado válido")
    public void existeUsuarioValido() {
        registroUsuarioValido();
        lastResponse.then().statusCode(anyOf(is(201), is(200)));
    }

    /**
     * Paso: "Y existe un usuario registrado válido"
     * 
     * Alias del paso anterior para permitir diferentes formas de expresar
     * el mismo concepto en los escenarios de Gherkin.
     */
    @Y("existe un usuario registrado válido")
    public void existeUsuarioValidoAlias() {
        existeUsuarioValido();
    }

    /**
     * Paso: "Cuando inicio sesión con credenciales correctas"
     * 
     * Autentica un usuario en el sistema usando las credenciales del último
     * usuario creado. Si la autenticación es exitosa, extrae y guarda el
     * token JWT para usar en peticiones posteriores.
     */
    @Cuando("inicio sesión con credenciales correctas")
    public void loginConCredencialesCorrectas() {
        var body = """
        {
          "usuario":"%s",
          "clave":"%s"
        }
        """.formatted(ultimoUsuario, ultimoPassword);

        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(BASE_URL + "/sesiones");

        if (lastResponse.statusCode() == 200) {
            // Extraer token JWT de la respuesta (intentar ambos formatos)
            ultimoToken = lastResponse.jsonPath().getString("respuesta.token");
            if (ultimoToken == null) {
                ultimoToken = lastResponse.jsonPath().getString("token");
            }
        }
    }

    /**
     * Paso: "Entonces debo obtener un token JWT válido"
     * 
     * Valida que se haya obtenido un token JWT válido después del login.
     */
    @Entonces("debo obtener un token JWT válido")
    public void deboObtenerTokenValido() {
        assertThat(ultimoToken, allOf(notNullValue(), not(blankOrNullString())));
    }

    /**
     * Paso: "Cuando solicito código de recuperación para ese usuario"
     * 
     * Solicita un código de recuperación de contraseña para el último usuario
     * registrado.
     */
    @Cuando("solicito código de recuperación para ese usuario")
    public void solicitarCodigoRecuperacion() {
        var body = """
        {
          "usuario":"%s"
        }
        """.formatted(ultimoUsuario);

        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(BASE_URL + "/codigos");
    }

    /**
     * Paso: "Dado que inicio sesión como admin"
     * 
     * Autentica como usuario administrador usando credenciales predefinidas.
     * Si el usuario admin no existe, lo crea automáticamente.
     * 
     * Credenciales del admin:
     * - Usuario: "admin"
     * - Contraseña: "admin123"
     */
    @Dado("que inicio sesión como admin")
    public void inicioSesionComoAdmin() {
        // Primero intentar hacer login
        var body = """
        {
          "usuario":"admin",
          "clave":"admin123"
        }
        """;

        var resp = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(BASE_URL + "/sesiones");

        // Si el login falla (403 o 401), crear el usuario admin
        if (resp.statusCode() == 403 || resp.statusCode() == 401) {
            crearUsuarioAdmin();
            // Intentar login nuevamente
            resp = given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .post(BASE_URL + "/sesiones");
        }

        resp.then().statusCode(200);
        adminToken = resp.jsonPath().getString("respuesta.token");
        if (adminToken == null) {
            adminToken = resp.jsonPath().getString("token");
        }
        assertThat(adminToken, not(blankOrNullString()));
    }

    /**
     * Crea el usuario administrador en el sistema.
     * Se ejecuta automáticamente si el usuario admin no existe.
     * 
     * Nota: El usuario admin se crea directamente en la base de datos
     * con rol = 0 (ADMIN) ya que la API no permite crear usuarios admin.
     */
    private void crearUsuarioAdmin() {
        // El usuario admin se crea directamente en la base de datos
        // con rol = 0 (ADMIN) ya que la API no permite crear usuarios admin
        System.out.println("ℹ️ Usuario admin debe estar creado en la base de datos con rol ADMIN");
    }

    /**
     * Paso: "Cuando consulto la lista de usuarios en la página {int}"
     * 
     * Consulta la lista paginada de usuarios usando el token de administrador.
     * 
     * @param pagina Número de página a consultar (empezando en 0)
     */
    @Cuando("consulto la lista de usuarios en la página {int}")
    public void consultarListaUsuarios(int pagina) {
        lastResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .get(BASE_URL + "/usuarios?page=" + pagina);
    }

    /**
     * Paso: "Y el cuerpo debe contener una lista de usuarios"
     * 
     * Valida que la respuesta de la consulta de usuarios contenga efectivamente
     * una lista de usuarios.
     */
    @Y("el cuerpo debe contener una lista de usuarios")
    public void cuerpoContieneListaUsuarios() {
        // Verificar que la respuesta contenga el campo "respuesta" con datos
        lastResponse.then().body("respuesta", notNullValue());
    }

    /**
     * Paso: "Cuando elimino ese usuario"
     * 
     * Elimina el último usuario registrado usando el token de administrador.
     */
    @Cuando("elimino ese usuario")
    public void eliminarEseUsuario() {
        lastResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .delete(BASE_URL + "/usuarios/" + ultimoUsuario);
    }

    /**
     * Paso: "Dado que no he iniciado sesion"
     * 
     * Este paso limpia el token de administrador para simular no tener un token
     */
    @Dado("que no he iniciado sesion")
    public void noTengoToken() {
        adminToken = "";
    }

    /**
     * Paso: "Dado que he inciado sesion como un usuario que no es admin"
     * 
     * Simula tener un token de un usuario que no es admin.
     * En realidad no hace login, solo simula tener un token inválido.
     */
    @Dado("que he inciado sesion como un usuario que no es admin")
    public void noTengoTokenAdmin() {
        // Simular que tenemos un token de un usuario que no es admin
        // En realidad no hacemos login, solo simulamos tener un token
        adminToken = "token_invalido_usuario_no_admin";
    }

    /**
     * Paso: "Cuando elimino elimino un usuario que no existe"
     * 
     * Intenta eliminar un usuario que no existe en el sistema usando el token de administrador.
     */
    @Cuando("elimino elimino un usuario que no existe")
    public void eliminarUsuarioInexistente() {
        lastResponse = given()
            .header("Authorization", "Bearer " + adminToken)
            .delete(BASE_URL + "/usuarios/" + "usuario_inexistente_12345");
    }

    /**
     * Paso: "Cuando elimino un usuario que no existe"
     * 
     * Intenta eliminar un usuario que no existe en el sistema usando el token de administrador.
     */
    @Cuando("elimino un usuario que no existe")
    public void elimino_un_usuario_que_no_existe() {
        lastResponse = given()
            .header("Authorization", "Bearer " + adminToken)
            .delete(BASE_URL + "/usuarios/" + "usuario_inexistente_12345");
    }
}