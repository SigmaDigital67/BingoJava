import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;

public class Carton extends JFrame {

    private static final int TOTAL = 27;
    private static final int FILAS = 3;
    private static final int COLUMNAS = 9;
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtNombre;
    private JTextField txtNums;
    private JButton btnPartida;
    private JButton btnNum1;
    private JButton btnNum2;
    private JButton btnNum3;
    private JButton btnNum4;
    private JButton btnNum5;
    private JButton btnNum6;
    private JButton btnNum7;
    private JButton btnNum8;
    private JButton btnNum9;
    private JButton btnNum10;
    private JButton btnNum11;
    private JButton btnNum12;
    private JButton btnNum13;
    private JButton btnNum14;
    private JButton btnNum15;
    private JButton btnB;
    private JButton btnI;
    private JButton btnN;
    private JButton btnG;
    private JButton btnO;
    private JButton []arrayBtn;
    private int arrayAleatorio[];
    private int arrayConexion[];
    private JButton btnColor;
    private JColorChooser dlgColor;
    private JTextField textField;	
    private JTextField textField_1;
    private JButton btnNum16;
    private JButton btnNum17;
    private JButton btnNum18;
    private JButton btnNum19;
    private JButton btnNum20;
    private JButton btnNum21;
    private JButton btnNum22;
    private JButton btnNum23;
    private JButton btnNum24;
    private JButton btnNum25;
    private JButton btnNum26;
    private JButton btnNum27;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Carton frame = new Carton();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Carton() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 850, 600);
        contentPane = new JPanel();
        contentPane.setLocation(new Point(1, 1));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.CENTER);
        panel.setLayout(new GridLayout(0, 9, 0, 0));

        btnB = new JButton("B");
        panel.add(btnB);

        textField = new JTextField();
        panel.add(textField);
        textField.setColumns(10);

        btnI = new JButton("I");
        panel.add(btnI);

        btnPartida = new JButton("Nueva Partida");
        btnPartida.setEnabled(false);	//No le pueda dar al iniciar 
        panel.add(btnPartida);

        btnN = new JButton("N");
        panel.add(btnN);

        btnColor = new JButton("CambiarColor");
        panel.add(btnColor);

        btnG = new JButton("G");
        panel.add(btnG);

        textField_1 = new JTextField();
        panel.add(textField_1);
        textField_1.setColumns(10);

        btnO = new JButton("O");
        panel.add(btnO);

        btnNum1 = new JButton("1");
        panel.add(btnNum1);
        btnNum2 = new JButton("2");
        panel.add(btnNum2);
        btnNum3 = new JButton("3");
        panel.add(btnNum3);
        btnNum4 = new JButton("4");
        panel.add(btnNum4);
        btnNum5 = new JButton("5");
        panel.add(btnNum5);
        btnNum6 = new JButton("6");
        panel.add(btnNum6);
        btnNum7 = new JButton("7");
        panel.add(btnNum7);
        btnNum8 = new JButton("8");
        panel.add(btnNum8);
        btnNum9 = new JButton("9");
        panel.add(btnNum9);
        btnNum10 = new JButton("10");
        panel.add(btnNum10);
        btnNum11 = new JButton("11");
        panel.add(btnNum11);
        btnNum12 = new JButton("12");
        panel.add(btnNum12);
        btnNum13 = new JButton("13");
        panel.add(btnNum13);
        btnNum14 = new JButton("14");
        panel.add(btnNum14);
        btnNum15 = new JButton("15");
        panel.add(btnNum15);
        btnNum16 = new JButton("16");
        panel.add(btnNum16);
        btnNum17 = new JButton("17");
        panel.add(btnNum17);
        btnNum18 = new JButton("18");
        panel.add(btnNum18);
        btnNum19 = new JButton("19");
        panel.add(btnNum19);
        btnNum20 = new JButton("20");
        panel.add(btnNum20);
        btnNum21 = new JButton("21");
        panel.add(btnNum21);
        btnNum22 = new JButton("22");
        panel.add(btnNum22);
        btnNum23 = new JButton("23");
        panel.add(btnNum23);
        btnNum24 = new JButton("24");
        panel.add(btnNum24);
        btnNum25 = new JButton("25");
        panel.add(btnNum25);
        btnNum26 = new JButton("26");
        panel.add(btnNum26);
        btnNum27 = new JButton("27");
        panel.add(btnNum27);

        txtNombre = new JTextField();
        txtNombre.setText("Tu Nombre");
        contentPane.add(txtNombre, BorderLayout.NORTH);
        txtNombre.setColumns(1);
        
        txtNums = new JTextField();
        txtNums.setText("Numeros");
        contentPane.add(txtNums, BorderLayout.NORTH);
        txtNums.setColumns(1);

        arrayAleatorio = new int[TOTAL];
        arrayConexion = new int[100];

        eventos();

        // Llenamos los botones y los habilitamos al abrir
        llenarArray();
        generarCarton();
    }

    // ---------------- EVENTOS ----------------
    public void eventos() {
    	// Declarar
        arrayBtn = new JButton[] {
            btnNum1, btnNum2, btnNum3, btnNum4, btnNum5, btnNum6, btnNum7, btnNum8, btnNum9,
            btnNum10, btnNum11, btnNum12, btnNum13, btnNum14, btnNum15, btnNum16, btnNum17,
            btnNum18, btnNum19, btnNum20, btnNum21, btnNum22, btnNum23, btnNum24, btnNum25,
            btnNum26, btnNum27
        };

        // Nueva partida
        btnPartida.addActionListener(e -> {
            llenarArray();
            generarCarton();
            btnColor.setEnabled(true);
            btnPartida.setEnabled(false);	//no le puede dar hasta que finalize la misma
        });

        // Cambiar color
        btnColor.addActionListener(e -> {
            dlgColor = new JColorChooser();
            Color color = dlgColor.showDialog(rootPane, "Elige color", btnColor.getBackground());
            if(color!=null) {
                btnColor.setBackground(color);
            }
        });

        // Deshabilitar botón y comprobar ganador
        for (JButton boton : arrayBtn) {
            boton.addActionListener(e -> {
                boton.setEnabled(false);
                boton.setBackground(btnColor.getBackground());
                comprobarGanador(); // Llamar al comprobador onclick
            });
        }
    }

    private void llenarArray() {
        int pos = 0;
        while (pos < TOTAL) {
            int numero = (int) (Math.random() * 90 + 1);
            boolean repetido = false;
            for (int i = 0; i < pos; i++) {
                if (arrayAleatorio[i] == numero) {
                    repetido = true;
                    break;
                }
            }
            if (!repetido) {
                arrayAleatorio[pos] = numero;
                pos++;
            }
        }

        for (int i = 0; i < TOTAL; i++) {
            arrayBtn[i].setText(String.valueOf(arrayAleatorio[i]));
            arrayBtn[i].setBackground(Color.LIGHT_GRAY);
            arrayBtn[i].setEnabled(false);
        }
    }

    private void generarCarton() {
        for (JButton b : arrayBtn) {
            b.setEnabled(true);
            b.setBackground(Color.WHITE);
        }

        for (int pos = 0; pos < FILAS; pos++) {
            int deshabilitados = 0;
            while (deshabilitados < 4) {
                int col = (int) (Math.random() * COLUMNAS);
                int aux = pos * COLUMNAS + col;
                if (aux < arrayBtn.length && arrayBtn[aux].isEnabled()) {
                    arrayBtn[aux].setEnabled(false);
                    arrayBtn[aux].setBackground(Color.GRAY);
                    deshabilitados++;
                }
            }
        }
    }

    // Comprobar si el jugador ganó
    private void comprobarGanador() {
        boolean todosMarcados = true;
        for (JButton b : arrayBtn) {
            if (b.isEnabled()) {
                todosMarcados = false;	//comprobar si todos los botones numericos han sido pinchados
                break;
            }
        }
        if (todosMarcados) {			// ha pinchado todos
            String nombre = txtNombre.getText();
            if (nombre.isEmpty()) {
            	nombre = "Jefesito";
            }
            JOptionPane.showMessageDialog(this, nombre + " ha ganado!", "¡Felicidades!", JOptionPane.WARNING_MESSAGE);
            btnPartida.setEnabled(true); // Activao
        }
    }
}
