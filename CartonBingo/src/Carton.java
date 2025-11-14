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
    private JButton btnPartida, btnNumeroActual, btnAutocompletar, btnColor;
    private JButton[] arrayBtn;
    private int[] arrayAleatorio;
    private Color colorMarcado = new Color(173, 216, 230);
    private Color colorEsperando = new Color(255, 102, 102);
    private Color colorLineaCompletada = new Color(255, 255, 153);
    private File carpetaActual, archivoNumeros, archivoNumeroActual;
    private boolean juegoTerminado = false;
    private boolean[] lineasCompletadas = new boolean[FILAS];
    private boolean warningMostrado = false;
    private javax.swing.Timer timerActualizacion;

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

        btnNumeroActual = new JButton("-");
        btnAutocompletar = new JButton("Autocompletar");
        btnPartida = new JButton("Nueva Partida");
        btnColor = new JButton("Números anteriores");

        btnNumeroActual.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnNumeroActual.setBackground(Color.WHITE);
        btnNumeroActual.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.add(lblB);
        panel.add(btnPartida);
        panel.add(lblI);
        panel.add(btnNumeroActual);
        panel.add(lblN);
        panel.add(btnColor);
        panel.add(lblG);
        panel.add(btnAutocompletar);
        panel.add(lblO);

        arrayBtn = new JButton[TOTAL];
        for (int i = 0; i < TOTAL; i++) {
            arrayBtn[i] = new JButton("");
            arrayBtn[i].setFont(new Font("SansSerif", Font.BOLD, 16));
            panel.add(arrayBtn[i]);
        }

        txtNombre = new JTextField("Tu Nombre");
        contentPane.add(txtNombre, BorderLayout.NORTH);

        arrayAleatorio = new int[TOTAL];

        eventos();
        llenarArray();
        generarCarton();
        
        pedirRutaCarpeta();
        verificarCarpeta(); // ← Nueva línea para depuración
        iniciarActualizacionAutomatica();
        verificarGanadorExistente();
    }

    private void verificarCarpeta() {
        if (carpetaActual != null) {
            System.out.println("=== INFORMACIÓN DE CARPETA ===");
            System.out.println("Carpeta actual: " + carpetaActual.getAbsolutePath());
            System.out.println("Archivos en carpeta:");
            File[] archivos = carpetaActual.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    System.out.println(" - " + archivo.getName() + " (" + archivo.length() + " bytes)");
                }
            }
            System.out.println("=== FIN INFORMACIÓN ===");
        } else {
            System.out.println("Carpeta actual es NULL - No conectada");
        }
    }

    private void iniciarActualizacionAutomatica() {
        timerActualizacion = new javax.swing.Timer(2000, e -> {
            if (archivoNumeroActual != null && archivoNumeroActual.exists()) {
                actualizarNumeroActualDesdeArchivo();
            }
            
            if (carpetaActual != null && !warningMostrado) {
                File archivoGanador = new File(carpetaActual, "Ganador.txt");
                if (archivoGanador.exists() && archivoGanador.length() > 0) {
                    mostrarGanadorUnaVez();
                }
            }
        });
        timerActualizacion.start();
    }

    private void pedirRutaCarpeta() {
        String ruta = JOptionPane.showInputDialog(this, 
            "Escribe la ruta de la carpeta compartida:", 
            "Conectar a Carpeta", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (ruta != null && !ruta.isEmpty()) {
            File carpeta = new File(ruta);
            if (carpeta.isDirectory()) {
                carpetaActual = carpeta;
                archivoNumeros = new File(carpeta, "Numeros.txt");
                archivoNumeroActual = new File(carpeta, "NumeroActual.txt");
                
                if (archivoNumeroActual.exists()) {
                    actualizarNumeroActualDesdeArchivo();
                }
                verificarGanadorExistente();
                JOptionPane.showMessageDialog(this, 
                    "Conectado a: " + ruta, 
                    "Conexión Exitosa", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "La ruta no es una carpeta válida", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarNumeroActualDesdeArchivo() {
        if (archivoNumeroActual == null || !archivoNumeroActual.exists()) {
            btnNumeroActual.setText("-");
            return;
        }
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(archivoNumeroActual));
            String contenido = reader.readLine();
            reader.close();
            
            if (contenido != null && !contenido.trim().isEmpty()) {
                if (!contenido.trim().equals(btnNumeroActual.getText())) {
                    btnNumeroActual.setText(contenido.trim());
                }
            } else {
                btnNumeroActual.setText("-");
            }
        } catch (Exception ex) {
            btnNumeroActual.setText("-");
        }
    }

    private void verificarGanadorExistente() {
        if (carpetaActual != null) {
            File archivoGanador = new File(carpetaActual, "Ganador.txt");
            if (archivoGanador.exists() && archivoGanador.length() > 0 && !warningMostrado) {
                mostrarGanadorUnaVez();
            }
        }
    }

    private void eventos() {
        btnPartida.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres empezar una nueva partida?\n\n" +
                "Se generará un nuevo cartón y se reiniciará el juego.",
                "Confirmar Nueva Partida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                llenarArray();
                generarCarton();
                juegoTerminado = false;
                warningMostrado = false;
                for (int i = 0; i < FILAS; i++) {
                    lineasCompletadas[i] = false;
                }
                btnNumeroActual.setText("-");
                
                JOptionPane.showMessageDialog(this,
                    "Nueva partida iniciada\nSe ha generado un nuevo cartón.",
                    "Nueva Partida",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnColor.addActionListener(e -> {
            if (archivoNumeros == null || !archivoNumeros.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "No hay archivo de números conectado", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int[] numeros = leerNumerosArchivo();
            if (numeros.length == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No hay números anteriores", 
                    "Números anteriores", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            StringBuilder texto = new StringBuilder("Números anteriores:\n\n");
            for (int i = 0; i < numeros.length; i++) {
                texto.append(numeros[i]);
                if (i < numeros.length - 1) {
                    if ((i + 1) % 15 == 0) texto.append("\n");
                    else texto.append(" , ");
                }
            }
            JOptionPane.showMessageDialog(this, texto.toString(), "Números anteriores", JOptionPane.INFORMATION_MESSAGE);
        });

        btnNumeroActual.addActionListener(e -> {
            // No hace nada - solo muestra el número
        });

        btnAutocompletar.addActionListener(e -> {
            if (archivoNumeros == null || !archivoNumeros.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "No hay archivo de números conectado", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Leer directamente del archivo y marcar los números
            int[] numerosActuales = leerNumerosArchivo();
            
            for (int i = 0; i < arrayBtn.length; i++) {
                // No marcar si ya está en amarillo (línea completada)
                if (arrayBtn[i].getBackground().equals(colorLineaCompletada)) {
                    continue;
                }
                
                String textoBoton = arrayBtn[i].getText();
                if (textoBoton.isEmpty()) continue;
                
                int numBoton = convertirTextoANumero(textoBoton);
                
                if (contieneNumero(numerosActuales, numBoton) && 
                    !arrayBtn[i].getBackground().equals(Color.GRAY)) {
                    arrayBtn[i].setBackground(colorMarcado);
                }
            }
            
            comprobarLineas();
            comprobarGanador();
        });

        for (int i = 0; i < arrayBtn.length; i++) {
            final int index = i;
            arrayBtn[i].addActionListener(e -> {
                if (juegoTerminado) return;
                
                // Verificar si el botón está en gris (deshabilitado) o en amarillo (línea completada)
                if (arrayBtn[index].getBackground().equals(Color.GRAY) || 
                    arrayBtn[index].getBackground().equals(colorLineaCompletada)) {
                    return;
                }
                
                String textoBoton = arrayBtn[index].getText();
                if (textoBoton.isEmpty()) return;
                
                int num = convertirTextoANumero(textoBoton);
                
                // Verificar contra los números actuales del archivo
                int[] numerosActuales = leerNumerosArchivo();
                
                if (contieneNumero(numerosActuales, num)) {
                    arrayBtn[index].setBackground(colorMarcado);
                } else {
                    arrayBtn[index].setBackground(colorEsperando);
                }
                comprobarLineas();
                comprobarGanador();
            });
        }
    }

    private void mostrarGanadorUnaVez() {
        if (warningMostrado) return;
        
        String nombreGanador = "Otro jugador";
        String nombreGanadorLinea = "Nadie";
        
        if (carpetaActual != null) {
            File archivoGanador = new File(carpetaActual, "Ganador.txt");
            File archivoGanadorLinea = new File(carpetaActual, "GanadorLinea.txt");
            
            // Leer ganador del bingo
            if (archivoGanador.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(archivoGanador));
                    nombreGanador = reader.readLine();
                    reader.close();
                    if (nombreGanador == null || nombreGanador.isEmpty()) {
                        nombreGanador = "Otro jugador";
                    }
                } catch (Exception e) {
                    nombreGanador = "Otro jugador";
                }
            }
            
            // Leer ganador de línea
            if (archivoGanadorLinea.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(archivoGanadorLinea));
                    nombreGanadorLinea = reader.readLine();
                    reader.close();
                    if (nombreGanadorLinea == null || nombreGanadorLinea.isEmpty()) {
                        nombreGanadorLinea = "Nadie";
                    }
                } catch (Exception e) {
                    nombreGanadorLinea = "Nadie";
                }
            }
            
            // Guardar en historial antes de limpiar
            guardarEnHistorial(nombreGanador, nombreGanadorLinea);
            
            // Limpiar archivos
            limpiarArchivosGanadores();
        }
        
        // Mostrar mensaje con ambos ganadores
        JOptionPane.showMessageDialog(this, 
            "LA PARTIDA HA TERMINADO\n\n" +
            "GANADOR DEL BINGO: " + nombreGanador + "\n" +
            "GANADOR DE LINEA: " + nombreGanadorLinea + "\n\n" +
            "Felicidades a los ganadores\nGracias a todos por participar.", 
            "PARTIDA FINALIZADA", 
            JOptionPane.WARNING_MESSAGE);
        
        warningMostrado = true;
        juegoTerminado = true;
    }

    private void guardarEnHistorial(String ganadorBingo, String ganadorLinea) {
        if (carpetaActual == null) return;
        
        File archivoHistorial = new File(carpetaActual, "GanadoresAnteriores.txt");
        try {
            FileWriter fw = new FileWriter(archivoHistorial, true);
            String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            fw.write("Partida: " + fecha + "\n");
            fw.write("Ganador Bingo: " + ganadorBingo + "\n");
            fw.write("Ganador Linea: " + ganadorLinea + "\n");
            fw.write("------------------------\n");
            fw.close();
        } catch (IOException ignored) {}
    }

    private void limpiarArchivosGanadores() {
        if (carpetaActual == null) return;
        
        try {
            // Limpiar Ganador.txt
            File archivoGanador = new File(carpetaActual, "Ganador.txt");
            if (archivoGanador.exists()) {
                FileWriter fw = new FileWriter(archivoGanador, false);
                fw.write("");
                fw.close();
            }
            
            // Limpiar GanadorLinea.txt
            File archivoGanadorLinea = new File(carpetaActual, "GanadorLinea.txt");
            if (archivoGanadorLinea.exists()) {
                FileWriter fw = new FileWriter(archivoGanadorLinea, false);
                fw.write("");
                fw.close();
            }
        } catch (IOException ignored) {}
    }

    private void comprobarLineas() {
        // Verificar primero si ya hay ganador de línea
        boolean hayGanadorLinea = !archivoGanadorLineaEstaVacio();
        
        for (int fila = 0; fila < FILAS; fila++) {
            if (lineasCompletadas[fila]) continue;
            
            boolean lineaCompleta = true;
            for (int columna = 0; columna < COLUMNAS; columna++) {
                int indice = fila * COLUMNAS + columna;
                if (!arrayBtn[indice].getBackground().equals(colorMarcado) && 
                    !arrayBtn[indice].getBackground().equals(Color.GRAY)) {
                    lineaCompleta = false;
                    break;
                }
            }
            
            if (lineaCompleta && !lineasCompletadas[fila]) {
                lineasCompletadas[fila] = true;
                
                if (hayGanadorLinea) {
                    // Si YA hay ganador de línea, solo mostrar mensaje (NO pintar amarillo)
                    JOptionPane.showMessageDialog(this, 
                        "Linea " + (fila + 1) + " completada!", 
                        "Linea!", 
                        JOptionPane.WARNING_MESSAGE);
                } else {
                    // Si NO hay ganador de línea, mostrar pregunta
                    mostrarPreguntaYRespuesta(fila + 1);
                }
            }
        }
    }

    private void mostrarPreguntaYRespuesta(int numeroLinea) {
        if (carpetaActual == null) return;
        
        File archivoPreguntas = new File(carpetaActual, "PreguntasYRespuestas.txt");
        if (!archivoPreguntas.exists()) {
            JOptionPane.showMessageDialog(this, "Linea " + numeroLinea + " completada!", "Linea!", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Leer todas las líneas del archivo
            BufferedReader reader = new BufferedReader(new FileReader(archivoPreguntas));
            ArrayList<String> todasLasLineas = new ArrayList<>();
            String linea;
            while ((linea = reader.readLine()) != null) {
                todasLasLineas.add(linea);
            }
            reader.close();
            
            // Contar cuántos bloques completos de 5 líneas hay
            int totalBloques = todasLasLineas.size() / 5;
            if (totalBloques == 0) {
                JOptionPane.showMessageDialog(this, "Linea " + numeroLinea + " completada!", "Linea!", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Elegir un bloque aleatorio
            int bloqueAleatorio = (int) (Math.random() * totalBloques);
            int inicioBloque = bloqueAleatorio * 5;
            
            // Obtener las 5 líneas del bloque seleccionado
            String pregunta = todasLasLineas.get(inicioBloque).trim();
            String respuestaA = todasLasLineas.get(inicioBloque + 1).trim();
            String respuestaB = todasLasLineas.get(inicioBloque + 2).trim();
            String respuestaC = todasLasLineas.get(inicioBloque + 3).trim();
            
            // Crear el mensaje con la pregunta
            String mensaje = "Linea " + numeroLinea + " completada!\n\n" + pregunta + "\n\n";
            
            // Crear botones para las respuestas
            JButton btnA = new JButton(respuestaA);
            JButton btnB = new JButton(respuestaB);
            JButton btnC = new JButton(respuestaC);
            
            // Mezclar aleatoriamente el orden de los botones
            JButton[] botonesMezclados = {btnA, btnB, btnC};
            for (int i = botonesMezclados.length - 1; i > 0; i--) {
                int j = (int) (Math.random() * (i + 1));
                JButton temp = botonesMezclados[i];
                botonesMezclados[i] = botonesMezclados[j];
                botonesMezclados[j] = temp;
            }
            
            // Panel para los botones
            JPanel panelBotones = new JPanel(new GridLayout(1, 3, 5, 5));
            panelBotones.add(botonesMezclados[0]);
            panelBotones.add(botonesMezclados[1]);
            panelBotones.add(botonesMezclados[2]);
            
            // Dialogo personalizado
            JDialog dialog = new JDialog(this, "Pregunta para la Linea " + numeroLinea, true);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JLabel(mensaje), BorderLayout.NORTH);
            dialog.add(panelBotones, BorderLayout.CENTER);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            
            // Determinar qué botón es el correcto después de mezclar
            final boolean esCorrectaBtn1 = botonesMezclados[0].getText().equals(respuestaA);
            final boolean esCorrectaBtn2 = botonesMezclados[1].getText().equals(respuestaA);
            final boolean esCorrectaBtn3 = botonesMezclados[2].getText().equals(respuestaA);
            
            // Acciones de los botones
            botonesMezclados[0].addActionListener(e -> {
                dialog.dispose();
                if (esCorrectaBtn1) {
                    // Respuesta correcta - guardar en GanadorLinea.txt
                    guardarGanadorLinea(numeroLinea);
                    JOptionPane.showMessageDialog(this, "Has ganado la linea!", "Felicidades!", JOptionPane.INFORMATION_MESSAGE);
                    // SOLO pintar de amarillo si no había ganador previo
                    if (archivoGanadorLineaEstaVacio()) {
                        marcarLineaComoCompletada(numeroLinea);
                    }
                } else {
                    // Respuesta incorrecta - no guardar nada
                    JOptionPane.showMessageDialog(this, "Has perdido la linea", "Incorrecto", JOptionPane.WARNING_MESSAGE);
                }
            });
            
            botonesMezclados[1].addActionListener(e -> {
                dialog.dispose();
                if (esCorrectaBtn2) {
                    guardarGanadorLinea(numeroLinea);
                    JOptionPane.showMessageDialog(this, "Has ganado la linea!", "Felicidades!", JOptionPane.INFORMATION_MESSAGE);
                    // SOLO pintar de amarillo si no había ganador previo
                    if (archivoGanadorLineaEstaVacio()) {
                        marcarLineaComoCompletada(numeroLinea);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Has perdido la linea", "Incorrecto", JOptionPane.WARNING_MESSAGE);
                }
            });
            
            botonesMezclados[2].addActionListener(e -> {
                dialog.dispose();
                if (esCorrectaBtn3) {
                    guardarGanadorLinea(numeroLinea);
                    JOptionPane.showMessageDialog(this, "Has ganado la linea!", "Felicidades!", JOptionPane.INFORMATION_MESSAGE);
                    // SOLO pintar de amarillo si no había ganador previo
                    if (archivoGanadorLineaEstaVacio()) {
                        marcarLineaComoCompletada(numeroLinea);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Has perdido la linea", "Incorrecto", JOptionPane.WARNING_MESSAGE);
                }
            });
            
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Linea " + numeroLinea + " completada!", "Linea!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean archivoGanadorLineaEstaVacio() {
        if (carpetaActual == null) return true;
        
        File archivoLinea = new File(carpetaActual, "GanadorLinea.txt");
        if (!archivoLinea.exists()) return true;
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(archivoLinea));
            String contenido = reader.readLine();
            reader.close();
            return contenido == null || contenido.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    private void guardarGanadorLinea(int numeroLinea) {
        if (carpetaActual == null) return;
        
        String nombre = txtNombre.getText();
        if (nombre.isEmpty()) nombre = "Jefesito";
        
        File archivoLinea = new File(carpetaActual, "GanadorLinea.txt");
        try {
            FileWriter fw = new FileWriter(archivoLinea, false);
            fw.write(nombre);
            fw.close();
        } catch (IOException ignored) {}
    }

    private void marcarLineaComoCompletada(int numeroLinea) {
        int fila = numeroLinea - 1;
        
        for (int columna = 0; columna < COLUMNAS; columna++) {
            int indice = fila * COLUMNAS + columna;
            if (indice < TOTAL && arrayBtn[indice].isEnabled() && 
                !arrayBtn[indice].getBackground().equals(Color.GRAY)) {
                arrayBtn[indice].setBackground(colorLineaCompletada);
            }
        }
    }

    private int convertirTextoANumero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int[] leerNumerosArchivo() {
        if (archivoNumeros == null || !archivoNumeros.exists()) {
            return new int[0];
        }
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(archivoNumeros));
            ArrayList<Integer> numerosList = new ArrayList<>();
            String linea;
            
            while ((linea = reader.readLine()) != null) {
                String numeroLimpio = linea.trim();
                if (!numeroLimpio.isEmpty()) {
                    try {
                        int numero = Integer.parseInt(numeroLimpio);
                        numerosList.add(numero);
                    } catch (NumberFormatException e) {
                        // Ignorar líneas que no sean números
                    }
                }
            }
            reader.close();
            
            // Convertir ArrayList a array int[]
            int[] resultado = new int[numerosList.size()];
            for (int i = 0; i < numerosList.size(); i++) {
                resultado[i] = numerosList.get(i);
            }
            return resultado;
            
        } catch (Exception ex) {
            return new int[0];
        }
    }

    private boolean contieneNumero(int[] array, int numero) {
        for (int value : array) {
            if (value == numero) {
                return true;
            }
        }
        return false;
    }

    private void comprobarGanador() {
        boolean todos = true;
        for (int i = 0; i < arrayBtn.length; i++) {
            if (!arrayBtn[i].getBackground().equals(colorMarcado) && 
                !arrayBtn[i].getBackground().equals(Color.GRAY)) {
                todos = false;
                break;
            }
        }
        if (todos) {
            juegoTerminado = true;
            String nombre = txtNombre.getText();
            if (nombre.isEmpty()) nombre = "Jefesito";
            
            if (carpetaActual != null) {
                File ganador = new File(carpetaActual, "Ganador.txt");
                try {
                    FileWriter fw = new FileWriter(ganador, false);
                    fw.write(nombre);
                    fw.close();
                    
                    // Mensaje de confirmación
                    JOptionPane.showMessageDialog(this, 
                        "¡BINGO COMPLETADO!\n" +
                        "Ganador: " + nombre + "\n" +
                        "Archivo guardado en: " + ganador.getAbsolutePath(),
                        "¡FELICIDADES!",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al guardar ganador: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No hay carpeta conectada. No se pudo guardar el ganador.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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
        
        for (int i = 0; i < TOTAL; i++) {
            arrayBtn[i].setText(String.valueOf(arrayAleatorio[i]));
            arrayBtn[i].setBackground(Color.WHITE);
            arrayBtn[i].setEnabled(true);
        }
    }

    private void generarCarton() {
        for (int i = 0; i < arrayBtn.length; i++) {
            arrayBtn[i].setBackground(Color.WHITE);
        }
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
        
        for (int i = 0; i < FILAS; i++) {
            lineasCompletadas[i] = false;
        }
    }
}