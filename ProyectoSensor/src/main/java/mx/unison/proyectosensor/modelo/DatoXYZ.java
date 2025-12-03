/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.proyectosensor.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Roberto Moreno
 */
public class DatoXYZ {

   private float x;
    private float y;
    private float z;
    private long timestamp;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor que crea un dato con timestamp actual
     */
    public DatoXYZ(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructor que permite especificar el timestamp (para reconstruir desde BD)
     */
    
    public DatoXYZ(float x, float y, float z, long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }
    
    /**
     * Constructor vacío (útil para algunos frameworks)
     */
    public DatoXYZ() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getZ() {
        return z;
    }
    
    public void setZ(float z) {
        this.z = z;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Obtiene la fecha y hora en formato legible
     */
    public String getFechaHoraFormateada() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            java.time.ZoneId.systemDefault()
        );
        return dateTime.format(FORMATTER);
    }
    
    /**
     * Serializa el dato a formato CSV: timestamp,x,y,z
     * Este formato se usa para encriptar y enviar al servidor
     */
    @Override
    public String toString() {
        return timestamp + "," + x + "," + y + "," + z;
    }
    
    /**
     * Crea un objeto DatoXYZ desde una cadena en formato: timestamp,x,y,z
     * Útil para desencriptar y reconstruir el objeto
     */
    
    // valida si la cadena esta vacia si esta vacia lanza excepcion
    public static DatoXYZ fromString(String str) throws IllegalArgumentException {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("La cadena no puede estar vacía");
        }
        
        String[] parts = str.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Formato inválido. Se esperaba: timestamp,x,y,z");
        }
        
        try {
            long timestamp = Long.parseLong(parts[0].trim());
            float x = Float.parseFloat(parts[1].trim());
            float y = Float.parseFloat(parts[2].trim());
            float z = Float.parseFloat(parts[3].trim());
            return new DatoXYZ(x, y, z, timestamp);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error al parsear los valores numéricos: " + e.getMessage());
        }
    }
    
    /**
     * Representación detallada para debugging
     */
    public String toStringDetallado() {
        return String.format("DatoXYZ[timestamp=%s, x=%.2f, y=%.2f, z=%.2f]", 
                             getFechaHoraFormateada(), x, y, z);
    }
}