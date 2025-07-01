# AlertAi  
**Sistema de Eventos**

**AlertAi** es una plataforma web integral para la gestión de eventos públicos y la difusión de alertas comunitarias, desarrollada con tecnologías modernas como **Spring Boot**, **MySQL** y **Leaflet.js**.

Permite a los ciudadanos crear, explorar, denunciar y visualizar eventos relevantes para su comunidad mediante interfaces intuitivas.

El sistema también cuenta con un completo panel de administración que permite a los moderadores revisar reportes, gestionar usuarios y mantener la plataforma segura y actualizada. Su enfoque principal es fomentar la participación ciudadana y mejorar la comunicación en situaciones de emergencia o interés general.

---

## Autores

- Marla Mendez - [@MarlaMendez](https://github.com/MarlaMendez)
- Natalie Fernández - [@sabbinat](https://github.com/sabbinat)
- Nicolás Lara - [@niikila](https://github.com/niikila)
- Paula Suarez - [@TainaSuarez](https://github.com/TainaSuarez)

---

## Funcionalidades principales

### Para los usuarios:

- Registro y autenticación de cuentas.
- Creación, edición y eliminación de eventos.
- Visualización de eventos creados.
- Gestión del perfil de usuario (actualización de datos personales).
- Reporte/denuncia de eventos inapropiados.

### Para el administrador:

- Dashboard de administración.
- Gestión de categorías de eventos.
- Visualización y control de denuncias realizadas por los usuarios.
- Gestión general de usuarios y eventos.

---

## Tecnologías utilizadas

- Java 21+
- Spring Boot
- Spring Security
- MySQL
- JPA / Hibernate
- Thymeleaf
- Bootstrap 5
- Maven
- OpenStreetMap – visualización interactiva de la ubicación de eventos.
- Leaflet.js – biblioteca para mapas interactivos.

---

## Configuración de Credenciales

Para que el proyecto funcione correctamente, necesitás un archivo de configuración con las credenciales necesarias:

1. Copiá el archivo `application-secret.example.properties` como `application-secret.properties`.
2. Completá los siguientes campos:
   - Contraseña de aplicación de Gmail (para envío de correos).
   - Usuario y contraseña de la base de datos MySQL.

> ⚠️ Este archivo está en `.gitignore` y no se sube al repositorio por seguridad.

Si no contás con las credenciales, consultalo con el equipo de **Star Software**.

📌 **Guía para generar una contraseña de aplicación en Gmail:**  
[https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)

---

## Ejecución Local

1. Asegurate de tener **Java 21** y **MySQL** instalados.
2. Cloná el proyecto:

   ```bash
   git clone https://github.com/sabbinat/alertai.git
   ```

3. Configurá el archivo application-secret.properties como se explicó.
4. Ejecutá el proyecto desde tu IDE o con:

   ```bash
   mvn spring-boot:run
   ```
5. Accedé a la aplicación en:
http://localhost:8080

