import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DGP_Fixer extends JFrame {
    private JPanel Panel;
    private JTextField LoginInput;
    private JPasswordField PasswordInput;
    private JButton loginButton;

    //public static final String DB_URL = "jdbc:postgresql://mytest.cavsgul9fmfx.us-east-2.rds.amazonaws.com:5432/mytest";
    public static final String DB_Driver = "org.postgresql.Driver";
    public static Statement statement;
    public static ResultSet Answer;
    public static Connection conn;

    public void LoginToBase()
    {
        try{
            Class.forName(DB_Driver).getDeclaredConstructor().newInstance();
            System.out.println("Initialization succesfull!");
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(null, ex);
        }
        try {
            conn = java.sql.DriverManager.getConnection(Main.DBURL, LoginInput.getText(), PasswordInput.getText());
            statement = conn.createStatement();
                    /*Answer = statement.executeQuery("select * from \"Test\".testtable");
                    while (Answer.next()) {
                        JOptionPane.showMessageDialog(null, Answer.getString(1)+" "+Answer.getString(2));
                    }*/
            statement.close();
            conn.close();
            Main.DBLogin = LoginInput.getText();
            Main.DBPassword = PasswordInput.getText();
            DGPMainForm dgpMainForm = new DGPMainForm();
            // Упаковываем все элементы с нашей формы
            dgpMainForm.pack();
            // Изменяем размеры окна
            dgpMainForm.setSize(new Dimension(400,200));
            // Отображаем созданное окно
            dgpMainForm.setVisible(true);
            /*DGP_Fixer_Main dgpFixerMain = new DGP_Fixer_Main();
            // Упаковываем все элементы с нашей формы
            dgpFixerMain.pack();
            // Изменяем размеры окна
            dgpFixerMain.setSize(new Dimension(400,200));
            // Отображаем созданное окно
            dgpFixerMain.setVisible(true);*/
            dispose();
        } catch (SQLException throwables) {
            JOptionPane.showMessageDialog(null, throwables);
        }
    }

    public DGP_Fixer()
    {
        this.getContentPane().add(Panel);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginToBase();
            }
        });

        LoginInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    LoginToBase();
                }
            }
        });
        PasswordInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    LoginToBase();
                }
            }
        });
        loginButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    LoginToBase();
                }
            }
        });
    }
}
