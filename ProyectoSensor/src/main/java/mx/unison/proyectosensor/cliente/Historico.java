/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mx.unison.proyectosensor.cliente;

import java.awt.BorderLayout;


import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import mx.unison.proyectosensor.modelo.DatoXYZ;
import mx.unison.proyectosensor.modelo.EASUtil;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
/**
 *
 * @author Roberto Moreno
 */
public class Historico extends javax.swing.JPanel {
    
    private DefaultTableModel modeloTabla;
    private EASUtil encriptador;
    private PaginaPrincipal paginaPrincipal;
    
    private XYSeries seriesX;
    private XYSeries seriesY;
    private XYSeries seriesZ;
    private XYSeriesCollection dataset;
    private ChartPanel chartPanel;
    
    private List<DatoXYZ> datosCompletos; // Almacena todos los datos
    
    private ServerSocket serverSocket;
    private volatile boolean escuchando = false;
    private int puertoEscucha = 5001; 
    
    private static final DateTimeFormatter formatoFecha = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter formatoHora = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Creates new form Historico
     */
    public Historico() {
        initComponents();
        
        inicializarEncriptador();
        inicializarTabla();
        inicializarGrafica();
        datosCompletos = new ArrayList<>();
        
         iniciarServidorEscucha();
    }
     
    /**
     * Inicializa el encriptador AES
     */
    
     private void inicializarEncriptador() {
        try {
            encriptador = EASUtil.getInstance();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al inicializar encriptador: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void inicializarTabla() {
        String[] columnas = {"ID", "fecha", "hora", "x", "y", "z"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaDatos.setModel(modeloTabla);
        
        // Configurar anchos de columnas
        tablaDatos.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaDatos.getColumnModel().getColumn(1).setPreferredWidth(100); // fecha
        tablaDatos.getColumnModel().getColumn(2).setPreferredWidth(100); // hora
        tablaDatos.getColumnModel().getColumn(3).setPreferredWidth(80);  // x
        tablaDatos.getColumnModel().getColumn(4).setPreferredWidth(80);  // y
        tablaDatos.getColumnModel().getColumn(5).setPreferredWidth(80);  // z
    }
    
    /**
     * Inicializa la gráfica histórica
     */
    private void inicializarGrafica() {
        seriesX = new XYSeries("Eje X");
        seriesY = new XYSeries("Eje Y");
        seriesZ = new XYSeries("Eje Z");
        
        dataset = new XYSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Histórico de Sensores XYZ",
            "Tiempo",
            "Valor",
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            true, true, false
        );
        
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.RED);
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.GREEN);
        chart.getXYPlot().getRenderer().setSeriesPaint(2, Color.BLUE);
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        panelGrafica.setLayout(new BorderLayout());
        panelGrafica.removeAll();
        panelGrafica.add(chartPanel, BorderLayout.CENTER);
        panelGrafica.revalidate();
        panelGrafica.repaint();
    }
    
