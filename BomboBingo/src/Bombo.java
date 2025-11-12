import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Bombo extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtNumeroActual;
    private JTextField txtPreguns;
    private JTextArea txtHistorialnums;
    private JTextArea txtRespus;
    private JTextField txtBingo;
    private JTextField txtSostenible;
    private JButton btnNext;
    private JButton btnConectar;
    private JButton btnGenerar;

    private int[] arrayNums;
    private boolean[] numerosSacados;
    private String historial = "";
    private boolean juegoBloqueado = false;
    private String rutaCarpetaSeleccionada = "";
    private Timer timerMonitoreo;

    // Usamos un array en lugar de ArrayList
    private String[][] preguntasYRespuestas = new String[500][5]; // máx. 500 preguntas
    private int totalPreguntas = 0;

    // Carpeta de red
    private File carpetaVinculada;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Bombo frame = new Bombo();
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al iniciar la aplicación: " + e.getMessage());
            }
        });
    }

    public Bombo() {
        try {
            // Solo carpeta de red
            File carpetaRed = new File("\\\\192.168.0.37\\Almingo\\");
            if (carpetaRed.exists() && carpetaRed.isDirectory()) {
                carpetaVinculada = carpetaRed;
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "❌ No se pudo acceder a la carpeta de red: \\\\192.168.0.37\\Almingo\\",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }

            // Crear/limpiar archivo de números
            File archivoNumeros = new File(carpetaVinculada, "Numeros.txt");
            if (archivoNumeros.exists()) archivoNumeros.delete();

            setTitle("Bombo");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setBounds(100, 100, 801, 527);
            contentPane = new JPanel();
            contentPane.setBackground(new Color(64, 0, 0));
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            setContentPane(contentPane);
            contentPane.setLayout(new BorderLayout(0, 0));

            JPanel panel = new JPanel();
            contentPane.add(panel, BorderLayout.CENTER);
            panel.setLayout(new GridLayout(4, 2, 0, 0));

            txtBingo = new JTextField("Bingo");
            txtBingo.setBackground(new Color(128, 255, 128));
            txtBingo.setEditable(false);
            txtBingo.setForeground(new Color(0, 128, 255));
            txtBingo.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(txtBingo);

            txtSostenible = new JTextField("Sostenible");
            txtSostenible.setBackground(new Color(64, 128, 128));
            txtSostenible.setEditable(false);
            txtSostenible.setForeground(new Color(0, 255, 0));
            txtSostenible.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(txtSostenible);

            txtNumeroActual = new JTextField("Número Actual:");
            txtNumeroActual.setForeground(new Color(0, 128, 0));
            panel.add(txtNumeroActual);

            txtPreguns = new JTextField("Preguntas");
            txtPreguns.setForeground(new Color(64, 128, 128));
            panel.add(txtPreguns);

            txtHistorialnums = new JTextArea("HistorialNums");
            txtHistorialnums.setForeground(new Color(0, 128, 255));
            txtHistorialnums.setLineWrap(true);
            txtHistorialnums.setWrapStyleWord(true);
            txtHistorialnums.setEditable(false);
            panel.add(txtHistorialnums);

            txtRespus = new JTextArea("Respuesta");
            txtRespus.setForeground(new Color(0, 255, 64));
            txtRespus.setLineWrap(true);
            txtRespus.setWrapStyleWord(true);
            txtRespus.setEditable(false);
            panel.add(txtRespus);

            btnConectar = new JButton("Conectar");
            btnGenerar = new JButton("Generar");
            btnGenerar.setEnabled(false);
            panel.add(btnConectar);
            panel.add(btnGenerar);

            btnNext = new JButton("Siguiente");
            btnNext.setFont(new Font("Tahoma", Font.BOLD, 16));
            contentPane.add(btnNext, BorderLayout.PAGE_END);

            inicializarNumeros();
            cargarPreguntas();
            generarInicio();
            eventos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al crear la interfaz: " + e.getMessage());
        }
    }

    private void eventos() {
        btnNext.addActionListener(e -> {
            if (juegoBloqueado) {
                JOptionPane.showMessageDialog(this, "⚠️ Juego bloqueado. Hay un ganador registrado.");
                return;
            }
            mostrarNumero();
            mostrarPreguntaAleatoria();
        });

        btnConectar.addActionListener(e -> {
            String ruta = JOptionPane.showInputDialog(
                this, 
                "Introduce la ruta de la carpeta:", 
                "Ruta de carpeta", 
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (ruta != null && !ruta.trim().isEmpty()) {
                File carpetaElegida = new File(ruta.trim());
                if (carpetaElegida.exists() && carpetaElegida.isDirectory()) {
                    rutaCarpetaSeleccionada = carpetaElegida.getAbsolutePath();
                    btnGenerar.setEnabled(true);
                    
                    // Iniciar monitoreo continuo del archivo Ganador.txt
                    iniciarMonitoreoGanador(carpetaElegida);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ La ruta no es válida o no es una carpeta.");
                }
            }
        });

        btnGenerar.addActionListener(e -> {
            if (rutaCarpetaSeleccionada.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Primero debes seleccionar una carpeta con 'Conectar'.");
                return;
            }
            // Detener el monitoreo anterior si existe
            if (timerMonitoreo != null) {
                timerMonitoreo.cancel();
            }
            generarNuevoJuego();
            cambiarColoresEcologicos();
        });
    }

    private void iniciarMonitoreoGanador(File carpeta) {
        // Cancelar timer anterior si existe
        if (timerMonitoreo != null) {
            timerMonitoreo.cancel();
        }
        
        timerMonitoreo = new Timer();
        timerMonitoreo.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                File ganadorFile = new File(carpeta, "Ganador.txt");
                if (ganadorFile.exists()) {
                    try (Scanner sc = new Scanner(ganadorFile)) {
                        if (sc.hasNextLine()) {
                            String nombre = sc.nextLine().trim();
                            if (!nombre.isEmpty()) {
                                // Usar SwingUtilities.invokeLater para actualizar UI desde el timer
                                SwingUtilities.invokeLater(() -> {
                                    // Añadir a GanadoresAnteriores.txt en CARPETA DE RED
                                    File ganadoresAnteriores = new File(carpetaVinculada, "GanadoresAnteriores.txt");
                                    try (PrintWriter pw = new PrintWriter(new FileWriter(ganadoresAnteriores, true))) {
                                        pw.println(nombre);
                                    } catch (IOException ex) {
                                        JOptionPane.showMessageDialog(null, "Error al guardar ganador: " + ex.getMessage());
                                    }

                                    // Mostrar warning
                                    JOptionPane.showMessageDialog(
                                            Bombo.this,
                                            "🎉 ¡BINGO! Ganador: " + nombre + "\n\nEl juego se ha bloqueado.",
                                            "¡Tenemos Ganador!",
                                            JOptionPane.WARNING_MESSAGE
                                    );

                                    juegoBloqueado = true;
                                    btnNext.setEnabled(false);

                                    // Detener el timer ya que encontramos ganador
                                    timerMonitoreo.cancel();
                                });

                                // SOBRESCRIBIR Y VACIAR Ganador.txt de la carpeta seleccionada
                                try (PrintWriter pw = new PrintWriter(ganadorFile)) {
                                    pw.print("");
                                } catch (IOException ex) {
                                    // No mostramos error aquí porque estamos en un timer thread
                                    System.err.println("Error al vaciar Ganador.txt: " + ex.getMessage());
                                }
                            }
                        }
                    } catch (IOException ex) {
                        System.err.println("Error al leer ganador: " + ex.getMessage());
                    }
                }
            }
        }, 0, 2000); // Comprobar cada 2 segundos (2000 ms)
    }

    private void generarNuevoJuego() {
        for (int i = 0; i < numerosSacados.length; i++) {
            numerosSacados[i] = false;
        }
        historial = "";
        
        txtNumeroActual.setText("Número Actual: ---");
        txtPreguns.setText("Las Preguntas Saldran Aqui");
        txtHistorialnums.setText("Historial Numeros");
        txtRespus.setText("Las Respuestas Saldran Aqui");
        
        try (PrintWriter pw = new PrintWriter(new File(carpetaVinculada, "Numeros.txt"))) {
            pw.print("");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al sobrescribir Numeros.txt: " + e.getMessage());
        }
        
        try (PrintWriter pw = new PrintWriter(new File(carpetaVinculada, "Ganador.txt"))) {
            pw.print("");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al sobrescribir Ganador.txt: " + e.getMessage());
        }
        
        juegoBloqueado = false;
        btnNext.setEnabled(true);
        
        JOptionPane.showMessageDialog(this, "✅ Nuevo juego generado. Archivos limpios y botón 'Siguiente' desbloqueado.");
    }

    private void cambiarColoresEcologicos() {
        btnConectar.setBackground(new Color(144, 238, 144));
        btnGenerar.setBackground(new Color(34, 139, 34));
        btnNext.setBackground(new Color(80, 200, 120));
        
        btnConectar.setForeground(Color.BLACK);
        btnGenerar.setForeground(Color.WHITE);
        btnNext.setForeground(Color.WHITE);
    }

    private void inicializarNumeros() {
        arrayNums = new int[100];
        numerosSacados = new boolean[100];
        for (int pos = 0; pos < 100; pos++) {
            arrayNums[pos] = pos + 1;
            numerosSacados[pos] = false;
        }
    }

    private void generarInicio() {
        txtNumeroActual.setText("Número Actual: ---");
        txtPreguns.setText("Las Preguntas Saldran Aqui");
        txtHistorialnums.setText("Historial Numeros");
        txtRespus.setText("Las Respuestas Saldran Aqui");
    }

    private void guardarNumerosArchivo() {
        try (PrintWriter writer = new PrintWriter(new File(carpetaVinculada, "Numeros.txt"))) {
            for (int i = 0; i < numerosSacados.length; i++)
                if (numerosSacados[i]) writer.println(i + 1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar números: " + e.getMessage());
        }
    }

    private void cargarPreguntas() {
        totalPreguntas = 0;
        File archivo = new File(carpetaVinculada, "PreguntasYRespuestas.txt");
        if (!archivo.exists()) {
            try {
                archivo.createNewFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error al crear archivo de preguntas: " + e.getMessage());
                return;
            }
        }

        try (Scanner sc = new Scanner(archivo)) {
            String[] bloque = new String[5];
            int contador = 0;

            while (sc.hasNextLine() && totalPreguntas < preguntasYRespuestas.length) {
                String linea = sc.nextLine();
                bloque[contador] = linea;
                contador++;

                if (contador == 5) {
                    for (int i = 0; i < 5; i++) {
                        preguntasYRespuestas[totalPreguntas][i] = bloque[i];
                    }
                    totalPreguntas++;
                    contador = 0;
                    bloque = new String[5];
                }
            }

            if (contador > 0) {
                for (int i = 0; i < contador; i++) {
                    preguntasYRespuestas[totalPreguntas][i] = bloque[i];
                }
                totalPreguntas++;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar preguntas: " + e.getMessage());
        }
    }

    private void mostrarNumero() {
        try {
            int num;
            do {
                num = (int) (Math.random() * 100) + 1;
            } while (numerosSacados[num - 1]);

            numerosSacados[num - 1] = true;

            historial = "";

            int contador = 0;
            for (int i = 0; i < numerosSacados.length; i++) {
                if (numerosSacados[i]) {
                    contador++;
                    // Salto cada 18 números
                    if (contador % 18 == 0) {
                        historial += (i + 1) + "\n";
                    } else {
                        historial += (i + 1) + ", ";
                    }
                }
            }

            txtNumeroActual.setText("Número Actual: " + num);
            txtHistorialnums.setText("Historial: \n" + historial);

            guardarNumerosArchivo();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar número: " + e.getMessage());
        }
    }

    private void mostrarPreguntaAleatoria() {
        try {
            if (totalPreguntas == 0) {
                txtPreguns.setText("No hay preguntas disponibles.");
                txtRespus.setText("---");
                return;
            }

            int idx = (int) (Math.random() * totalPreguntas);
            String[] arrayString = preguntasYRespuestas[idx];

            txtPreguns.setText(arrayString[0]);

            StringBuilder respuestas = new StringBuilder();
            for (int i = 1; i < arrayString.length; i++) {
                respuestas.append(arrayString[i]).append("\n");
            }

            txtRespus.setText(respuestas.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar pregunta: " + e.getMessage());
        }
    }
}