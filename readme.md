# Similar Products API

Esta aplicación implementa una API REST que proporciona productos similares a uno dado, siguiendo los requerimientos de la prueba técnica.

## Descripción

La aplicación expone un endpoint REST que, dado un ID de producto, devuelve una lista detallada de productos similares a él. Para lograr esto, la aplicación se comunica con dos APIs externas:
- Una API para obtener los IDs de productos similares
- Una API para obtener los detalles de cada producto

## Arquitectura

La aplicación se basa en una arquitectura de microservicios y utiliza Spring Boot como framework principal. Está organizada en las siguientes capas:

1. **Controlador**: Maneja las solicitudes HTTP entrantes y devuelve las respuestas.
2. **Servicio**: Implementa la lógica de negocio, orquestando las llamadas a las APIs externas.
3. **Cliente API**: Encapsula la comunicación con las APIs externas.
4. **Modelos (DTOs)**: Representan las estructuras de datos utilizadas por la aplicación.

## Tecnologías utilizadas

- **Spring Boot 3.4.5**: Framework para desarrollo de aplicaciones.
- **Spring WebFlux**: Proporciona un modelo de programación reactiva para operaciones asíncronas.
- **Resilience4j**: Implementación de patrones de resiliencia como circuit breaker.
- **WebClient**: Cliente HTTP reactivo para comunicaciones asíncronas.

## Funcionalidades implementadas

- **Endpoint GET /product/{productId}/similar**: Devuelve una lista de productos similares con sus detalles.
- **Circuit Breaker**: Manejo de fallos en las APIs externas para mejorar la resiliencia.
- **Manejo de errores**: Incluye gestión de timeout y manejo de excepciones.

## Cómo ejecutar la aplicación

### Requisitos previos

- Java 24
- Docker y Docker Compose
- Maven

### Pasos para ejecutar

1. Arrancar los mocks y la infraestructura necesaria:
   ```
   docker-compose up -d simulado influxdb grafana
   ```

2. Ejecutar la aplicación Spring Boot:
   ```
   mvn spring-boot:run
   ```
**Nota para usuarios de Windows**: Si encuentras errores relacionados con la codificación de archivos o MalformedInputException, utiliza el siguiente comando alternativo: 
   ```
   mvnw.cmd spring-boot:run -Dmaven.resources.skip=true
   ```

3. La aplicación estará disponible en: `http://localhost:5000`

### Ejemplos de uso

Obtener productos similares para el producto con ID 1:
```
curl http://localhost:5000/product/1/similar
```

Respuesta esperada:
```json
[
  {
    "id": "2",
    "name": "Dress",
    "price": 19.99,
    "availability": true
  },
  {
    "id": "4",
    "name": "Boots",
    "price": 39.99,
    "availability": true
  },
  {
    "id": "3",
    "name": "Blazer",
    "price": 29.99,
    "availability": false
  }
]
```

## Pruebas de rendimiento

Para ejecutar las pruebas de rendimiento:

```
docker-compose run --rm k6 run scripts/test.js
```

Los resultados se pueden visualizar en Grafana:
```
http://localhost:3000/d/Le2Ku9NMk/k6-performance-test
```

### Resultados de rendimiento

En las pruebas de carga, la aplicación mostró los siguientes resultados:

- **Peticiones procesadas**: ~10,000 peticiones
- **Tasa de solicitudes**: ~123 solicitudes/segundo
- **Tiempo medio de respuesta**: ~913ms
- **Tiempo mediano de respuesta**: ~105ms
- **90% de las peticiones**: < 320ms
- **Usuarios concurrentes**: Hasta 200

## Posibles mejoras

- **Implementación de caché**: Para reducir el número de llamadas a las APIs externas.
- **Ajuste fino de los timeouts**: Para mejorar la gestión de conexiones lentas.
- **Optimización de llamadas paralelas**: Para reducir los tiempos de respuesta.
- **Monitorización avanzada**: Para identificar cuellos de botella.
- **Implementación de pruebas unitarias y de integración**: Para garantizar la calidad del código.

## Estructura del proyecto

```
src/main/java/com/RubenBorque/prueba/
├── PruebaApplication.java         # Clase principal
├── controller/
│   └── SimilarProductsController.java  # API REST
├── service/
│   └── SimilarProductsService.java     # Lógica de negocio
├── client/
│   └── ProductApiClient.java          # Cliente para APIs externas
└── model/
    └── ProductDetailDTO.java          # Modelo de datos
```

## Configuración

Las principales configuraciones se encuentran en el archivo `application.properties`:

- Puerto del servidor: 5000
- URL base para las APIs externas: http://localhost:3001
- Rutas de las APIs externas
- Configuraciones de circuit breaker y resilience
- Timeouts y conexiones máximas

## Evaluación

- **Claridad y mantenibilidad del código**: Se ha organizado el código en capas bien definidas con responsabilidades claras.
- **Rendimiento**: La aplicación maneja correctamente la carga de trabajo, aunque hay oportunidades de mejora.
- **Resiliencia**: Se han implementado patrones como circuit breaker para manejar fallos en las APIs externas.