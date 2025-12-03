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


Dificultades:

- una de las dificultades que tuve fue en le diseño de la interface mas que nada utilizando la paleta de la educion visual GUI.
-problemas al momento de estructura el proyecto. clases, metodos, herramientas, a utilizar.
-incriptado de datos

soluciones:
-mediante invistigacion pude informarme mas de las proiedades y caracteristicas que se pueden implementar.
-comprension del problema y sus objetivos,analisis de la funciones del sistemas y investigacion de los problemas a resolver.
-pues tuve in investigar que elemntos podria utilizar para la incriptacion de datos.



conclusiones:
en conclusion, en esta activida se pudieron implementar todos los conocimientos aprendidos en todo el semestre, fue un gran reto a superar ya que no solamente se trataba de aplicar los conocimientos que se aprendieron en el semetre si no tambien estuvimos obligados a investigar sobre las tecnologias a utiliza, investigar sobre otro tipo de funciones que pedia como requisitos el proyecto como lo podria ser la incriptacion de datos o la conexion serial. En consideracion creo que fue un proyecto aunque en momentos bastante estresante “algo necesario”



