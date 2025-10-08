# language: es

Característica: Gestión de usuarios (registro, login y administración)

  Antecedentes:
    Dado que el servicio está disponible

  Escenario: Registrar un nuevo usuario
    Cuando registro un usuario con datos válidos
    Entonces la respuesta debe tener estado 201
    Y el cuerpo debe indicar éxito
    Y el cuerpo cumple el esquema "schemas/message_dto.schema.json"

  Escenario: Iniciar sesión con credenciales válidas
    Dado que existe un usuario registrado válido
    Cuando inicio sesión con credenciales correctas
    Entonces la respuesta debe tener estado 200
    Y debo obtener un token JWT válido
    Y el cuerpo cumple el esquema "schemas/token_response.schema.json"

  @admin
  Escenario: Listar usuarios con token de admin
    Dado que inicio sesión como admin
    Cuando consulto la lista de usuarios en la página 0
    Entonces la respuesta debe tener estado 200
    Y el cuerpo debe contener una lista de usuarios
    Y el cuerpo cumple el esquema "schemas/usuarios_page.schema.json"

  @admin
  Escenario: Eliminar un usuario con token de admin
    Dado que inicio sesión como admin
    Y existe un usuario registrado válido
    Cuando elimino ese usuario
    Entonces la respuesta debe tener estado 200

  @admin
  Escenario: Eliminar un usuario sin token
    Dado que no he iniciado sesion
    Y existe un usuario registrado válido
    Cuando elimino ese usuario
    Entonces la respuesta debe tener estado 401

  @admin
  Escenario: Eliminar un usuario con token inválido
    Dado que he inciado sesion como un usuario que no es admin
    Y existe un usuario registrado válido
    Cuando elimino ese usuario
    Entonces la respuesta debe tener estado 403

  @admin
  Escenario: Eliminar un usuario que no existe
    Dado que inicio sesión como admin
    Cuando elimino un usuario que no existe
    Entonces la respuesta debe tener estado 404

  @admin
  Escenario: Listar usuarios sin token
    Dado que no he iniciado sesion
    Cuando consulto la lista de usuarios en la página 0
    Entonces la respuesta debe tener estado 401

  @admin
  Escenario: Listar usuarios con un numero de pagina invalido
    Dado que inicio sesión como admin
    Cuando consulto la lista de usuarios en la página -1
    Entonces la respuesta debe tener estado 400


