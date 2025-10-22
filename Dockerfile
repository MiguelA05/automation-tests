FROM maven:3.9

WORKDIR /app

# Instalar curl y postgresql-client para health checks y conexión a BD
RUN apt-get update && apt-get install -y curl postgresql-client && rm -rf /var/lib/apt/lists/*

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Copiar código fuente
COPY src ./src

# Script para setup de base de datos y tests
COPY setup-and-test.sh /setup-and-test.sh
RUN chmod +x /setup-and-test.sh

# Ejecutar setup y tests contra el servicio domain-service
CMD ["/setup-and-test.sh", "domain-service", "8080"]
