/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.proyectosensor.comunicacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Roberto Moreno
 */
public class ConexionSQLite {
    
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";
    private Connection conn;
    private String dbFilePath;
    
    
    public ConexionSQLite(String dbFilePath) throws SQLException {
        this.dbFilePath = dbFilePath;
        this.conn = DriverManager.getConnection(DB_URL_PREFIX + dbFilePath);
        crearTablaSiNoExiste();
        System.out.println("Conexión SQLite establecida: " + dbFilePath);
        System.out.println("Ubicación: " + new java.io.File(dbFilePath).getAbsolutePath());
    }
    
    
    private void crearTablaSiNoExiste() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS historico ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "datos TEXT NOT NULL"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'historico' verificada/creada correctamente");
        }
    }
    
    
    public synchronized void insertarRegistro(String datos) throws SQLException {
        String sql = "INSERT INTO historico(datos) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, datos);
            ps.executeUpdate();
            System.out.println("Registro insertado correctamente");
        }
    }
    
    
    public synchronized List<String> obtenerRegistros() throws SQLException {
        String sql = "SELECT id, timestamp, datos FROM historico ORDER BY id DESC";
        List<String> registros = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String ts = rs.getString("timestamp");
                String datos = rs.getString("datos");
                registros.add(id + " | " + ts + " | " + datos);
            }
            
            System.out.println("Se recuperaron " + registros.size() + " registros");
        }
        
        return registros;
    }
    
    
    public synchronized List<String> obtenerDatosEncriptados() throws SQLException {
        String sql = "SELECT datos FROM historico ORDER BY id ASC";
        List<String> datos = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                datos.add(rs.getString("datos"));
            }
        }
        
        return datos;
    }
    
    
    public synchronized int contarRegistros() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM historico";
        
        try (Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        
        return 0;
    }
    
    /**
     * Elimina todos los registros (útil para testing)
     */
    public synchronized void limpiarTabla() throws SQLException {
        String sql = "DELETE FROM historico";
        
        try (Statement stmt = conn.createStatement()) {
            int filasEliminadas = stmt.executeUpdate(sql);
            System.out.println("Se eliminaron " + filasEliminadas + " registros");
        }
    }
    
    /**
     * Cierra la conexión con la base de datos
     */
    public void cerrar() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexión SQLite cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica si la conexión está activa
     */
    public boolean estaConectado() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

