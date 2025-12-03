/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mx.unison.proyectosensor.cliente;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Roberto Moreno
 */
public class PaginaPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PaginaPrincipal.class.getName());
    
  
    private static final Color AZUL_UNISON = new Color(0, 82, 158);
    
    // ========== PANELES DE VISTAS ==========
    private Cliente panelMonitor;
    private Historico panelHistorico;
    private JPanel panelInicio;

    /**
     * Creates new form PaginaPrincipal
     */
    public PaginaPrincipal() {
        initComponents();
        setLocationRelativeTo(null);
        inicializarVistas();
        guardarPanelInicio();
        
    }
    
    // ========== INICIALIZACIÓN ==========
    
    /**
     * Crea las instancias de los paneles de vistas
     */
    private void inicializarVistas() {
        panelMonitor = new Cliente();
        panelHistorico = new Historico();
    }
    
    /**
     * Guarda referencia al panel de inicio
     */
    private void guardarPanelInicio() {
        // Clonar el contenido actual (página de inicio)
        Container contentPane = getContentPane();
        
        // Crear panel de inicio con los componentes
        panelInicio = new JPanel();
        panelInicio.setLayout(new BorderLayout());
        panelInicio.setBackground(Color.WHITE);
        
        // Crear panel central con logo y título
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBackground(Color.WHITE);
        
        // Espaciador superior
        centro.add(Box.createRigidArea(new Dimension(0, 80)));
        
        // Logo (si existe en el diseño)
        if (lblLogo != null) {
            JLabel logo = new JLabel(lblLogo.getText(), lblLogo.getIcon(), JLabel.CENTER);
            logo.setFont(lblLogo.getFont());
            logo.setForeground(lblLogo.getForeground());
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            centro.add(logo);
        }
        
        centro.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Título
        if (lblTitulo != null) {
            JLabel titulo = new JLabel(lblTitulo.getText(), JLabel.CENTER);
            titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
            titulo.setForeground(AZUL_UNISON);
            titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
            centro.add(titulo);
        }
        
        centro.add(Box.createRigidArea(new Dimension(0, 40)));
        
        // Botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(Color.WHITE);
        
        // Botón Monitor
        JButton btnMon = new JButton("Monitor");
        btnMon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnMon.setPreferredSize(new Dimension(150, 40));
        btnMon.setBackground(AZUL_UNISON);
        btnMon.setForeground(Color.WHITE);
        btnMon.setFocusPainted(false);
        btnMon.addActionListener(e -> mostrarMonitor());
        panelBotones.add(btnMon);
        
        // Botón Histórico
        JButton btnHist = new JButton("Histórico");
        btnHist.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnHist.setPreferredSize(new Dimension(150, 40));
        btnHist.setBackground(AZUL_UNISON);
        btnHist.setForeground(Color.WHITE);
        btnHist.setFocusPainted(false);
        btnHist.addActionListener(e -> mostrarHistorico());
        panelBotones.add(btnHist);
        
        centro.add(panelBotones);
        centro.add(Box.createVerticalGlue());
        
        panelInicio.add(centro, BorderLayout.CENTER);
    }
    
    // ========== NAVEGACIÓN ==========
    
    /**
     * Muestra la vista Monitor
     */
    private void mostrarMonitor() {
        cambiarVista(panelMonitor);
        setTitle("Sistema de Monitoreo - Monitor en Tiempo Real");
    }
    
    /**
     * Muestra la vista Histórico
     */
    private void mostrarHistorico() {
        cambiarVista(panelHistorico);
        setTitle("Sistema de Monitoreo - Históricos de Datos");
    }
    
    /**
     * Vuelve a la página de inicio
     */
    public void mostrarInicio(String inicio) {
        cambiarVista(panelInicio);
    setTitle("Sistema de Monitoreo - Universidad de Sonora");
    }
    
    
    /**
     * Cambia la vista actual
     */
    private void cambiarVista(JPanel nuevaVista) {
        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(nuevaVista, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        Histórico = new javax.swing.JButton();
        Monitor = new javax.swing.JButton();
        lblTitulo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sistema de Monitoreo - Universidad de Sonora");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(600, 600));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(994, 70));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setPreferredSize(new java.awt.Dimension(994, 80));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel4, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(800, 700));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 1));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setAlignmentY(0.9F);

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ESCUDO-COLOR V2.png"))); // NOI18N
        lblLogo.setAlignmentX(0.5F);
        lblLogo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        Histórico.setBackground(new java.awt.Color(0, 82, 156));
        Histórico.setForeground(new java.awt.Color(255, 255, 255));
        Histórico.setText("Histórico");
        Histórico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HistóricoActionPerformed(evt);
            }
        });

        Monitor.setBackground(new java.awt.Color(0, 82, 156));
        Monitor.setForeground(new java.awt.Color(255, 255, 255));
        Monitor.setText("Monitor");
        Monitor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MonitorActionPerformed(evt);
            }
        });

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 82, 157));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("“Sistema de Monitoreo en Tiempo Real”");
        lblTitulo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(Monitor)
                        .addGap(42, 42, 42)
                        .addComponent(Histórico)
                        .addGap(54, 54, 54))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(lblTitulo)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(lblLogo)
                        .addGap(49, 49, 49))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addComponent(lblLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTitulo)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Histórico)
                    .addComponent(Monitor))
                .addContainerGap())
        );

        jPanel1.add(jPanel5);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void HistóricoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HistóricoActionPerformed
        // TODO add your handling code here:
        mostrarHistorico();
    }//GEN-LAST:event_HistóricoActionPerformed

    private void MonitorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MonitorActionPerformed
        // TODO add your handling code here:
        mostrarMonitor();
    }//GEN-LAST:event_MonitorActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
     try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new PaginaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Histórico;
    private javax.swing.JButton Monitor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTitulo;
    // End of variables declaration//GEN-END:variables
}
