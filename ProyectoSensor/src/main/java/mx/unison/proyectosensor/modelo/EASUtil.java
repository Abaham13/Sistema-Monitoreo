    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.proyectosensor.modelo;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;

/**
 *
 * @author Roberto Moreno
 */
public class EASUtil {
    private SecretKey clave;
    private static final String ARCHIVO_CLAVE = "clave_aes.key";
    private static EASUtil instance;
    
    /**
     * Constructor privado que genera una nueva clave aleatoria
     */
    private EASUtil() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        this.clave = kg.generateKey();
    }
    
    /**
     * Constructor que usa una clave existente en formato Base64
     */
    public EASUtil(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        this.clave = new SecretKeySpec(decoded, 0, decoded.length, "AES");
    }
    
    /**
     * Constructor que carga la clave desde un objeto SecretKey
     */
    public EASUtil(SecretKey clave) {
        this.clave = clave;
    }
    
    /**
     * Obtiene una instancia singleton de EASUtil con clave compartida
     * Si no existe archivo de clave, genera una nueva y la guarda
     * Si existe, la carga desde el archivo
     */
    public static synchronized EASUtil getInstance() throws Exception {
        if (instance == null) {
            File archivo = new File(ARCHIVO_CLAVE);
            
            if (archivo.exists()) {
                // Cargar clave existente
                String claveBase64 = cargarClaveDesdeArchivo();
                instance = new EASUtil(claveBase64);
                System.out.println("Clave AES cargada desde archivo: " + ARCHIVO_CLAVE);
            } else {
                // Generar nueva clave y guardarla
                instance = new EASUtil();
                instance.guardarClaveEnArchivo();
                System.out.println("Nueva clave AES generada y guardada en: " + ARCHIVO_CLAVE);
            }
        }
        return instance;
    }
    
    /**
     * Crea una nueva instancia con clave fresca (solo para testing o reset)
     */
    public static EASUtil crearNuevaInstancia() throws Exception {
        EASUtil util = new EASUtil();
        util.guardarClaveEnArchivo();
        instance = util;
        System.out.println("Nueva instancia de EASUtil creada con clave nueva");
        return util;
    }
    
    /**
     * Exporta la clave en formato Base64
     */
    public String getKeyBase64() {
        return Base64.getEncoder().encodeToString(clave.getEncoded());
    }
    
    /**
     * Guarda la clave actual en un archivo
     */
    public void guardarClaveEnArchivo() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CLAVE))) {
            writer.write(getKeyBase64());
            System.out.println("Clave guardada en: " + new File(ARCHIVO_CLAVE).getAbsolutePath());
        }
    }
    
    /**
     * Carga la clave desde el archivo
     */
    private static String cargarClaveDesdeArchivo() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO_CLAVE))) {
            return reader.readLine();
        }
    }
    
    /**
     * Encripta un texto plano usando AES-128/CBC con IV aleatorio
     * Formato de salida: "IV_base64:CipherText_base64"
     */
    public String encryptWithIV(String plain) throws Exception {
        if (plain == null || plain.isEmpty()) {
            throw new IllegalArgumentException("El texto a encriptar no puede estar vacío");
        }
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
        // Generar IV aleatorio de 16 bytes
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Encriptar
        cipher.init(Cipher.ENCRYPT_MODE, clave, ivSpec);
        byte[] ct = cipher.doFinal(plain.getBytes("UTF-8"));
        
        // Codificar en Base64
        String ivB64 = Base64.getEncoder().encodeToString(iv);
        String ctB64 = Base64.getEncoder().encodeToString(ct);
        
        return ivB64 + ":" + ctB64;
    }
    
    /**
     * Desencripta un texto en formato "IV_base64:CipherText_base64"
     */
    public String decryptWithIV(String ivAndCt) throws Exception {
        if (ivAndCt == null || ivAndCt.isEmpty()) {
            throw new IllegalArgumentException("El texto a desencriptar no puede estar vacío");
        }
        
        String[] parts = ivAndCt.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato IV:CT inválido. Se encontró: " + ivAndCt);
        }
        
        try {
            // Decodificar Base64
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] ct = Base64.getDecoder().decode(parts[1]);
            
            // Desencriptar
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, clave, new IvParameterSpec(iv));
            byte[] plain = cipher.doFinal(ct);
            
            return new String(plain, "UTF-8");
        } catch (Exception e) {
            throw new Exception("Error al desencriptar: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina el archivo de clave (útil para reset o testing)
     */
    public static boolean eliminarArchivoClave() {
        File archivo = new File(ARCHIVO_CLAVE);
        instance = null; // Resetear singleton
        boolean eliminado = archivo.delete();
        if (eliminado) {
            System.out.println("Archivo de clave eliminado");
        }
        return eliminado;
    }
    
    /**
     * Verifica si existe un archivo de clave
     */
    public static boolean existeArchivoClave() {
        return new File(ARCHIVO_CLAVE).exists();
    }
    
    /**
     * Obtiene la ruta completa del archivo de clave
     */
    public static String getRutaArchivoClave() {
        return new File(ARCHIVO_CLAVE).getAbsolutePath();
    }    
}
