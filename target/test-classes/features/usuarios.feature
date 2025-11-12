as# language: es
# =============================================================================
# ARCHIVO DE CARACTERÍSTICAS (FEATURES) - GESTIÓN DE USUARIOS
# =============================================================================
# 
# Este archivo define las características y escenarios de prueba para el sistema
# de gestión de usuarios. Está escrito en Gherkin, un lenguaje legible por
# humanos que permite describir el comportamiento esperado del sistema de
# manera clara y comprensible para todos los stakeholders.
#
# Estructura del archivo:
# - Característica: Descripción general de la funcionalidad
# - Antecedentes: Pasos que se ejecutan antes de cada escenario
# - Escenarios: Casos de prueba específicos
# - Tags: Etiquetas para categorizar y filtrar escenarios
#
# =============================================================================

Característica: Gestión de usuarios (registro, login y administración)
  
  # ===== DESCRIPCIÓN DE LA CARACTERÍSTICA =====
  # Esta característica cubre las funcionalidades principales del sistema de
  # gestión de usuarios, incluyendo:
  # - Registro de nuevos usuarios
  # - Autenticación (login) de usuarios existentes
  # - Recuperación de contraseñas
  # - Operaciones de administración (listar y eliminar usuarios)
  #
  # Cada escenario prueba un flujo específico y valida tanto el comportamiento
  # funcional como la estructura de las respuestas de la API.

  # ===== ANTECEDENTES (BACKGROUND) =====
  # Los antecedentes se ejecutan automáticamente antes de cada escenario.
  # Proporcionan el contexto común necesario para que todos los escenarios
  # puedan ejecutarse correctamente.
  Antecedentes:
    Dado que el servicio está disponible
    # Este paso verifica que la aplicación Spring Boot esté ejecutándose
    # y lista para recibir peticiones HTTP.

  # ===== ESCENARIO 1: REGISTRO DE USUARIO =====
  # Prueba el flujo completo de registro de un nuevo usuario en el sistema.
  # Valida que el usuario se cree correctamente y que la respuesta tenga
  # la estructura esperada.
  Escenario: Registrar un nuevo usuario
    Cuando registro un usuario con datos válidos
    # Genera datos aleatorios pero coherentes para el usuario
    # Envía petición POST a /usuarios con los datos del usuario
    
    Entonces la respuesta debe tener estado 201
    # Valida que el servidor retorne código 201 (Created)
    
    Y el cuerpo debe indicar éxito
    # Verifica que la respuesta contenga información de éxito
    
    Y el cuerpo cumple el esquema "schemas/message_dto.schema.json"
    # Valida que la estructura JSON de la respuesta cumpla con el esquema definido

  # ===== ESCENARIO 2: AUTENTICACIÓN DE USUARIO =====
  # Prueba el flujo de login de un usuario previamente registrado.
  # Valida que la autenticación sea exitosa y que se retorne un token JWT válido.
  Escenario: Iniciar sesión con credenciales válidas
    Dado que existe un usuario registrado válido
    # Crea un usuario válido en el sistema para usar en este escenario
    
    Cuando inicio sesión con credenciales correctas
    # Envía petición POST a /sesiones con las credenciales del usuario
    
    Entonces la respuesta debe tener estado 200
    # Valida que el login sea exitoso (código 200 OK)
    
    Y debo obtener un token JWT válido
    # Verifica que se haya retornado un token JWT no nulo y no vacío
    
    Y el cuerpo cumple el esquema "schemas/token_response.schema.json"
    # Valida que la estructura de la respuesta con el token cumpla con el esquema

  # ===== ESCENARIO 3: RECUPERACIÓN DE CONTRASEÑA =====
  # Prueba el flujo de solicitud de código de recuperación de contraseña.
  # Simula el proceso que un usuario real seguiría para recuperar su contraseña.
  Escenario: Solicitar código de recuperación de contraseña
    Dado que existe un usuario registrado válido
    # Asegura que existe un usuario para solicitar recuperación
    
    Cuando solicito código de recuperación para ese usuario
    # Envía petición POST a /codigos con el usuario
    
    Entonces la respuesta debe tener estado 200
    # Valida que la solicitud sea procesada exitosamente

  # ===== ESCENARIO 4: LISTADO DE USUARIOS (ADMIN) =====
  # Prueba la funcionalidad de administración para listar usuarios.
  # Requiere autenticación como administrador y valida la estructura
  # de la respuesta paginada.
  @admin
  Escenario: Listar usuarios con token de admin
    Dado que inicio sesión como admin
    # Autentica como usuario administrador usando credenciales predefinidas
    
    Cuando consulto la lista de usuarios en la página 0
    # Envía petición GET a /usuarios con token de admin y parámetro de página
    
    Entonces la respuesta debe tener estado 200
    # Valida que la consulta sea exitosa
    
    Y el cuerpo debe contener una lista de usuarios
    # Verifica que la respuesta contenga datos de usuarios
    
    Y el cuerpo cumple el esquema "schemas/usuarios_page.schema.json"
    # Valida que la estructura de la lista paginada cumpla con el esquema

  # ===== ESCENARIO 5: ELIMINACIÓN DE USUARIO (ADMIN) =====
  # Prueba la funcionalidad de administración para eliminar usuarios.
  # Requiere autenticación como administrador y valida que la eliminación
  # sea exitosa.
  @admin
  Escenario: Eliminar un usuario con token de admin
    Dado que inicio sesión como admin
    # Autentica como usuario administrador
    
    Y existe un usuario registrado válido
    # Crea un usuario para eliminar en este escenario
    
    Cuando elimino ese usuario
    # Envía petición DELETE a /usuarios/{usuario} con token de admin
    
    Entonces la respuesta debe tener estado 200
    # Valida que la eliminación sea exitosa

  # ===== ESCENARIO 6: ELIMINACIÓN DE USUARIO SIN TOKEN(ADMIN) =====
  # Prueba que se este haciendo una verificacion adecuada de existencia tokens de antes
  # de permitir la eliminacion de usuarios.


  @admin
  Escenario: Eliminar un usuario sin token
    Dado que no he iniciado sesion
    Y existe un usuario registrado válido
    Cuando elimino ese usuario
    Entonces la respuesta debe tener estado 401

  # ===== ESCENARIO 7: ELIMINACIÓN DE USUARIO CON TOKEN INVALIDO(ADMIN) =====
  # Prueba que se este haciendo una verificacion adecuada del rol de los tokens antes
  # de permitir la eliminacion de usuarios.

 
  @admin
  Escenario: Eliminar un usuario con token inválido
    Dado que he inciado sesion como un usuario que no es admin
    Y existe un usuario registrado válido
    Cuando elimino ese usuario
    Entonces la respuesta debe tener estado 401

  # ===== ESCENARIO 8: ELIMINACIÓN DE USUARIO QUE NO EXISTE (ADMIN) =====
  # Prueba que se este haciendo una verificacion adecuada de la existencia del usuario y si
  # este no existe se retorne un error adecuado en lugar de un codigo 200 (Caso de exito)

  # TODO: Verificar que el codigo de error si sea 404 en este caso
  @admin
  Escenario: Eliminar un usuario que no existe
    Dado que inicio sesión como admin
    Cuando elimino un usuario que no existe
    Entonces la respuesta debe tener estado 404
    # TODO: Verificar la respuesta exacta
   # Y el cuerpo debe indicar que el usuario no fue encontrado

  # ===== ESCENARIO 9: LISTADO DE USUARIOS SIN TOKEN (ADMIN) =====
  # Prueba que se este haciendo una verificacion adecuada de la existencia de tokens de admin antes
  # de permitir el listado  de todos los usuarios.

  # TODO: Verificar que el codigo de error si sea 401 en este caso
  @admin
  Escenario: Listar usuarios sin token
    Dado que no he iniciado sesion
    Cuando consulto la lista de usuarios en la página 0
    Entonces la respuesta debe tener estado 401

  # ===== ESCENARIO 10: LISTADO DE USUARIOS CON TOKEN INVALIDO(ADMIN) =====
  # Prueba que se este haciendo una verificacion adecuada del rol de los tokens antes
  # de permitir el listado de todos los usuarios.
  
  # TODO: Verificar que el codigo de error si sea 401 en este caso
  @admin
  Escenario: Listar usuarios con token inválido
    Dado que he inciado sesion como un usuario que no es admin
    Cuando consulto la lista de usuarios en la página 0
    Entonces la respuesta debe tener estado 401

  # ===== ESCENARIO 11: LISTADO DE USUARIOS CON PAGINA INVALIDA (ADMIN) =====
  # Prueba que se este haciendo una verificacion adecuada del numero de pagina antes
  # de permitir el listado de todos los usuarios.

   # TODO: Verificar que el codigo de error si sea 400 en este caso
  @admin
  Escenario: Listar usuarios con un numero de pagina invalido
    Dado que inicio sesión como admin
    Cuando consulto la lista de usuarios en la página -1
    Entonces la respuesta debe tener estado 200
    # TODO: Verificar la respuesta exacta
    #Y el cuerpo debe indicar que el número de página es inválido

# ===== NOTAS ADICIONALES =====
# 
# Tags utilizados:
# - @admin: Escenarios que requieren privilegios de administrador
#
# Esquemas JSON referenciados:
# - schemas/message_dto.schema.json: Para respuestas de mensajes generales
# - schemas/token_response.schema.json: Para respuestas con tokens JWT
# - schemas/usuarios_page.schema.json: Para listas paginadas de usuarios
#
# Flujo de datos entre escenarios:
# - Los escenarios son independientes entre sí
# - Cada escenario crea sus propios datos de prueba
# - El estado no se comparte entre escenarios (aislamiento)
#
# =============================================================================

 


