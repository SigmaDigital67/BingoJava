import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.util.Scanner;

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

    private File carpetaVinculada;
    private int contadorPartidas = 1;
    
    private String ganadorLinea = "";
    private boolean partidaFinalizada = false;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Bombo frame = new Bombo();
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al iniciar la aplicacion: " + e.getMessage());
            }
        });
    }

    public Bombo() {
        try {
            carpetaVinculada = null;

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

            txtNumeroActual = new JTextField("Numero Actual:");
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
            btnNext.setEnabled(false);
            btnNext.setFont(new Font("Tahoma", Font.BOLD, 16));
            contentPane.add(btnNext, BorderLayout.PAGE_END);

            inicializarNumeros();
            generarInicio();
            eventos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al crear la interfaz: " + e.getMessage());
        }
    }

    private void eventos() {
        btnNext.addActionListener(e -> {
            if (juegoBloqueado) {
                JOptionPane.showMessageDialog(this, "Juego bloqueado. Hay un ganador registrado. Presiona 'Generar' para nuevo juego.", "Juego Bloqueado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (rutaCarpetaSeleccionada.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero debes conectar una carpeta.");
                return;
            }
            mostrarNumero();
            cargarPreguntaActual();
            verificarGanadoresAutomatico();
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
                    btnNext.setEnabled(true);
                    
                    cargarContadorPartidas(carpetaElegida);
                    
                    // Verificar si hay ganadores existentes al conectar
                    verificarGanadoresAlConectar(carpetaElegida);
                    
                    File carpetaRed = new File("\\\\192.168.0.37\\Almingo\\");
                    if (carpetaRed.exists() && carpetaRed.isDirectory()) {
                        carpetaVinculada = carpetaRed;
                        JOptionPane.showMessageDialog(this, 
                            "Conectado a: " + rutaCarpetaSeleccionada + "\nPreguntas disponibles", 
                            "Conexion Exitosa", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Conectado a: " + rutaCarpetaSeleccionada + "\nPreguntas no disponibles", 
                            "Conexion Exitosa", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "La ruta no es valida o no es una carpeta.");
                }
            }
        });

        btnGenerar.addActionListener(e -> {
            if (rutaCarpetaSeleccionada.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primero debes seleccionar una carpeta con 'Conectar'.");
                return;
            }
            generarNuevoJuego();
            cambiarColoresEcologicos();
        });
    }

    private void verificarGanadoresAlConectar(File carpeta) {
        // Verificar si ya hay un ganador de bingo al conectar
        File archivoBingo = new File(carpeta, "Ganador.txt");
        if (archivoBingo.exists()) {
            try {
                Scanner sc = new Scanner(archivoBingo);
                if (sc.hasNextLine()) {
                    String ganadorBingo = sc.nextLine().trim();
                    if (!ganadorBingo.isEmpty()) {
                        partidaFinalizada = true;
                        juegoBloqueado = true;
                        
                        // Verificar también ganador de línea
                        File archivoLinea = new File(carpeta, "GanadorLinea.txt");
                        if (archivoLinea.exists()) {
                            Scanner scLinea = new Scanner(archivoLinea);
                            if (scLinea.hasNextLine()) {
                                ganadorLinea = scLinea.nextLine().trim();
                            }
                            scLinea.close();
                        }
                        
                        String mensaje = "PARTIDA YA FINALIZADA!\n\n";
                        if (!ganadorLinea.isEmpty()) {
                            mensaje += "Ganador de Linea: " + ganadorLinea + "\n";
                        }
                        mensaje += "Ganador de Bingo: " + ganadorBingo + "\n\n";
                        mensaje += "El juego está bloqueado. Presiona 'Generar' para nuevo juego.";
                        
                        JOptionPane.showMessageDialog(
                                this,
                                mensaje,
                                "PARTIDA COMPLETADA!",
                                JOptionPane.WARNING_MESSAGE
                        );
                        
                        btnNext.setEnabled(false);
                    }
                }
                sc.close();
            } catch (IOException ex) {
                System.err.println("Error al leer Ganador.txt al conectar: " + ex.getMessage());
            }
        }
    }

    private void cargarContadorPartidas(File carpeta) {
        File archivoGanadores = new File(carpeta, "GanadoresAnteriores.txt");
        if (archivoGanadores.exists()) {
            try {
                Scanner sc = new Scanner(archivoGanadores);
                int partidasCount = 0;
                while (sc.hasNextLine()) {
                    String linea = sc.nextLine().trim();
                    if (linea.startsWith("=== Partida")) {
                        partidasCount++;
                    }
                }
                sc.close();
                contadorPartidas = partidasCount + 1;
            } catch (IOException e) {
                contadorPartidas = 1;
            }
        } else {
            contadorPartidas = 1;
        }
    }

    private void cargarPreguntaActual() {
        if (carpetaVinculada != null) {
            File archivoPregunta = new File(carpetaVinculada, "PreguntaActual.txt");
            if (archivoPregunta.exists() && archivoPregunta.length() > 0) {
                try {
                    // Abrir y leer el archivo
                    Scanner sc = new Scanner(archivoPregunta, "UTF-8");
                    java.util.ArrayList<String> lineas = new java.util.ArrayList<>();
                    while (sc.hasNextLine()) {
                        String linea = sc.nextLine().trim();
                        if (!linea.isEmpty()) {
                            lineas.add(linea);
                        }
                    }
                    sc.close();
                    
                    if (lineas.size() >= 4) { // Necesitamos al menos 4 líneas (pregunta + 3 respuestas)
                        mostrarPreguntaYRespuestasDesdeArchivo(lineas);
                        
                        // Limpiar el archivo después de leerlo
                        PrintWriter pw = new PrintWriter(archivoPregunta, "UTF-8");
                        pw.print("");
                        pw.close();
                    }
                    // Si no hay 4 líneas, simplemente no hacemos nada (sin warnings)
                } catch (IOException ex) {
                    // Error silencioso - no mostrar diálogo
                    System.err.println("Error al leer PreguntaActual.txt: " + ex.getMessage());
                }
            }
            // Si el archivo no existe o está vacío, no hacer nada (comportamiento normal)
        }
    }

    private void mostrarPreguntaYRespuestasDesdeArchivo(java.util.ArrayList<String> lineas) {
        try {
            // Primera línea va a PREGUNTA
            String pregunta = lineas.get(0);
            txtPreguns.setText(pregunta);
            
            // Líneas 2, 3 y 4 van a RESPUESTAS (con la primera marcada como correcta)
            StringBuilder respuestas = new StringBuilder();
            
            // Primera respuesta (línea 2) con "(correcta)"
            respuestas.append(lineas.get(1)).append(" (correcta)\n");
            
            // Segunda respuesta (línea 3)
            if (lineas.size() > 2) {
                respuestas.append(lineas.get(2)).append("\n");
            }
            
            // Tercera respuesta (línea 4)
            if (lineas.size() > 3) {
                respuestas.append(lineas.get(3));
            }
            
            txtRespus.setText(respuestas.toString().trim());
            
        } catch (Exception e) {
            // Error silencioso - solo en consola
            System.err.println("Error al procesar la pregunta: " + e.getMessage());
        }
    }

    private void verificarGanadoresAutomatico() {
        if (rutaCarpetaSeleccionada.isEmpty() || partidaFinalizada) {
            return;
        }
        
        File carpetaSeleccionada = new File(rutaCarpetaSeleccionada);
        
        File archivoLinea = new File(carpetaSeleccionada, "GanadorLinea.txt");
        if (archivoLinea.exists() && ganadorLinea.isEmpty()) {
            try {
                Scanner sc = new Scanner(archivoLinea);
                if (sc.hasNextLine()) {
                    String nombre = sc.nextLine().trim();
                    if (!nombre.isEmpty()) {
                        ganadorLinea = nombre;
                        
                        JOptionPane.showMessageDialog(
                                this,
                                "LINEA COMPLETADA! Ganador: " + nombre,
                                "Tenemos Ganador de LINEA!",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        PrintWriter pw = new PrintWriter(archivoLinea);
                        pw.print("");
                        pw.close();
                    }
                }
                sc.close();
            } catch (IOException ex) {
                System.err.println("Error al leer GanadorLinea.txt: " + ex.getMessage());
            }
        }
        
        File archivoBingo = new File(carpetaSeleccionada, "Ganador.txt");
        if (archivoBingo.exists() && !partidaFinalizada) {
            try {
                Scanner sc = new Scanner(archivoBingo);
                if (sc.hasNextLine()) {
                    String ganadorBingo = sc.nextLine().trim();
                    if (!ganadorBingo.isEmpty()) {
                        partidaFinalizada = true;
                        juegoBloqueado = true;
                        
                        guardarPartidaEnHistorial(carpetaSeleccionada, ganadorLinea, ganadorBingo);
                        
                        String mensaje = "PARTIDA FINALIZADA!\n\n";
                        if (!ganadorLinea.isEmpty()) {
                            mensaje += "Ganador de Linea: " + ganadorLinea + "\n";
                        }
                        mensaje += "Ganador de Bingo: " + ganadorBingo + "\n\n";
                        mensaje += "El juego se ha bloqueado. Presiona 'Generar' para nuevo juego.";
                        
                        JOptionPane.showMessageDialog(
                                this,
                                mensaje,
                                "PARTIDA COMPLETADA!",
                                JOptionPane.WARNING_MESSAGE
                        );
                        
                        btnNext.setEnabled(false);
                        
                        PrintWriter pw = new PrintWriter(archivoBingo);
                        pw.print("");
                        pw.close();
                    }
                }
                sc.close();
            } catch (IOException ex) {
                System.err.println("Error al leer Ganador.txt: " + ex.getMessage());
            }
        }
    }

    private void guardarPartidaEnHistorial(File carpeta, String ganadorLinea, String ganadorBingo) {
        File ganadoresAnteriores = new File(carpeta, "GanadoresAnteriores.txt");
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(ganadoresAnteriores, true));
            pw.println("=== Partida " + contadorPartidas + " ===");
            
            if (!ganadorLinea.isEmpty()) {
                pw.println("LINEA: " + ganadorLinea);
            }
            
            pw.println("BINGO: " + ganadorBingo);
            pw.println();
            
            contadorPartidas++;
            pw.close();
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error al guardar partida en historial: " + ex.getMessage());
        }
    }

    private void generarNuevoJuego() {
        if (rutaCarpetaSeleccionada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero debes seleccionar una carpeta con 'Conectar'.");
            return;
        }
        
        File carpetaSeleccionada = new File(rutaCarpetaSeleccionada);
        
        // Reiniciar arrays de números
        for (int i = 0; i < numerosSacados.length; i++) {
            numerosSacados[i] = false;
        }
        historial = "";
        
        // Reiniciar variables de juego
        ganadorLinea = "";
        partidaFinalizada = false;
        juegoBloqueado = false;
        
        // Limpiar interfaz
        txtNumeroActual.setText("Numero Actual: ---");
        txtPreguns.setText("Las Preguntas Saldran Aqui");
        txtHistorialnums.setText("Historial Numeros");
        txtRespus.setText("Las Respuestas Saldran Aqui");
        
        try {
            // LIMPIAR TODOS LOS ARCHIVOS DE JUEGO (incluyendo Ganador.txt)
            PrintWriter pw1 = new PrintWriter(new File(carpetaSeleccionada, "Numeros.txt"));
            pw1.print("");
            pw1.close();
            
            PrintWriter pw2 = new PrintWriter(new File(carpetaSeleccionada, "NumeroActual.txt"));
            pw2.print("");
            pw2.close();
            
            // AHORA SÍ LIMPIAR Ganador.txt
            PrintWriter pw3 = new PrintWriter(new File(carpetaSeleccionada, "Ganador.txt"));
            pw3.print("");
            pw3.close();
            
            PrintWriter pw4 = new PrintWriter(new File(carpetaSeleccionada, "GanadorLinea.txt"));
            pw4.print("");
            pw4.close();
            
            System.out.println("Todos los archivos de juego limpiados en: " + carpetaSeleccionada.getAbsolutePath());
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al limpiar archivos: " + e.getMessage());
        }
        
        // Reactivar botones
        btnNext.setEnabled(true);
        btnGenerar.setEnabled(true);
        
        JOptionPane.showMessageDialog(this, 
            "Nuevo juego generado.\n" +
            "Todos los archivos limpiados en: " + rutaCarpetaSeleccionada + "\n" +
            "Boton 'Siguiente' desbloqueado", 
            "Juego Reiniciado", 
            JOptionPane.INFORMATION_MESSAGE);
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
        txtNumeroActual.setText("Numero Actual: ---");
        txtPreguns.setText("Las Preguntas Saldran Aqui");
        txtHistorialnums.setText("Historial Numeros");
        txtRespus.setText("Las Respuestas Saldran Aqui");
    }

    private void guardarNumerosArchivo() {
        if (rutaCarpetaSeleccionada.isEmpty()) return;
        
        File carpetaSeleccionada = new File(rutaCarpetaSeleccionada);
        try {
            PrintWriter writer = new PrintWriter(new File(carpetaSeleccionada, "Numeros.txt"));
            for (int i = 0; i < numerosSacados.length; i++)
                if (numerosSacados[i]) writer.println(i + 1);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error al guardar numeros: " + e.getMessage());
        }
    }

    private void guardarNumeroActual(int numero) {
        if (rutaCarpetaSeleccionada.isEmpty()) return;
        
        File carpetaSeleccionada = new File(rutaCarpetaSeleccionada);
        try {
            PrintWriter writer = new PrintWriter(new File(carpetaSeleccionada, "NumeroActual.txt"));
            writer.print(numero);
            writer.close();
        } catch (IOException e) {
            System.err.println("Error al guardar numero actual: " + e.getMessage());
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
                    if (contador % 18 == 0) {
                        historial += (i + 1) + "\n";
                    } else {
                        historial += (i + 1) + ", ";
                    }
                }
            }

            txtNumeroActual.setText("Numero Actual: " + num);
            txtHistorialnums.setText("Historial: \n" + historial);

            guardarNumerosArchivo();
            guardarNumeroActual(num);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar numero: " + e.getMessage());
        }
    }
}