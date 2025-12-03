/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mx.unison.proyectosensor.cliente;

import mx.unison.proyectosensor.comunicacion.SimuladorArduino;
import mx.unison.proyectosensor.modelo.DatoXYZ;
import mx.unison.proyectosensor.modelo.EASUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import mx.unison.proyectosensor.comunicacion.ConexionSerial;

/**
 *
 * @author Roberto Moreno
 */
public class Cliente extends javax.swing.JPanel {
    
    private XYSeries seriesX;
    private XYSeries seriesY;
    private XYSeries seriesZ;
    private XYSeriesCollection dataset;
    private ChartPanel chartPanel;
    
    private ConexionSerial conexion;
    private SimuladorArduino simulador;
    private EASUtil encriptador;
    private DefaultListModel<String> modeloLista;
    private JList<String> listaDatos;
    
    private boolean leyendo = false;
    private int contadorPuntos = 0;
    private String servidorIP = "localhost";
    private int servidorPuerto = 5000;
    private PaginaPrincipal paginaPrincipal;
    
    
   
    /**
     * Creates new form Cliente
     */
    
    
    public Cliente() {
        initComponents();
        inicializarGrafica();
        inicializarEncriptador();
        cargarPuertos();
        inicializarLista();
        
    }
    
    private void inicializarLista() {
        modeloLista = new DefaultListModel<>();
        listaDatos = new JList<>(modeloLista);
        
        
    }
    
    private void inicializarGrafica() {
        
        seriesX = new XYSeries("Eje X");
        seriesY = new XYSeries("Eje Y");
        seriesZ = new XYSeries("Eje Z");
        
        dataset = new XYSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);
        
        // Crear gráfica
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Lectura de Sensores XYZ",
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
    
    private void inicializarEncriptador() {
        try {
            encriptador = EASUtil.getInstance();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al inicializar encriptador: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarPuertos() {
        cbPuertoCOM.removeAllItems();
        cbPuertoCOM.addItem("SIMULADOR");
        cbPuertoCOM.addItem("Ardunio 1");
        
        String[] puertos = ConexionSerial.obtenerPuertosDisponibles();
        for (String puerto : puertos) {
            cbPuertoCOM.addItem(puerto);
        }
        
        if (cbPuertoCOM.getItemCount() > 0) {
            cbPuertoCOM.setSelectedIndex(0);
        }
    }

    private void iniciarLectura() {
        try {
            String puerto = (String) cbPuertoCOM.getSelectedItem();
            
            if (puerto == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un puerto");
                return;
            }
            
            if ("SIMULADOR".equals(puerto)) {
                simulador = new SimuladorArduino(this::procesarLinea);
                simulador.iniciar();
            } else {
                conexion = new ConexionSerial(puerto, 9600, this::procesarLinea);
                if (!conexion.open()) {
                    JOptionPane.showMessageDialog(this, "No se pudo abrir " + puerto);
                    return;
                }
            }
            
            leyendo = true;
            btnIniciarLectura.setText("detener lectura");
            cbPuertoCOM.setEnabled(false);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void detenerLectura() {
        if (conexion != null) {
            conexion.close();
            conexion = null;
        }
        if (simulador != null) {
            simulador.detener();
            simulador = null;
        }
        
        leyendo = false;
        btnIniciarLectura.setText("iniciar lectura");
        cbPuertoCOM.setEnabled(true);
    }
    
    private void procesarLinea(String linea) {
        try {
            
            String[] partes = linea.split(",");
            if (partes.length != 3) return;
            
            float x = Float.parseFloat(partes[0].split(":")[1].trim());
            float y = Float.parseFloat(partes[1].split(":")[1].trim());
            float z = Float.parseFloat(partes[2].split(":")[1].trim());
            
            DatoXYZ dato = new DatoXYZ(x, y, z);
            
            
            SwingUtilities.invokeLater(() -> {
                // 1. Agregar a gráfica
                contadorPuntos++;
                seriesX.add(contadorPuntos, x);
                seriesY.add(contadorPuntos, y);
                seriesZ.add(contadorPuntos, z);
                
                // Limitar a 50 puntos visibles
                if (seriesX.getItemCount() > 50) {
                    seriesX.remove(0);
                    seriesY.remove(0);
                    seriesZ.remove(0);
                }
                
               
                modeloLista.insertElementAt(
                    String.format("x:%.0f y:%.0f z:%.0f", x, y, z), 0);
                
                
                if (modeloLista.size() > 100) {
                    modeloLista.removeElementAt(100);
                }
            });
            
           
            new Thread(() -> enviarAlServidor(dato)).start();
            
        } catch (Exception e) {
            System.err.println("Error procesando línea: " + e.getMessage());
        }
    }
    
    private void enviarAlServidor(DatoXYZ dato) {
        try {
            String datosPlanos = dato.toString();
            String datosEncriptados = encriptador.encryptWithIV(datosPlanos);
            
            try (Socket socket = new Socket(servidorIP, servidorPuerto);
                 BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {
                
                out.write(datosEncriptados);
                out.newLine();
                out.flush();
                
                String respuesta = in.readLine();
                if (!"OK".equals(respuesta)) {
                    System.err.println("Respuesta del servidor: " + respuesta);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al enviar al servidor: " + e.getMessage());
        }
    }
    
    public void regresar() {
    int respuesta = JOptionPane.showConfirmDialog(this,
        "¿Desea limpiar todos los datos antes de regresar?",
        "Confirmar", JOptionPane.YES_NO_CANCEL_OPTION);
    
    if (respuesta == JOptionPane.CANCEL_OPTION) {
        return; 
    }
    
    if (respuesta == JOptionPane.YES_OPTION) {
        
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();
        detenerLectura();
    }
    
    
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (frame instanceof PaginaPrincipal) {
        PaginaPrincipal pagina = (PaginaPrincipal) frame;
        pagina.mostrarInicio("INICIO");
    }
}
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        panelControl = new javax.swing.JPanel();
        btnIniciarLectura = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbPuertoCOM = new javax.swing.JComboBox<>();
        btnregresar = new javax.swing.JButton();
        panelGraficaContainer = new javax.swing.JPanel();
        panelGrafica = new javax.swing.JPanel();
        almacenEstado = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1000, 523));
        setLayout(new java.awt.BorderLayout(10, 10));

        panelTitulo.setBackground(new java.awt.Color(0, 82, 153));
        panelTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panelTitulo.setMinimumSize(new java.awt.Dimension(0, 0));
        panelTitulo.setName(""); // NOI18N
        panelTitulo.setPreferredSize(new java.awt.Dimension(0, 80));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText(" Monitor en Tiempo Real ");

        javax.swing.GroupLayout panelTituloLayout = new javax.swing.GroupLayout(panelTitulo);
        panelTitulo.setLayout(panelTituloLayout);
        panelTituloLayout.setHorizontalGroup(
            panelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTituloLayout.createSequentialGroup()
                .addGap(188, 188, 188)
                .addComponent(lblTitulo)
                .addContainerGap(237, Short.MAX_VALUE))
        );
        panelTituloLayout.setVerticalGroup(
            panelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTituloLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(lblTitulo)
                .addContainerGap(5, Short.MAX_VALUE))
        );

        add(panelTitulo, java.awt.BorderLayout.NORTH);

        panelControl.setBackground(new java.awt.Color(255, 255, 255));
        panelControl.setMaximumSize(new java.awt.Dimension(200, 40));
        panelControl.setMinimumSize(new java.awt.Dimension(0, 0));
        panelControl.setPreferredSize(new java.awt.Dimension(100, 50));

        btnIniciarLectura.setText("iniciar lectura");
        btnIniciarLectura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarLecturaActionPerformed(evt);
            }
        });
        panelControl.add(btnIniciarLectura);
        panelControl.add(jLabel1);

