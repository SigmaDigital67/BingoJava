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

	private int[] arrayNums;
	private boolean[] numerosSacados;
	private String historial = "";

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
			panel.setLayout(new GridLayout(0, 2, 0, 0));

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

			btnNext = new JButton("Siguiente");
			contentPane.add(btnNext, BorderLayout.SOUTH);

			inicializarNumeros();
			cargarPreguntas();
			generarInicio();
			eventos();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error al crear la interfaz: " + e.getMessage());
		}		
	}								/////----***CONSTRUCTO***----\\\\\/////----***CONSTRUCTO***----\\\\\/////----***CONSTRUCTO***----\\\\\
	private void eventos() {
		btnNext.addActionListener(e -> {
			mostrarNumero();
			mostrarPreguntaAleatoria();
		});
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

			// si quedan líneas sueltas, las guardamos también
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
	                historial += (i + 1);
	                if (contador % 14 == 0) historial += "\n";	//ENTERS CAMBIAR EL NUMERO DEL PORCENTAJE\\
	                else historial += ", ";
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

	        txtPreguns.setText(arrayString[0]); // sin checks

	        String respuestas = "";
	        for (int i = 1; i < arrayString.length; i++) {
	            respuestas += arrayString[i] + "\n";
	        }

	        txtRespus.setText(respuestas);

	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null, "Error al mostrar pregunta: " + e.getMessage());
	    }
	}

	
}
