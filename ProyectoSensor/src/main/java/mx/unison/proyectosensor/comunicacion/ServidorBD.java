/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.proyectosensor.comunicacion;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import mx.unison.proyectosensor.modelo.DatoXYZ;
import mx.unison.proyectosensor.modelo.EASUtil;



/**
 *
 * @author Roberto Moreno
 */
public class ServidorBD {
    
    private int puerto;
    private ServerSocket serverSocket;
    private Connection conexionBD;
    private EASUtil encriptador;
    private volatile boolean ejecutando;
    
    // Puerto adicional para enviar datos al histórico
    private String historicoIP = "localhost";
    private int historicoPuerto = 5001;
    
    private static final String DB_NAME = "monitorBD.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;
    
    private static final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * Constructor del servidor
     * @param puerto Puerto en el que escuchará el servidor
     * @param dbFilePath Ruta del archivo de base de datos SQLite
     */
     public ServidorBD(int puerto) throws Exception {
        this.puerto = puerto;
        this.ejecutando = false;
        this.encriptador = EASUtil.getInstance();
        
        
        inicializarBaseDatos();
    }
    
    
    public ServidorBD(int puerto, String historicoIP, int historicoPuerto) throws Exception {
        this(puerto);
        this.historicoIP = historicoIP;
        this.historicoPuerto = historicoPuerto;
    }
    
    
    private void inicializarBaseDatos() throws SQLException {
        conexionBD = DriverManager.getConnection(DB_URL);
        
        String sql = "CREATE TABLE IF NOT EXISTS datos_sensor ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "x INTEGER NOT NULL,"
                + "y INTEGER NOT NULL,"
                + "z INTEGER NOT NULL,"
                + "fecha_de_captura TEXT NOT NULL,"
                + "hora_de_captura TEXT NOT NULL"
                + ");";
        
        try (Statement stmt = conexionBD.createStatement()) {
            stmt.execute(sql);
            System.out.println("✓ Base de datos 'monitorBD' inicializada correctamente");
            System.out.println("  Ubicación: " + new File(DB_NAME).getAbsolutePath());
        }
    }
    
