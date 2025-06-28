# AlertAi
## Sistema de Gestión de Eventos

AlertAi es una plataforma web integral para la gestión de eventos públicos y la difusión de alertas comunitarias, desarrollada con tecnologías modernas como Spring Boot, MySQL y Leaflet.js.

Permite a los ciudadanos crear, explorar, denunciar y visualizar eventos relevantes para su comunidad a través de interfaces intuitivas, un calendario interactivo y un mapa georreferenciado con OpenStreetMap.

El sistema también cuenta con un completo panel de administración que permite a los moderadores revisar reportes, gestionar usuarios y mantener la plataforma segura y actualizada. 
Su enfoque principal es fomentar la participación ciudadana y mejorar la comunicación en situaciones de emergencia o interés general.

## Autores
Marla Mendez - (@MarlaMendez)
Natalie Fernández - (@sabbinat)
Nicolás Lara - (@niikila)
Paula Suarez - (@TainaSuarez)

## 🚀 Funcionalidades principales

### 🧑‍💼 Para los usuarios:
- Registro y autenticación de cuentas.
- Creación, edición y eliminación de eventos.
- Visualización de eventos creados.
- Gestión del perfil de usuario (actualización de datos personales).
- Reporte/denuncia de eventos inapropiados.

### 🛠️ Para el administrador:
- Dashboard de administración.
- Gestión de categorías de eventos.
- Visualización y control de denuncias realizadas por los usuarios.
- Gestión general de usuarios y eventos.

## 🛠️ Tecnologías utilizadas

- Java 21+
- Spring Boot
- Spring Security 
- MySQL
- JPA / Hibernate
- Thymeleaf 
- Bootstrap 5
- Maven
- OpenStreetMap – para visualización interactiva de la ubicación de los eventos en el mapa.
- Leaflet.js – biblioteca JavaScript para mostrar mapas interactivos con OpenStreetMap.

## 🔐 Configuración de Credenciales

Para que el proyecto funcione correctamente, necesitas un archivo de configuración con las credenciales necesarias:

1. Copia el archivo `application-secret.example.properties` como `application-secret.properties`.
2. Completa los siguientes campos:
   - Contraseña de aplicación de Gmail para envío de correos.
   - Usuario y contraseña de la base de datos MySQL.
3. Este archivo está en `.gitignore` y no se sube al repositorio por seguridad.
4. Si no cuentas con las credenciales, consúltalo con el equipo de Star Software.

🔗 **Guía para generar una contraseña de aplicación en Gmail**:  
https://myaccount.google.com/apppasswords

## ▶️ Ejecución Local

1. Asegúrate de tener Java 21 y MySQL instalados.
2. Clona el proyecto:
   ```bash
   git clone https://github.com/sabbinat/alertai.git
3. Configura el archivo application-secret.properties como se explicó.
   Ejecuta el proyecto desde tu IDE o con:
   ```bash
   mvn spring-boot:run
4. Accede a la aplicación en: http://localhost:8080

📝 Licencia
Este proyecto se distribuye bajo la licencia MIT. Puedes usarlo libremente siempre que mantengas la atribución a sus autores.