        jLabel2.setText("puerto COM:");
        panelControl.add(jLabel2);

        cbPuertoCOM.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPuertoCOM.setPreferredSize(new java.awt.Dimension(250, 30));
        cbPuertoCOM.setVerifyInputWhenFocusTarget(false);
        panelControl.add(cbPuertoCOM);

        btnregresar.setText("regresar");
        btnregresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnregresarActionPerformed(evt);
            }
        });
        panelControl.add(btnregresar);

        add(panelControl, java.awt.BorderLayout.PAGE_END);

        panelGraficaContainer.setBackground(new java.awt.Color(255, 255, 255));
        panelGraficaContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 82, 152), 2), "Grafica en tiempo real", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(0, 82, 154))); // NOI18N
        panelGraficaContainer.setLayout(new java.awt.BorderLayout());

        panelGrafica.setBackground(new java.awt.Color(255, 255, 255));
        panelGrafica.setPreferredSize(new java.awt.Dimension(900, 500));

        javax.swing.GroupLayout panelGraficaLayout = new javax.swing.GroupLayout(panelGrafica);
        panelGrafica.setLayout(panelGraficaLayout);
        panelGraficaLayout.setHorizontalGroup(
            panelGraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 606, Short.MAX_VALUE)
        );
        panelGraficaLayout.setVerticalGroup(
            panelGraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 234, Short.MAX_VALUE)
        );

        panelGraficaContainer.add(panelGrafica, java.awt.BorderLayout.CENTER);

        almacenEstado.setBackground(new java.awt.Color(255, 255, 255));
        almacenEstado.setMinimumSize(new java.awt.Dimension(100, 30));
        almacenEstado.setPreferredSize(new java.awt.Dimension(500, 35));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 82, 152));
        jLabel3.setText("Listo");

        javax.swing.GroupLayout almacenEstadoLayout = new javax.swing.GroupLayout(almacenEstado);
        almacenEstado.setLayout(almacenEstadoLayout);
        almacenEstadoLayout.setHorizontalGroup(
            almacenEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(almacenEstadoLayout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addComponent(jLabel3)
                .addContainerGap(496, Short.MAX_VALUE))
        );
        almacenEstadoLayout.setVerticalGroup(
            almacenEstadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, almacenEstadoLayout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addContainerGap())
        );

        panelGraficaContainer.add(almacenEstado, java.awt.BorderLayout.PAGE_END);

        add(panelGraficaContainer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    
    private void btnIniciarLecturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarLecturaActionPerformed
        // TODO add your handling code here:
        if (!leyendo) {
       iniciarLectura();
   } else {
       detenerLectura();
   }
    }//GEN-LAST:event_btnIniciarLecturaActionPerformed

    private void btnregresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnregresarActionPerformed
        // TODO add your handling code here:
     regresar();
    }//GEN-LAST:event_btnregresarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel almacenEstado;
    private javax.swing.JButton btnIniciarLectura;
    private javax.swing.JButton btnregresar;
    private javax.swing.JComboBox<String> cbPuertoCOM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel panelControl;
    private javax.swing.JPanel panelGrafica;
    private javax.swing.JPanel panelGraficaContainer;
    private javax.swing.JPanel panelTitulo;
    // End of variables declaration//GEN-END:variables
}