    /**
     * Inicia el servidor y comienza a escuchar conexiones
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(puerto);
        ejecutando = true;
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("    ServidorBD iniciado correctamente");
        System.out.println("    Puerto: " + puerto);
        System.out.println("    Encriptación: AES-128/CBC");
        System.out.println("    Base de datos: " + DB_NAME);
        System.out.println("    Histórico: " + historicoIP + ":" + historicoPuerto);
        System.out.println("    Esperando conexiones...");
        System.out.println("═══════════════════════════════════════════\n");
        
        while (ejecutando && !serverSocket.isClosed()) {
            try {
                Socket cliente = serverSocket.accept();
                System.out.println("[+] Cliente conectado: " + cliente.getInetAddress());
                
                // Manejar cliente en hilo separado
                new Thread(() -> handleClient(cliente)).start();
                
            } catch (IOException e) {
                if (ejecutando) {
                    System.err.println("[!] Error aceptando cliente: " + e.getMessage());
                }
            }
        }
    }
    
    
    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()))) {
            
            String linea = in.readLine();
            
            if (linea == null || linea.trim().isEmpty()) {
                System.out.println("[-] Cliente envió línea vacía");
                socket.close();
                return;
            }
            
            String comando = linea.trim();
            System.out.println("[→] Comando recibido: " + 
                (comando.length() > 50 ? comando.substring(0, 50) + "..." : comando));
            
            
            if ("GET_HISTORICO".equalsIgnoreCase(comando)) {
                procesarConsultaHistorico(out);
                
            } else if (comando.startsWith("GET_COUNT")) {
                procesarContador(out);
                
            } else if (comando.contains(":")) {
                
                procesarGuardarDato(comando, out);
                
            } else {
                System.err.println("[!] Comando no reconocido: " + comando);
                out.write("ERROR:Comando no válido");
                out.newLine();
                out.flush();
            }
            
        } catch (Exception e) {
            System.err.println("[!] Error manejando cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                System.out.println("[-] Cliente desconectado\n");
            } catch (IOException e) {
                System.err.println("[!] Error cerrando socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Procesa el guardado de un dato encriptado
     * Después de guardar, envía TODOS los datos al histórico
     */
    private void procesarGuardarDato(String datosEncriptados, BufferedWriter out) 
            throws Exception {
        
        System.out.println("[→] Procesando dato encriptado...");
        
        try {
            // 1. Desencriptar
            String datosPlanos = encriptador.decryptWithIV(datosEncriptados);
            System.out.println("    Desencriptado: " + datosPlanos);
            
            
            DatoXYZ dato = DatoXYZ.fromString(datosPlanos);
            
            
            LocalDateTime fechaHora = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(dato.getTimestamp()),
                java.time.ZoneId.systemDefault()
            );
            
            String fecha = fechaHora.format(formatoFecha);
            String hora = fechaHora.format(formatoHora);
            
            // 4. Insertar en base de datos
            String sql = "INSERT INTO datos_sensor(x, y, z, fecha_de_captura, hora_de_captura) "
                    + "VALUES(?, ?, ?, ?, ?)";
            
            try (PreparedStatement ps = conexionBD.prepareStatement(sql)) {
                ps.setInt(1, (int) dato.getX());
                ps.setInt(2, (int) dato.getY());
                ps.setInt(3, (int) dato.getZ());
                ps.setString(4, fecha);
                ps.setString(5, hora);
                ps.executeUpdate();
                
                System.out.println("   Guardado: x=" + (int)dato.getX() + 
                                 ", y=" + (int)dato.getY() + 
                                 ", z=" + (int)dato.getZ() + 
                                 " (" + fecha + " " + hora + ")");
                
               
                enviarTodosLosRegistrosAlHistorico();
                
                
                out.write("OK");
                out.newLine();
                out.flush();
            }
            
        } catch (Exception e) {
            System.err.println("    ✗ Error al guardar: " + e.getMessage());
            out.write("ERROR:" + e.getMessage());
            out.newLine();
            out.flush();
        }
    }
    
    
    
    
    
    private void enviarTodosLosRegistrosAlHistorico() {
        new Thread(() -> {
            try {
                System.out.println("[→] Enviando histórico completo al componente...");
                
                String sql = "SELECT x, y, z, fecha_de_captura, hora_de_captura "
                        + "FROM datos_sensor ORDER BY id ASC";
                
                int count = 0;
                
                try (Socket socket = new Socket(historicoIP, historicoPuerto);
                     BufferedWriter out = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream()));
                     Statement stmt = conexionBD.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        String fecha = rs.getString("fecha_de_captura");
                        String hora = rs.getString("hora_de_captura");
                        
                        
                        LocalDateTime fechaHora = LocalDateTime.parse(
                            fecha + " " + hora,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        );
                        long timestamp = fechaHora.atZone(java.time.ZoneId.systemDefault())
                                                 .toInstant().toEpochMilli();
                        
                        
                        DatoXYZ dato = new DatoXYZ(x, y, z, timestamp);
                        
                        
                        String datosPlanos = dato.toString();
                        
                        
                        String datosEncriptados = encriptador.encryptWithIV(datosPlanos);
                        
                        
                        out.write(datosEncriptados);
                        out.newLine();
                        count++;
                    }
                    
                    
                    out.write("END");
                    out.newLine();
                    out.flush();
                    
                    System.out.println("    ✓ Enviados " + count + " registros encriptados al histórico");
                }
                
            } catch (Exception e) {
                System.err.println("    ✗ Error enviando al histórico: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Procesa una consulta de histórico completo
     */
    private void procesarConsultaHistorico(BufferedWriter out) throws Exception {
        System.out.println("[→] Procesando consulta GET_HISTORICO...");
        
        String sql = "SELECT x, y, z, fecha_de_captura, hora_de_captura "
                + "FROM datos_sensor ORDER BY id ASC";
        
        int count = 0;
        
        try (Statement stmt = conexionBD.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                String fecha = rs.getString("fecha_de_captura");
                String hora = rs.getString("hora_de_captura");
                
                // Convertir fecha y hora a timestamp
                LocalDateTime fechaHora = LocalDateTime.parse(
                    fecha + " " + hora,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                long timestamp = fechaHora.atZone(java.time.ZoneId.systemDefault())
                                         .toInstant().toEpochMilli();
                
                
                DatoXYZ dato = new DatoXYZ(x, y, z, timestamp);
                
                
                String datosPlanos = dato.toString();
                
               
                String datosEncriptados = encriptador.encryptWithIV(datosPlanos);
                
                
                out.write(datosEncriptados);
                out.newLine();
                count++;
            }
            
            
            out.write("END");
            out.newLine();
            out.flush();
            
            System.out.println("    ✓ Enviados " + count + " registros encriptados");
        }
    }
    
    
    private void procesarContador(BufferedWriter out) throws Exception {
        String sql = "SELECT COUNT(*) as total FROM datos_sensor";
        
        try (Statement stmt = conexionBD.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("total");
            }
            
            out.write("COUNT:" + count);
            out.newLine();
            out.flush();
            
            System.out.println("    ✓ Total de registros: " + count);
        }
    }
    
    
    public void stop() throws Exception {
        System.out.println("\n[!] Deteniendo servidor...");
        ejecutando = false;
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        
        if (conexionBD != null && !conexionBD.isClosed()) {
            conexionBD.close();
        }
        
        System.out.println("[✓] Servidor detenido correctamente");
    }
    
    
    public static void main(String[] args) {
        int puerto = 5000;
        String historicoIP = "localhost";
        int historicoPuerto = 5001;
        
        // Procesar argumentos: java ServidorBD [puerto] [historicoIP] [historicoPuerto]
        if (args.length >= 1) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido, usando default: 5000");
            }
        }
        if (args.length >= 2) {
            historicoIP = args[1];
        }
        if (args.length >= 3) {
            try {
                historicoPuerto = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto histórico inválido, usando default: 5001");
            }
        }
        
        try {
            ServidorBD servidor = new ServidorBD(puerto, historicoIP, historicoPuerto);
            
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    servidor.stop();
                } catch (Exception e) {
                    System.err.println("Error al detener servidor: " + e.getMessage());
                }
            }));
            
            
            servidor.start();
            
        } catch (Exception e) {
            System.err.println("❌ Error fatal al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