    /**
     * Inicia el servidor que escucha actualizaciones automáticas del ServidorBD
     */
    private void iniciarServidorEscucha() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puertoEscucha);
                escuchando = true;
                
                
                System.out.println("   Histórico iniciado");
                System.out.println("   Escuchando en puerto: " + puertoEscucha);
                System.out.println("   Esperando actualizaciones automáticas...");
               
                
                while (escuchando && !serverSocket.isClosed()) {
                    try {
                        Socket cliente = serverSocket.accept();
                        System.out.println("[:M] Actualización automática recibida del servidor");
                        
                        
                        new Thread(() -> recibirActualizacionAutomatica(cliente)).start();
                        
                    } catch (IOException e) {
                        if (escuchando) {
                            System.err.println(" Error aceptando conexión: " + e.getMessage());
                        }
                    }
                }
                
            } catch (IOException e) {
                System.err.println(" Error al iniciar servidor de escucha: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Recibe actualización automática del servidor cuando guarda nuevos datos
     */
    private void recibirActualizacionAutomatica(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println(" Recibiendo actualización automática...");
            
            
            datosCompletos.clear();
            
            String linea;
            int contador = 0;
            
            while ((linea = in.readLine()) != null) {
                if ("END".equals(linea.trim())) {
                    System.out.println("    Fin de actualización");
                    break;
                }
                
                if (linea.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // Desencriptar
                    String datosPlanos = encriptador.decryptWithIV(linea);
                    
                    // Reconstruir DatoXYZ
                    DatoXYZ dato = DatoXYZ.fromString(datosPlanos);
                    datosCompletos.add(dato);
                    contador++;
                    
                } catch (Exception e) {
                    System.err.println("    Error procesando línea: " + e.getMessage());
                }
            }
            
            System.out.println("    Total de registros actualizados: " + contador);
            
            
            SwingUtilities.invokeLater(() -> {
                actualizarVisualizacion(datosCompletos);
            });
            
        } catch (IOException e) {
            System.err.println("[!] Error recibiendo actualización: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println(" Conexión cerrada\n");
            } catch (IOException e) {
                System.err.println(" Error cerrando socket: " + e.getMessage());
            }
        }
    }
    
    
    public void cargarDatosDelServidor() {
        new Thread(() -> {
            try {
                String servidor = txtServidor.getText().trim();
                int puerto = Integer.parseInt(txtPuerto.getText().trim());
                
                System.out.println("[→] Consultando histórico completo de " + servidor + ":" + puerto);
                
                try (Socket socket = new Socket(servidor, puerto);
                     BufferedWriter out = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream()));
                     BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {
                    
                    
                    out.write("GET_HISTORICO");
                    out.newLine();
                    out.flush();
                    
                    System.out.println("[→] Recibiendo datos encriptados...");
                    
                    datosCompletos.clear();
                    String linea;
                    int contador = 0;
                    
                    while ((linea = in.readLine()) != null) {
                        if ("END".equals(linea.trim())) {
                            break;
                        }
                        
                        if (linea.trim().isEmpty()) {
                            continue;
                        }
                        
                        try {
                            
                            String datosPlanos = encriptador.decryptWithIV(linea);
                            
                           
                            DatoXYZ dato = DatoXYZ.fromString(datosPlanos);
                            datosCompletos.add(dato);
                            contador++;
                            
                        } catch (Exception e) {
                            System.err.println("Error procesando línea: " + e.getMessage());
                        }
                    }
                    
                    System.out.println("✓ Total de registros recibidos: " + contador);
                    
                    
                    final int totalRegistros = contador;
                    
                    
                    SwingUtilities.invokeLater(() -> {
                        actualizarVisualizacion(datosCompletos);
                        JOptionPane.showMessageDialog(Historico.this,
                            "Se cargaron " + totalRegistros + " registros correctamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
                
            } catch (NumberFormatException e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(Historico.this,
                        "Puerto inválido", "Error", JOptionPane.ERROR_MESSAGE));
            } catch (Exception e) {
                System.err.println("Error al cargar datos: " + e.getMessage());
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(Historico.this,
                        "Error al conectar con el servidor:\n" + e.getMessage() + 
                        "\n\nAsegúrate de que el ServidorBD esté ejecutándose.",
                        "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }
    
    
    private void actualizarVisualizacion(List<DatoXYZ> datos) {
        
        modeloTabla.setRowCount(0);
        
        
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();
        
        int id = 1;
        for (DatoXYZ dato : datos) {
            
            LocalDateTime fechaHora = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(dato.getTimestamp()),
                ZoneId.systemDefault()
            );
            
            String fecha = fechaHora.format(formatoFecha);
            String hora = fechaHora.format(formatoHora);
            
            
            modeloTabla.addRow(new Object[]{
                id,
                fecha,
                hora,
                (int) dato.getX(),
                (int) dato.getY(),
                (int) dato.getZ()
            });
            
            // Agregar a gráfica
            seriesX.add(id, dato.getX());
            seriesY.add(id, dato.getY());
            seriesZ.add(id, dato.getZ());
            
            id++;
        }
    }
    
    
    public void consultarConFiltros() {
       try {
        String fechaInicioStr = txtFechaInicio.getText().trim();
        String horaInicioStr = txtHoraInicio.getText().trim();
        String fechaFinStr = txtFechaFin.getText().trim();
        String horaFinStr = txtHoraFin.getText().trim();
        
        
        if (fechaInicioStr.isEmpty() && horaInicioStr.isEmpty() && 
            fechaFinStr.isEmpty() && horaFinStr.isEmpty()) {
            actualizarVisualizacion(datosCompletos);
            return;
        }
        
        
        LocalDateTime inicio = null;
        LocalDateTime fin = null;
        
        
        if (!fechaInicioStr.isEmpty()) {
            LocalDate fecha = LocalDate.parse(fechaInicioStr, formatoFecha);
            LocalTime hora;
            
            if (horaInicioStr.isEmpty()) {
                
                hora = LocalTime.of(0, 0, 0);
            } else {
                hora = LocalTime.parse(horaInicioStr, formatoHora);
            }
            
            inicio = LocalDateTime.of(fecha, hora);
            System.out.println("[→] Filtro inicio: " + inicio);
        }
        
        
        if (!fechaFinStr.isEmpty()) {
            LocalDate fecha = LocalDate.parse(fechaFinStr, formatoFecha);
            LocalTime hora;
            
            if (horaFinStr.isEmpty()) {
               
                hora = LocalTime.of(23, 59, 59);
            } else {
                hora = LocalTime.parse(horaFinStr, formatoHora);
            }
            
            fin = LocalDateTime.of(fecha, hora);
            System.out.println("[→] Filtro fin: " + fin);
        }
        
        
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            JOptionPane.showMessageDialog(this,
                "La fecha/hora de inicio no puede ser posterior a la fecha/hora de fin",
                "Error en filtros", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        
        List<DatoXYZ> datosFiltrados = new ArrayList<>();
        int descartadosPorInicio = 0;
        int descartadosPorFin = 0;
        
        for (DatoXYZ dato : datosCompletos) {
            LocalDateTime fechaHoraDato = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(dato.getTimestamp()),
                ZoneId.systemDefault()
            );
            
            boolean cumpleFiltro = true;
            
            
            if (inicio != null && fechaHoraDato.isBefore(inicio)) {
                cumpleFiltro = false;
                descartadosPorInicio++;
            }
            
            // Verificar límite superior (<=)
            if (fin != null && fechaHoraDato.isAfter(fin)) {
                cumpleFiltro = false;
                descartadosPorFin++;
            }
            
            if (cumpleFiltro) {
                datosFiltrados.add(dato);
            }
        }
        
        // Log de debug
        System.out.println("[✓] Filtrado completado:");
        System.out.println("    Total de datos: " + datosCompletos.size());
        System.out.println("    Datos filtrados: " + datosFiltrados.size());
        System.out.println("    Descartados por inicio: " + descartadosPorInicio);
        System.out.println("    Descartados por fin: " + descartadosPorFin);
        
        
        actualizarVisualizacion(datosFiltrados);
        
        
        String mensaje = "Se encontraron " + datosFiltrados.size() + " registros";
        if (inicio != null || fin != null) {
            mensaje += "\n\nRango aplicado:";
            if (inicio != null) {
                mensaje += "\nDesde: " + inicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (fin != null) {
                mensaje += "\nHasta: " + fin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }
        
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Consulta Completada", 
            JOptionPane.INFORMATION_MESSAGE);
        
    } catch (java.time.format.DateTimeParseException e) {
        String mensaje = "Error en el formato de fecha/hora.\n\n" +
                "Formatos correctos:\n" +
                "• Fecha: yyyy-MM-dd (ejemplo: 2025-12-03)\n" +
                "• Hora: HH:mm:ss (ejemplo: 14:30:00)\n\n" +
                "Error específico: " + e.getMessage();
        
        JOptionPane.showMessageDialog(this,
            mensaje,
            "Error de Formato", 
            JOptionPane.ERROR_MESSAGE);
        
        System.err.println("[!] Error de parseo: " + e.getMessage());
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Error inesperado al aplicar filtros:\n" + e.getMessage(),
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        
        System.err.println("[!] Error inesperado: " + e.getMessage());
        e.printStackTrace();
    }
    }
    
    
    public void limpiarFiltros() {
        txtFechaInicio.setText("");
        txtHoraInicio.setText("");
        txtFechaFin.setText("");
        txtHoraFin.setText("");
        actualizarVisualizacion(datosCompletos);
    }
    
    /**
     * Regresa a la vista anterior
     * Llamado por el botón "regresar"
     */
    public void regresar() {
     int respuesta = JOptionPane.showConfirmDialog(this,
        "¿Desea limpiar todos los datos antes de regresar?",
        "Confirmar", JOptionPane.YES_NO_CANCEL_OPTION);
    
    if (respuesta == JOptionPane.CANCEL_OPTION) {
        return; // No hacer nada si cancela
    }
    
    if (respuesta == JOptionPane.YES_OPTION) {
        datosCompletos.clear();
        modeloTabla.setRowCount(0);
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();
        txtFechaInicio.setText("");
        txtHoraInicio.setText("");
        txtFechaFin.setText("");
        txtHoraFin.setText("");
    }
    
    
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame instanceof PaginaPrincipal) {
        PaginaPrincipal pagina = (PaginaPrincipal) frame;
        pagina.mostrarInicio("INICIO"); 
    }
    }
    
    
    public void detener() {
        try {
            escuchando = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("[✓] Servidor de escucha detenido");
        } catch (IOException e) {
            System.err.println("Error al detener servidor: " + e.getMessage());
        }
    }
    
    /**
     * Exporta los datos visibles a CSV
     */
    public void exportarCSV() {
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay datos para exportar",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar histórico como CSV");
        fileChooser.setSelectedFile(new java.io.File("historico.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("ID,Fecha,Hora,X,Y,Z");
                
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    writer.printf("%d,%s,%s,%d,%d,%d%n",
                        modeloTabla.getValueAt(i, 0),
                        modeloTabla.getValueAt(i, 1),
                        modeloTabla.getValueAt(i, 2),
                        modeloTabla.getValueAt(i, 3),
                        modeloTabla.getValueAt(i, 4),
                        modeloTabla.getValueAt(i, 5)
                    );
                }
                
                JOptionPane.showMessageDialog(this,
                    "Histórico exportado correctamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al exportar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void setPaginaPrincipal(PaginaPrincipal pagina) {
    this.paginaPrincipal = pagina;
    }
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelFiltros = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtFechaInicio = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtHoraInicio = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtFechaFin = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtHoraFin = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        btnCargar = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        btnConsultar = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        btnregresar = new javax.swing.JButton();
        panelSuperior = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelServidor = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtPuerto = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtServidor = new javax.swing.JTextField();
        panelCentral = new javax.swing.JPanel();
        panelGraficaContainer = new javax.swing.JPanel();
        panelGrafica = new javax.swing.JPanel();
        panelTablaContainer = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaDatos = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1100, 750));
        setLayout(new java.awt.BorderLayout(10, 10));

        panelFiltros.setBackground(new java.awt.Color(255, 255, 255));
        panelFiltros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 82, 158), 2)));
        panelFiltros.setPreferredSize(new java.awt.Dimension(200, 0));
        panelFiltros.setRequestFocusEnabled(false);
        panelFiltros.setLayout(new javax.swing.BoxLayout(panelFiltros, javax.swing.BoxLayout.Y_AXIS));

        jLabel4.setFont(new java.awt.Font("Segoe UI Emoji", 0, 14)); // NOI18N
        jLabel4.setText("Filtros de consulta:");
        jLabel4.setAlignmentX(0.5F);
        jLabel4.setAlignmentY(0.15F);
        panelFiltros.add(jLabel4);

        jLabel5.setAlignmentX(0.5F);
        jLabel5.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel5.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel5.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel5);
        panelFiltros.add(jLabel6);

        jLabel7.setText("Fecha Inicio:");
        jLabel7.setAlignmentX(0.5F);
        panelFiltros.add(jLabel7);

        txtFechaInicio.setMaximumSize(new java.awt.Dimension(123, 20));
        txtFechaInicio.setMinimumSize(new java.awt.Dimension(5, 5));
        panelFiltros.add(txtFechaInicio);

        jLabel8.setAlignmentX(0.5F);
        jLabel8.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel8.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel8.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel8);

        jLabel9.setText("Hora Inicio:");
        jLabel9.setAlignmentX(0.5F);
        panelFiltros.add(jLabel9);

        txtHoraInicio.setMaximumSize(new java.awt.Dimension(123, 20));
        txtHoraInicio.setMinimumSize(new java.awt.Dimension(5, 5));
        panelFiltros.add(txtHoraInicio);

        jLabel10.setAlignmentX(0.5F);
        jLabel10.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel10.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel10.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel10);

        jLabel11.setText("Fecha fin:");
        jLabel11.setAlignmentX(0.5F);
        panelFiltros.add(jLabel11);

        txtFechaFin.setMaximumSize(new java.awt.Dimension(123, 20));
        txtFechaFin.setMinimumSize(new java.awt.Dimension(5, 5));
        panelFiltros.add(txtFechaFin);

        jLabel12.setAlignmentX(0.5F);
        jLabel12.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel12.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel12.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel12);

        jLabel13.setText("Hora fin:");
        jLabel13.setAlignmentX(0.5F);
        panelFiltros.add(jLabel13);

        txtHoraFin.setMaximumSize(new java.awt.Dimension(123, 20));
        txtHoraFin.setMinimumSize(new java.awt.Dimension(5, 5));
        panelFiltros.add(txtHoraFin);

        jLabel14.setAlignmentX(0.5F);
        jLabel14.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel14.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel14.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel14);

        jLabel17.setAlignmentX(0.5F);
        jLabel17.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel17.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel17.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel17);

        btnLimpiar.setBackground(new java.awt.Color(0, 82, 154));
        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setText("limpiar");
        btnLimpiar.setAlignmentX(0.5F);
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });
        panelFiltros.add(btnLimpiar);

        jLabel15.setAlignmentX(0.5F);
        jLabel15.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel15.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel15.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel15);

        btnCargar.setBackground(new java.awt.Color(0, 82, 156));
        btnCargar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCargar.setForeground(new java.awt.Color(255, 255, 255));
        btnCargar.setText("cargar");
        btnCargar.setAlignmentX(0.5F);
        btnCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarActionPerformed(evt);
            }
        });
        panelFiltros.add(btnCargar);

        jLabel18.setAlignmentX(0.5F);
        jLabel18.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel18.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel18.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel18);

        btnConsultar.setBackground(new java.awt.Color(0, 82, 153));
        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setForeground(new java.awt.Color(255, 255, 255));
        btnConsultar.setText("consultar");
        btnConsultar.setAlignmentX(0.5F);
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });
        panelFiltros.add(btnConsultar);
        panelFiltros.add(jLabel16);

        jLabel19.setAlignmentX(0.5F);
        jLabel19.setMaximumSize(new java.awt.Dimension(350, 15));
        jLabel19.setMinimumSize(new java.awt.Dimension(123, 50));
        jLabel19.setPreferredSize(new java.awt.Dimension(20, 16));
        panelFiltros.add(jLabel19);

        btnregresar.setBackground(new java.awt.Color(0, 82, 153));
        btnregresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnregresar.setForeground(new java.awt.Color(255, 255, 255));
        btnregresar.setText("regresar");
        btnregresar.setAlignmentX(0.5F);
        btnregresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnregresarActionPerformed(evt);
            }
        });
        panelFiltros.add(btnregresar);

        add(panelFiltros, java.awt.BorderLayout.EAST);

        panelSuperior.setBackground(new java.awt.Color(0, 82, 158));
        panelSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelSuperior.setLayout(new java.awt.BorderLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText("Historicos de datos");
        panelSuperior.add(lblTitulo, java.awt.BorderLayout.WEST);

        panelServidor.setBackground(new java.awt.Color(0, 84, 159));
        panelServidor.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Puerto:");
        panelServidor.add(jLabel3);

        txtPuerto.setColumns(5);
        txtPuerto.setText("5000");
        panelServidor.add(txtPuerto);
        panelServidor.add(jLabel2);

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Servidor:");
        panelServidor.add(jLabel1);

        txtServidor.setColumns(10);
        txtServidor.setText("localhost");
        txtServidor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtServidorActionPerformed(evt);
            }
        });
        panelServidor.add(txtServidor);

        panelSuperior.add(panelServidor, java.awt.BorderLayout.LINE_END);

        add(panelSuperior, java.awt.BorderLayout.NORTH);

        panelCentral.setBackground(new java.awt.Color(255, 255, 255));
        panelCentral.setLayout(new java.awt.GridLayout(2, 1, 10, 10));

        panelGraficaContainer.setBackground(new java.awt.Color(255, 255, 255));
        panelGraficaContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 86, 153), 2), "Grafica Historica", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(0, 82, 157))); // NOI18N
        panelGraficaContainer.setLayout(new java.awt.BorderLayout());

        panelGrafica.setBackground(new java.awt.Color(255, 255, 255));
        panelGrafica.setLayout(new java.awt.BorderLayout());
        panelGraficaContainer.add(panelGrafica, java.awt.BorderLayout.CENTER);

        panelCentral.add(panelGraficaContainer);

        panelTablaContainer.setBackground(new java.awt.Color(255, 255, 255));
        panelTablaContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 82, 158), 2), "Datos en tabla", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(0, 82, 158))); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tablaDatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "fecha", "hora", "x", "y", "z"
            }
        ));
        jScrollPane1.setViewportView(tablaDatos);

        javax.swing.GroupLayout panelTablaContainerLayout = new javax.swing.GroupLayout(panelTablaContainer);
        panelTablaContainer.setLayout(panelTablaContainerLayout);
        panelTablaContainerLayout.setHorizontalGroup(
            panelTablaContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTablaContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelTablaContainerLayout.setVerticalGroup(
            panelTablaContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaContainerLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        panelCentral.add(panelTablaContainer);

        add(panelCentral, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtServidorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtServidorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtServidorActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarFiltros();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarActionPerformed
        // TODO add your handling code here:
        cargarDatosDelServidor();
    }//GEN-LAST:event_btnCargarActionPerformed

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarConFiltros();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnregresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnregresarActionPerformed
    regresar();
    
    }//GEN-LAST:event_btnregresarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnregresar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelGrafica;
    private javax.swing.JPanel panelGraficaContainer;
    private javax.swing.JPanel panelServidor;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JPanel panelTablaContainer;
    private javax.swing.JTable tablaDatos;
    private javax.swing.JTextField txtFechaFin;
    private javax.swing.JTextField txtFechaInicio;
    private javax.swing.JTextField txtHoraFin;
    private javax.swing.JTextField txtHoraInicio;
    private javax.swing.JTextField txtPuerto;
    private javax.swing.JTextField txtServidor;
    // End of variables declaration//GEN-END:variables
}
