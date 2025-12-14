

DESCRIPCION DEL PROYECTO

Este proyecto consiste en una aplicación Android que implementa un sistema de comunicación Cliente/Servidor utilizando sockets TCP en Java.
La aplicación permite que un dispositivo actúe como servidor, escuchando conexiones entrantes, y que otro dispositivo actúe como cliente, enviando mensajes al servidor y recibiendo respuestas.

La comunicación se realiza en tiempo real y se apoya en el uso de hilos (Threads) para evitar bloqueos de la interfaz gráfica, cumpliendo las buenas prácticas de desarrollo Android.

OBJETIVOS

Comprender el funcionamiento de la comunicación por sockets TCP

Implementar un modelo Cliente/Servidor

Gestionar la comunicación en segundo plano mediante hilos

Evitar operaciones de red en el hilo principal (NetworkOnMainThreadException)

Probar la comunicación entre emuladores Android
------------------------------------------------------

TECNOLOGIAS UTILIZADAS

Lenguaje: Java

Plataforma: Android

Comunicación: Sockets TCP

Concurrencia: Threads

IDE: Android Studio

Control de versiones: Git / GitHub
-----------------------------------------------------

ARQUITECTURA DEL PROYECTO

- MainActivity

Gestiona la interfaz de usuario

Permite iniciar el servidor o conectar el cliente

Envía mensajes escritos por el usuario

Muestra el estado de la conexión y los mensajes recibidos

- ServerThread

Implementa el servidor TCP

Escucha conexiones en un puerto determinado

Acepta una conexión de cliente

Recibe mensajes del cliente y responde con un eco

Se ejecuta en un hilo independiente

- ClientThread

Implementa el cliente TCP

Se conecta al servidor mediante IP y puerto

Envía mensajes al servidor

Escucha respuestas del servidor en segundo plano

- Funcionamiento

Un dispositivo inicia el servidor indicando un puerto (por defecto 6000).

Otro dispositivo se conecta como cliente, indicando la IP del servidor y el puerto.

El cliente envía mensajes de texto.

El servidor recibe el mensaje y responde reenviándolo (eco).

Los mensajes se muestran en la interfaz de ambos dispositivos.

Asimismo, esta implementación soporta un solo cliente conectado a la vez, lo cual es suficiente para el objetivo académico del proyecto.

- Pruebas realizadas

La aplicación ha sido probada utilizando emuladores Android:

Emulador servidor: Pixel (API 36)

Emulador cliente: Pixel (API 36)

Configuración del cliente en emulador:

IP del servidor: 10.0.2.2

Puerto: 6000

Esto permite que el emulador cliente se comunique correctamente con el servidor que se ejecuta en el entorno local.

Estructura del proyecto
app/
 - src/main/java/es/ua/eps/chatsockets/
      MainActivity.java
      ClientThread.java
      ServerThread.java
---------------------------------------
CONCLUSIONES: 
El proyecto demuesta el uso correcto de sockets TCP Android.
La Aplicaciòn cumple con los objetivos planteados, y para extensiones y implementaciones.




     
