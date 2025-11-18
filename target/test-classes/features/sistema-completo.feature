# language: es
# =============================================================================
# PRUEBAS END-TO-END DEL SISTEMA COMPLETO INTEGRADO
# =============================================================================

Característica: Sistema Completo Integrado - Observabilidad y Health Checks
  
  Como desarrollador
  Quiero verificar que todos los componentes del sistema funcionen correctamente integrados
  Para asegurar que la plataforma está operativa y monitoreada

  # ===== VERIFICACIÓN DE HEALTH CHECKS =====
  @sistema-completo
  Escenario: Verificar que todos los microservicios tienen health checks operativos
    Dado que el sistema está desplegado
    Cuando consulto el health check de cada microservicio
    Entonces todos los microservicios deben responder con status "UP"
    Y cada respuesta debe incluir versión y uptime

  # ===== VERIFICACIÓN DE REGISTRO EN MONITOREO =====
  @sistema-completo
  Escenario: Verificar que los microservicios están registrados en el sistema de monitoreo
    Dado que el sistema de monitoreo está disponible
    Cuando consulto el estado global de salud
    Entonces debo ver todos los microservicios registrados
    Y cada servicio debe tener un estado (UP, DOWN, o UNKNOWN)

  # ===== VERIFICACIÓN DE LOGS CENTRALIZADOS =====
  Escenario: Verificar que los logs se están enviando al sistema centralizado
    Dado que un microservicio genera un log
    Cuando consulto el sistema de logs centralizado
    Entonces debo poder encontrar el log del microservicio
    Y el log debe contener información del servicio y nivel

  # ===== VERIFICACIÓN DE NOTIFICACIONES =====
  @sistema-completo
  Escenario: Verificar que las notificaciones se envían cuando un servicio cae
    Dado que un microservicio está registrado en el sistema de monitoreo
    Cuando el microservicio deja de responder
    Entonces el sistema de monitoreo debe detectar el fallo
    Y debe enviar una notificación a los emails configurados

  # ===== VERIFICACIÓN DE INTEGRACIÓN COMPLETA =====
  @sistema-completo
  Escenario: Verificar flujo completo de usuario con observabilidad
    Dado que un usuario se registra en el sistema
    Cuando el usuario realiza operaciones
    Entonces los logs deben registrarse en el sistema centralizado
    Y el sistema de monitoreo debe reportar todos los servicios como UP
    Y los health checks deben mostrar información correcta

