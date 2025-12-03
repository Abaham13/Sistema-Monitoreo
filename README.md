# Sistema-Monitoreo
como  proyecto consiste en realizar un sistema que permita leer los datos de un un arduino mediante conexion serial, el sistema consiste en varios  componentes principales para su funcionamiento: dispositivo de arduino, cliente, servidor y el historico.

introduccion:
como  proyecto consiste en realizar un sistema que permita leer los datos de un un arduino mediante conexion serial, el sistema consiste en varios  componentes principales para su funcionamiento: dispositivo de arduino, cliente, servidor y el historico.

como funciona:
Dispositivo (Arduino)
 El Arduino: genera datos sensor (x, y, z) y los envía continuamente al cliente por medio de comunicación serial.

 El cliente: recibe los datos en tiempo real, los grafica y permite controlar el proceso mediante acciones como iniciar o detener la lectura   
 Además el cliente empaqueta la información junto con metadatos como la hora de inicio y fin de cada sesión.                                   

Servidor / Base de Datos:
 El servidor almacena todos los registros procesados, creando un historial completo y estructurado para futuras consultas.
 También administra los datos en forma cifrada, garantizando integridad y seguridad.

Módulo de Histórico:
Aunque aún no está implementado, este módulo permitirá visualizar sesiones completas, consultar intervalos de tiempo y analizar las mediciones guardadas.

<img width="464" height="287" alt="image" src="https://github.com/user-attachments/assets/ccfb571f-88d0-412d-bc1a-4c23d74e6c20" />



Dificultades:

- una de las dificultades que tuve fue en le diseño de la interface mas que nada utilizando la paleta de la educion visual GUI.
-problemas al momento de estructura el proyecto. clases, metodos, herramientas, a utilizar.
-incriptado de datos

soluciones:
-mediante invistigacion pude informarme mas de las proiedades y caracteristicas que se pueden implementar.
-comprension del problema y sus objetivos,analisis de la funciones del sistemas y investigacion de los problemas a resolver.
-pues tuve in investigar que elemntos podria utilizar para la incriptacion de datos.

estructura del codigo:
<img width="1020" height="576" alt="image" src="https://github.com/user-attachments/assets/168b0d05-9b0c-48bb-adbd-976101138481" />

pagina Principal:
El inicio de secion donde cuenta con dos botones tranporta a monitor y el otro a historico.
<img width="1020" height="576" alt="image" src="https://github.com/user-attachments/assets/fb5d0f91-4819-4507-8bab-3952286e70b7" />

pagina monitor:
en el cliente se grafican los datos, dados por el arduino o el simulador a tiempo real 
<img width="722" height="567" alt="image" src="https://github.com/user-attachments/assets/1662d1bd-9e99-41e4-bfef-e8ef5a21daf8" />

pagina historico:
en el historico cuenta resive todos los datos los desencripta y los grafica a demas tambien los pone en una tabla.
<img width="751" height="571" alt="image" src="https://github.com/user-attachments/assets/edf4311f-e52d-4059-a47a-457ebcaafb8b" />


funcionamiento completo
<img width="1013" height="569" alt="image" src="https://github.com/user-attachments/assets/c9c8f851-d27c-43c5-adc4-772d797ffc27" />


conclusiones:
en conclusion, en esta activida se pudieron implementar todos los conocimientos aprendidos en todo el semestre, fue un gran reto a superar ya que no solamente se trataba de aplicar los conocimientos que se aprendieron en el semetre si no tambien estuvimos obligados a investigar sobre las tecnologias a utiliza, investigar sobre otro tipo de funciones que pedia como requisitos el proyecto como lo podria ser la incriptacion de datos o la conexion serial. En consideracion creo que fue un proyecto aunque en momentos bastante estresante “algo necesario”



