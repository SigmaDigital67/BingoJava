import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Carton extends JFrame {
    private static final int TOTAL = 27;
    private static final int FILAS = 3;
    private static final int COLUMNAS = 9;
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtNombre;
    private JButton btnPartida, btnConectar, btnAutocompletar, btnColor;
    private JButton[] arrayBtn;
    private int[] arrayAleatorio;
    private Color colorMarcado = new Color(173, 216, 230);
    private Color colorEsperando = new Color(255, 102, 102);
    private Set<Integer> numerosArchivo = new LinkedHashSet<>();
    private Set<Integer> numerosPrevios = new LinkedHashSet<>();
    private Map<Integer, JButton> botonPorNumero = new HashMap<>();
    private File carpetaActual, archivoNumeros;
    private boolean autocompletarActivo = false;
    private boolean juegoTerminado = false;
    private File CONFIG = new File("config.txt");
    private long lastModified = 0;
    private Thread monitor;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new Carton().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Carton() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 850, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel = new JPanel(new GridLayout(0, 9, 0, 0));
        contentPane.add(panel, BorderLayout.CENTER);

        Color verde = new Color(34, 139, 34);
        Font font = new Font("SansSerif", Font.BOLD, 24);

        JLabel lblB = new JLabel("B", JLabel.CENTER);
        lblB.setOpaque(true);
        lblB.setBackground(verde);
        lblB.setFont(font);
        JLabel lblI = new JLabel("I", JLabel.CENTER);
        lblI.setOpaque(true);
        lblI.setBackground(verde);
        lblI.setFont(font);
        JLabel lblN = new JLabel("N", JLabel.CENTER);
        lblN.setOpaque(true);
        lblN.setBackground(verde);
        lblN.setFont(font);
        JLabel lblG = new JLabel("G", JLabel.CENTER);
        lblG.setOpaque(true);
        lblG.setBackground(verde);
        lblG.setFont(font);
        JLabel lblO = new JLabel("O", JLabel.CENTER);
        lblO.setOpaque(true);
        lblO.setBackground(verde);
        lblO.setFont(font);

        btnConectar = new JButton("Conectar");
        btnAutocompletar = new JButton("Autocompletar OFF");
        btnPartida = new JButton("Nueva Partida");
        btnPartida.setEnabled(false);
        btnColor = new JButton("Números anteriores");

        panel.add(lblB);
        panel.add(btnConectar);
        panel.add(lblI);
        panel.add(btnPartida);
        panel.add(lblN);
        panel.add(btnColor);
        panel.add(lblG);
        panel.add(btnAutocompletar);
        panel.add(lblO);

        arrayBtn = new JButton[TOTAL];
        for (int i = 0; i < TOTAL; i++) {
            arrayBtn[i] = new JButton(String.valueOf(i + 1));
            panel.add(arrayBtn[i]);
        }

        txtNombre = new JTextField("Tu Nombre");
        contentPane.add(txtNombre, BorderLayout.NORTH);

        arrayAleatorio = new int[TOTAL];

        cargarConfig();

        if (!java.beans.Beans.isDesignTime()) {
            eventos();
            llenarArray();
            generarCarton();
            iniciarMonitor();
        }
    }

    private void cargarConfig() {
        if (CONFIG.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(CONFIG))) {
                String ruta = br.readLine();
                if (ruta != null && !ruta.trim().isEmpty()) {
                    carpetaActual = new File(ruta.trim());
                    archivoNumeros = new File(carpetaActual, "Numeros.txt");
                }
            } catch (Exception e) {
            }
        }
    }

    private void guardarConfig(String ruta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG))) {
            bw.write(ruta);
        } catch (IOException e) {
        }
    }

    private void eventos() {
        btnPartida.addActionListener(e -> {
            llenarArray();
            generarCarton();
            btnColor.setEnabled(true);
            btnPartida.setEnabled(false);
            juegoTerminado = false;
            habilitarBotones(true);
            numerosArchivo.clear();
            numerosPrevios.clear();
        });

        btnColor.addActionListener(e -> {
            if (archivoNumeros == null || !archivoNumeros.exists()) return;
            StringBuilder sb = new StringBuilder();
            Set<Integer> unicos = new LinkedHashSet<>();
            try (BufferedReader br = new BufferedReader(new FileReader(archivoNumeros))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    unicos.add(Integer.parseInt(linea.trim()));
                }
            } catch (Exception ex) {
            }
            int count = 0;
            for (int n : unicos) {
                sb.append(n);
                count++;
                if (count % 20 == 0) sb.append("\n");
                else sb.append(" , ");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Números anteriores", JOptionPane.WARNING_MESSAGE);
        });

        btnConectar.addActionListener(e -> {
            String rutaActual = carpetaActual != null ? carpetaActual.getAbsolutePath() : "";
            String ruta = JOptionPane.showInputDialog(this, "Escribe la ruta de la carpeta:", rutaActual);
            if (ruta != null && !ruta.trim().isEmpty()) {
                File carpeta = new File(ruta.trim());
                if (carpeta.isDirectory()) {
                    carpetaActual = carpeta;
                    archivoNumeros = new File(carpeta, "Numeros.txt");
                    if (archivoNumeros.exists()) {
                        guardarConfig(ruta.trim());
                    }
                }
            }
            btnConectar.setBackground(new Color(100, 221, 100));
        });

        btnAutocompletar.addActionListener(e -> {
            if (juegoTerminado) return;
            if (archivoNumeros == null || !archivoNumeros.exists()) return;
            
            autocompletarActivo = !autocompletarActivo;
            btnAutocompletar.setText(autocompletarActivo ? "Autocompletar ON" : "Autocompletar OFF");
            btnAutocompletar.setBackground(autocompletarActivo ? new Color(144, 238, 144) : Color.WHITE);
            
            // Cuando se activa el autocompletar, marcar inmediatamente todos los números del archivo
            if (autocompletarActivo) {
                marcarNumerosExistentes();
            }
        });

        for (JButton b : arrayBtn) {
            b.addActionListener(e -> {
                if (juegoTerminado) return;
                if (b.getBackground().equals(Color.GRAY)) return;
                int num = Integer.parseInt(b.getText());
                if (numerosArchivo.contains(num)) {
                    b.setBackground(colorMarcado);
                } else {
                    b.setBackground(colorEsperando);
                }
                comprobarGanador();
            });
        }
    }

    // Nuevo método para marcar números existentes cuando se activa autocompletar
    private void marcarNumerosExistentes() {
        if (archivoNumeros == null || !archivoNumeros.exists()) return;
        
        Set<Integer> numerosActuales = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoNumeros))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                try {
                    numerosActuales.add(Integer.parseInt(linea.trim()));
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception ex) {
            return;
        }
        
        // Marcar todos los botones cuyos números estén en el archivo
        for (JButton boton : arrayBtn) {
            try {
                int numeroBoton = Integer.parseInt(boton.getText());
                if (numerosActuales.contains(numeroBoton) && 
                    !boton.getBackground().equals(Color.GRAY)) {
                    boton.setBackground(colorMarcado);
                }
            } catch (NumberFormatException ignored) {}
        }
        
        comprobarGanador();
    }

    private void iniciarMonitor() {
        monitor = new Thread(() -> {
            while (true) {
                if (archivoNumeros != null && archivoNumeros.exists()) {
                    long actual = archivoNumeros.lastModified();

                    if (actual != lastModified) {
                        lastModified = actual;

                        Set<Integer> nuevos = new LinkedHashSet<>();
                        try (BufferedReader br = new BufferedReader(new FileReader(archivoNumeros))) {
                            String linea;
                            while ((linea = br.readLine()) != null) {
                                try {
                                    nuevos.add(Integer.parseInt(linea.trim()));
                                } catch (NumberFormatException ignored) {}
                            }
                        } catch (Exception ex) {
                            continue;
                        }

                        // Solo detectar los que realmente son nuevos
                        Set<Integer> realmenteNuevos = new LinkedHashSet<>(nuevos);
                        realmenteNuevos.removeAll(numerosPrevios);

                        numerosPrevios.clear();
                        numerosPrevios.addAll(nuevos);
                        numerosArchivo.clear();
                        numerosArchivo.addAll(nuevos);

                        if (!realmenteNuevos.isEmpty()) {
                            SwingUtilities.invokeLater(() -> {
                                for (int n : realmenteNuevos) {
                                    JOptionPane.showMessageDialog(this, "Número salido: " + n, "Nuevo número", JOptionPane.WARNING_MESSAGE);
                                }
                                
                                // Siempre marcar todos los números cuando autocompletar está activo
                                if (autocompletarActivo) {
                                    for (int n : nuevos) {
                                        for (JButton boton : arrayBtn) {
                                            try {
                                                int numeroBoton = Integer.parseInt(boton.getText());
                                                if (numeroBoton == n && boton.isEnabled() && 
                                                    !boton.getBackground().equals(Color.GRAY)) {
                                                    boton.setBackground(colorMarcado);
                                                }
                                            } catch (NumberFormatException ignored) {}
                                        }
                                    }
                                }
                                comprobarGanador();
                            });
                        }
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        });
        monitor.start();
    }

    private void llenarArray() {
        int pos = 0;
        while (pos < TOTAL) {
            int n = (int) (Math.random() * 90 + 1);
            boolean repetido = false;
            for (int i = 0; i < pos; i++) {
                if (arrayAleatorio[i] == n) {
                    repetido = true;
                    break;
                }
            }
            if (!repetido) {
                arrayAleatorio[pos++] = n;
            }
        }
        Arrays.sort(arrayAleatorio);
        
        // Actualizar el mapeo de botones por número real
        botonPorNumero.clear();
        for (int i = 0; i < TOTAL; i++) {
            arrayBtn[i].setText(String.valueOf(arrayAleatorio[i]));
            arrayBtn[i].setBackground(Color.LIGHT_GRAY);
            arrayBtn[i].setEnabled(true);
            // Mapear el número REAL del botón, no la posición
            botonPorNumero.put(arrayAleatorio[i], arrayBtn[i]);
        }
    }

    private void generarCarton() {
        for (JButton b : arrayBtn) b.setBackground(Color.WHITE);
        for (int f = 0; f < FILAS; f++) {
            int des = 0;
            while (des < 4) {
                int c = (int) (Math.random() * COLUMNAS);
                int idx = f * COLUMNAS + c;
                if (idx < TOTAL && !arrayBtn[idx].getBackground().equals(Color.GRAY)) {
                    arrayBtn[idx].setBackground(Color.GRAY);
                    arrayBtn[idx].setEnabled(false);
                    des++;
                }
            }
        }
    }

    private void comprobarGanador() {
        boolean todos = true;
        for (JButton b : arrayBtn) {
            if (!b.getBackground().equals(colorMarcado) && !b.getBackground().equals(Color.GRAY)) {
                todos = false;
                break;
            }
        }
        if (todos) {
            juegoTerminado = true;
            habilitarBotones(false);
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) nombre = "Jefesito";
            if (carpetaActual != null) {
                File ganador = new File(carpetaActual, "Ganador.txt");
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(ganador, false))) {
                    bw.write(nombre);
                } catch (IOException ignored) {}
            }
            JOptionPane.showMessageDialog(this, nombre + " ha ganado!", "¡Felicidades!", JOptionPane.WARNING_MESSAGE);
            btnPartida.setEnabled(true);
        }
    }

    private void habilitarBotones(boolean habilitar) {
        for (JButton b : arrayBtn) {
            if (b.getBackground().equals(Color.GRAY)) continue;
            b.setEnabled(habilitar);
        }
    }
}