/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.proyectosensor.comunicacion;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;


/**
 *
 * @author Roberto Moreno
 */
public class ConexionSerial {
    private SerialPort port;
    private DataCallback callback;
    
    /**
     * Interface para recibir notificaciones de líneas de datos
     */
    public interface DataCallback {
        void onDataLine(String line);
    }
    
    /**
     * Constructor
     * @param portName Nombre del puerto (ej: "COM3", "/dev/ttyUSB0")
     * @param baud Velocidad en baudios (típicamente 9600)
     * @param cb Callback para recibir líneas de datos
     */
    public ConexionSerial(String portName, int baud, DataCallback cb) {
        this.port = SerialPort.getCommPort(portName);
        this.port.setBaudRate(baud);
        this.callback = cb;
    }
    
    /**
     * Abre el puerto serial y comienza a escuchar datos
     * @return true si se abrió correctamente
     */
    public boolean open() {
        if (port.openPort()) {
            port.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }
                
                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        return;
                    }
                    int available = port.bytesAvailable();
                    if (available <= 0) {
                        return;
                    }
                    byte[] buffer = new byte[available];
                    int numRead = port.readBytes(buffer, buffer.length);
                    if (numRead > 0) {
                        String s = new String(buffer).trim();
                        for (String line : s.split("\\r?\\n")) {
                            if (!line.isBlank()) {
                                callback.onDataLine(line.trim());
                            }
                        }
                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }
    
    
    public void close() {
        if (port != null) {
            port.removeDataListener();
            port.closePort();
        }
    }
    
   
    public static String[] obtenerPuertosDisponibles() {
        SerialPort[] puertos = SerialPort.getCommPorts();
        String[] nombresPuertos = new String[puertos.length];
        
        for (int i = 0; i < puertos.length; i++) {
            nombresPuertos[i] = puertos[i].getSystemPortName();
        }
        
        return nombresPuertos;
    }
    
    
    public static SerialPort[] obtenerPuertos() {
        return SerialPort.getCommPorts();
    }
    
    
    public boolean estaConectado() {
        return port != null && port.isOpen();
    }
    
    
    public String getNombrePuerto() {
        return port != null ? port.getSystemPortName() : "Ninguno";
    }

    
}
