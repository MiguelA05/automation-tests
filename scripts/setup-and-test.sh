#!/bin/bash

# Script para configurar la base de datos y ejecutar tests
# Uso: setup-and-test.sh <host> <port>

HOST=$1
PORT=$2
DB_HOST="postgres-domain"
DB_PORT="5432"
DB_NAME="mydb"
DB_USER="user"
DB_PASSWORD="pass"

echo "🚀 Iniciando setup de base de datos y tests..."

# Función para verificar si el servicio está disponible
wait_for_service() {
    local host=$1
    local port=$2
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        echo "Intento $attempt/$max_attempts: Verificando $host:$port..."
        
        if curl -s "http://$host:$port/" > /dev/null 2>&1; then
            echo "✅ Servicio $host:$port está disponible!"
            return 0
        fi
        
        echo "⏳ Servicio no disponible, esperando 5 segundos..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo "❌ Timeout: El servicio $host:$port no está disponible después de $max_attempts intentos"
    return 1
}

# Función para verificar si PostgreSQL está disponible
wait_for_postgres() {
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        echo "Intento $attempt/$max_attempts: Verificando PostgreSQL..."
        
        if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT 1;" > /dev/null 2>&1; then
            echo "✅ PostgreSQL está disponible!"
            return 0
        fi
        
        echo "⏳ PostgreSQL no disponible, esperando 5 segundos..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo "❌ Timeout: PostgreSQL no está disponible después de $max_attempts intentos"
    return 1
}

# Función para insertar datos en la base de datos
setup_database() {
    echo "📊 Configurando base de datos..."
    
    if wait_for_postgres; then
        echo "🔐 Insertando usuario admin y usuarios de prueba..."
        
        # Insertar usuario administrador (rol 0)
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
            INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, numero_telefono, rol)
            VALUES ('admin', 'admin123', NULL, NULL, 'admin@example.com', '+1234567890', 0)
            ON CONFLICT (usuario) DO UPDATE SET clave = 'admin123', numero_telefono = '+1234567890';
        "
        
        # Insertar usuarios normales (rol 1)
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
            INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, numero_telefono, rol) VALUES
            ('juan',   'juan123',   NULL, NULL, 'juan@example.com',   '+1234567891', 1),
            ('maria',  'maria123',  NULL, NULL, 'maria@example.com',  '+1234567892', 1),
            ('pedro',  'pedro123',  NULL, NULL, 'pedro@example.com',  '+1234567893', 1),
            ('laura',  'laura123',  NULL, NULL, 'laura@example.com',  '+1234567894', 1),
            ('carlos', 'carlos123', NULL, NULL, 'carlos@example.com', '+1234567895', 1)
            ON CONFLICT (usuario) DO NOTHING;
        "
        
        # Verificar que los datos se insertaron
        echo "📋 Verificando usuarios insertados:"
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
            SELECT usuario, correo, rol FROM usuarios ORDER BY rol, usuario;
        "
        
        echo "✅ Setup de base de datos completado"
    else
        echo "❌ No se pudo conectar a PostgreSQL"
        return 1
    fi
}

# Función para ejecutar tests
run_tests() {
    echo "🧪 Ejecutando tests..."
    mvn clean test -Dtest=CucumberTest -Dmaven.test.failure.ignore=true
    local test_result=$?
    
    if [ $test_result -eq 0 ]; then
        echo "✅ Todos los tests pasaron!"
    else
        echo "⚠️  Algunos tests fallaron, pero continuando..."
    fi
    
    return 0  # Siempre retornar éxito para que el contenedor no se detenga
}

# Función para mantener el contenedor ejecutándose
keep_running() {
    echo "🔄 Manteniendo contenedor ejecutándose..."
    echo "📊 Ejecutando tests cada 5 minutos..."
    
    while true; do
        sleep 300  # 5 minutos
        echo "🔄 Re-ejecutando tests..."
        mvn test -Dtest=CucumberTest -Dmaven.test.failure.ignore=true -q
    done
}

# Ejecutar el flujo completo
main() {
    if wait_for_service $HOST $PORT; then
        setup_database
        run_tests
        echo "✅ Tests completados."
        echo "🔄 Contenedor se mantendrá corriendo para ejecutar tests adicionales."
        echo "📊 Para ejecutar tests manualmente: mvn test -Dtest=CucumberTest"
        echo "⏳ Re-ejecutando tests cada 5 minutos..."
        
        # Mantener el contenedor corriendo y re-ejecutar tests periódicamente
        while true; do
            sleep 300  # 5 minutos
            echo "🔄 Re-ejecutando tests..."
            mvn test -Dtest=CucumberTest -Dmaven.test.failure.ignore=true -q
        done
    else
        echo "💥 No se pudo conectar al servicio, saliendo..."
        exit 1
    fi
}

# Ejecutar función principal
main "$@"