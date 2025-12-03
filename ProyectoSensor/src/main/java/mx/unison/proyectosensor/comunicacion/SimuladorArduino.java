/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.proyectosensor.comunicacion;
import java.util.Random;

/**
 *
 * @author Roberto Moreno
 */
public class SimuladorArduino {
    private Thread hiloEnvio;
    private volatile boolean ejecutando = false;
    private Random random;
    private DataCallback callback;
    
    /**
     * Interface para recibir los datos simulados
     */
    public interface DataCallback {
        void onDataLine(String line);
    }
    
    /**
     * Constructor
     * @param callback Callback para recibir las líneas de datos
     */
    public SimuladorArduino(DataCallback callback) {
        this.callback = callback;
        this.random = new Random();
    }
    
    /**
     * Inicia la simulación (equivalente a abrir puerto serial)
     */
    public boolean iniciar() {
        if (ejecutando) {
            System.err.println("El simulador ya está ejecutándose");
            return false;
        }
        
        ejecutando = true;
        
        hiloEnvio = new Thread(() -> {
            System.out.println("Simulador Arduino iniciado");
            
            while (ejecutando) {
                try {
                   
                    
                    int num_x = random.nextInt(101); // 0 a 100
                    int num_y = random.nextInt(101);
                    int num_z = random.nextInt(101);
                    
                    
                    String dataString = "x:" + num_x + ",y:" + num_y + ",z:" + num_z;
                    
                    
                    if (callback != null) {
                        callback.onDataLine(dataString);
                    }
                    
                    
                    Thread.sleep(1000); // 1 segundo
                    
                } catch (InterruptedException e) {
                    System.out.println("Hilo del simulador interrumpido");
                    break;
                }
            }
            
            System.out.println("Simulador Arduino detenido");
        });
        
        hiloEnvio.setDaemon(true); 
        hiloEnvio.start();
        
        return true;
    }
    
    
    public void detener() {
        if (!ejecutando) {
            return;
        }
        
        ejecutando = false;
        
        if (hiloEnvio != null && hiloEnvio.isAlive()) {
            hiloEnvio.interrupt();
            try {
                hiloEnvio.join(1000); // Esperar máximo 1 segundo
            } catch (InterruptedException e) {
                System.err.println("Error al detener el simulador");
            }
        }
    }
    
    
    public boolean estaEjecutando() {
        return ejecutando;
    }
    
    public String getNombrePuerto() {
        return "SIMULADOR";
    }
    
    public static String[] obtenerPuertosSimulados() {
        return new String[]{"SIMULADOR - Arduino Virtual"};
    }
    
    
}
