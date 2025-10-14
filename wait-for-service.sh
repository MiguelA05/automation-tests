#!/bin/bash

# Script para esperar a que un servicio esté disponible antes de ejecutar comandos
# Uso: wait-for-service.sh <host> <port> <command>

HOST=$1
PORT=$2
shift 2
COMMAND="$@"

echo "Esperando a que $HOST:$PORT esté disponible..."

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

# Esperar a que el servicio esté disponible
if wait_for_service $HOST $PORT; then
    echo "🚀 Ejecutando comando: $COMMAND"
    exec $COMMAND
else
    echo "💥 No se pudo conectar al servicio, saliendo..."
    exit 1
fi
