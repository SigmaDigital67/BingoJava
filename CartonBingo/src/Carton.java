import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Carton extends JFrame {
    private static final int TOTAL = 27;
    private static final int FILAS = 3;
    private static final int COLUMNAS = 9;
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtNombre;
    private JButton btnPartida;
    private JButton btnConectar;
    private JButton btnAutocompletar;
    private JButton btnColor;
    private JButton[] arrayBtn;
    private int[] arrayAleatorio;
    private int[] arrayConexion;
    private Color colorMarcado = Color.YELLOW;

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
        contentPane.setLocation(new Point(1, 1));
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
        btnAutocompletar = new JButton("Autocompletar");
        btnPartida = new JButton("Nueva Partida");
        btnPartida.setEnabled(false);
        btnColor = new JButton("CambiarColor");

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
        arrayConexion = new int[100];

        if (!java.beans.Beans.isDesignTime()) {
            eventos();
            llenarArray();
            generarCarton();
        }
    }

    private void eventos() {
        btnPartida.addActionListener(e -> {
            llenarArray();
            generarCarton();
            btnColor.setEnabled(true);
            btnPartida.setEnabled(false);
        });

        btnColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Elige color", colorMarcado);
            if (c != null) colorMarcado = c;
        });

        for (JButton b : arrayBtn) {
            b.addActionListener(e -> {
                if (b.getBackground().equals(colorMarcado)) {
                    b.setBackground(Color.WHITE);
                } else {
                    b.setBackground(colorMarcado);
                }
                comprobarGanador();
            });
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
        for (int i = 0; i < TOTAL; i++) {
            arrayBtn[i].setText(String.valueOf(arrayAleatorio[i]));
            arrayBtn[i].setBackground(Color.LIGHT_GRAY);
        }
    }

    private void generarCarton() {
        for (JButton b : arrayBtn) {
            b.setBackground(Color.WHITE);
        }
        for (int f = 0; f < FILAS; f++) {
            int des = 0;
            while (des < 4) {
                int c = (int) (Math.random() * COLUMNAS);
                int idx = f * COLUMNAS + c;
                if (idx < TOTAL && !arrayBtn[idx].getBackground().equals(Color.GRAY)) {
                    arrayBtn[idx].setBackground(Color.GRAY);
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
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) nombre = "Jefesito";
            JOptionPane.showMessageDialog(this, nombre + " ha ganado!", "¡Felicidades!", JOptionPane.WARNING_MESSAGE);
            btnPartida.setEnabled(true);
        }
    }
}