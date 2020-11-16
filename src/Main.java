import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static String DBLogin;
    public static String DBPassword;
    public static String DBURL = "";


    public static void main(String[] args) {
        try {
            File file = new File("DGPFixer.conf");
            //создаем объект FileReader для объекта File
            FileReader fr = new FileReader(file);
            //создаем BufferedReader с существующего FileReader для построчного считывания
            BufferedReader reader = new BufferedReader(fr);
            // считаем сначала первую строку
            String line = reader.readLine();
            while (line != null) {
                if (line.split("=")[0].equals("CONNSTRING"))
                {
                    DBURL = line.split("=")[1];
                }
                // считываем остальные строки в цикле
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Не найден конфиг файл! \n " + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        if(DBURL.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Пустая строка подключения к базе данных!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        DGP_Fixer dgpFixer = new DGP_Fixer();
        // Упаковываем все элементы с нашей формы
        dgpFixer.pack();
        // Изменяем размеры окна
        dgpFixer.setSize(new Dimension(400,200));
        // Отображаем созданное окно
        dgpFixer.setVisible(true);
    }

}
