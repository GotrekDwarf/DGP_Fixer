import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DGPMainForm extends JFrame{
    private JPanel Panel;
    private JList list1;
    private JButton refreshButton;
    private JButton FixButton;
    private JButton перейтиВDGPButton;
    DefaultListModel testlist = new DefaultListModel();

    //public static final String DB_URL = "jdbc:postgresql://mytest.cavsgul9fmfx.us-east-2.rds.amazonaws.com:5432/mytest";
    public static final String DB_Driver = "org.postgresql.Driver";
    public static Statement statement;
    public static ResultSet Answer;
    public static Connection conn;

    public void GetErrorList() //получение списка гарантий в ошибке и запись его в JList
    {
        testlist.clear();
        try{
            Class.forName(DB_Driver).getDeclaredConstructor().newInstance();
            System.out.println("Connection succesfull!");
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(null, ex);
        }
        try {
            conn = java.sql.DriverManager.getConnection(Main.DBURL, Main.DBLogin, Main.DBPassword);
            statement = conn.createStatement();
            Answer = statement.executeQuery("select * from gps.guarantee where status = 'ERROR'");
            while (Answer.next()) {
                //JOptionPane.showMessageDialog(null, Answer.getString(1)+" "+Answer.getString(2));
                testlist.addElement(Answer.getString("number"));
            }
            statement.close();
            conn.close();
            list1.setModel(testlist);

        } catch (SQLException throwables) {
            JOptionPane.showMessageDialog(null, throwables);
        }
        this.getContentPane().add(Panel);
    }

    public DGPMainForm() {
        GetErrorList();
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GetErrorList();
            }
        }); //обновление списка гарантий в ошибке
        FixButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Class.forName(DB_Driver).getDeclaredConstructor().newInstance();
                    System.out.println("Initialization succesfull!");
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(null, ex);
                }
                try {
                    conn = java.sql.DriverManager.getConnection(Main.DBURL, Main.DBLogin, Main.DBPassword);
                    statement = conn.createStatement();
                    Answer = statement.executeQuery("select count(*) from gps.guarantee where status = 'ERROR' and number = '"+list1.getSelectedValue().toString()+"'");
                    Answer.next();

                    if(Answer.getInt(1) != 1) //проверка существования и уникальности записи
                    {
                        JOptionPane.showMessageDialog(null, "Не удается подтвердить уникальность гарантии! Исправьте гарантию вручную!");
                        return;
                    }


                    Answer = statement.executeQuery("select application_type from gps.guarantee where status = 'ERROR' and number = '"+list1.getSelectedValue().toString()+"'"); // проверяем стадию на которой гарантия свалилась в ошибку
                    Answer.next();


                    if(Answer.getString("application_type").equals("APPLICATION")) //если свалилось на создании
                    {
                        String response = JOptionPane.showInputDialog(null,
                                "Введите номер LOAN:",
                                "Enter LOAN number",
                                JOptionPane.QUESTION_MESSAGE);
                        //JOptionPane.showMessageDialog(null, response);
                        statement.execute("update gps.guarantee set status = 'ACTIVE', loan_id = '"+response+"' where status = 'ERROR' and application_type = 'APPLICATION' and \"number\" = '"+list1.getSelectedValue().toString()+"'");
                        Answer = statement.executeQuery("select id from gas.application where status = 'APPROVED' and number = '"+list1.getSelectedValue().toString()+"'");
                        Answer.next();
                        try {
                            Desktop.getDesktop().browse(new URL("http://dgp.raiffeisen.ru/applications/edit/"+Answer.getString("id")).toURI());
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                        GetErrorList();
                    }
                    else if(Answer.getString("application_type").equals("CHANGE")) //если свалилось на изменении
                    {
                        statement.execute("update gps.guarantee set status =" +
                            "case application_type" +
                            "when 'CHANGE' then 'ACTIVE'" +
                            "when 'PRE_CHANGE' then 'WAIT_BENEFICIARY_CONSENT'" +
                            "when 'REDUCE_AMOUNT_CHANGE' then 'WAIT_BENEFICIARY_CONSENT'" +
                            "when 'CORRECTION' then 'ACTIVE'" +
                            "when 'PRE_CORRECTION' then 'WAIT_BENEFICIARY_CONSENT'" +
                            "when 'REDUCE_AMOUNT_CORRECTION' then 'WAIT_BENEFICIARY_CONSENT'" +
                            "else 'ERROR'" +
                            "end" +
                            "where status = 'ERROR' and ID = " +
                            "(select max(id) from gps.guarantee g where number = " +
                            "(select number from gps.guarantee g  where g.id = " +
                            "(select guarantee_id from gas.guarantee_change_application where status = 'APPROVED' and id = " +
                            "(select max(id) from gas.guarantee_change_application where status = 'APPROVED' and guarantee_number = '" +
                            list1.getSelectedValue().toString() + "'))))");
                        Answer = statement.executeQuery("select max(id) from gas.guarantee_change_application where status = 'APPROVED' and guarantee_number = '" + list1.getSelectedValue().toString() + "'");
                        Answer.next();
                        try {
                            Desktop.getDesktop().browse(new URL("http://dgp.raiffeisen.ru/amendments/edit/" + Answer.getString("max")).toURI());
                        } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        GetErrorList();
                    }
                    else if(Answer.getString("application_type").equals("RENUNCIATION")) //если свалилось на отказе
                    {
                        statement.execute("update gps.guarantee set status = " +
                                "case when amount=0 or end_date <= current_date then 'FINISHED' else 'ACTIVE' end " +
                                "where status = 'ERROR' and application_type = 'RENUNCIATION' and ID =" +
                                "(select max(id) from gps.guarantee g where number = " +
                                "(select number from gps.guarantee g  where g.id = " +
                                "(select guarantee_id from gas.renunciation_of_guarantee where status = 'APPROVED' and id = " +
                                "(select max(id) from gas.renunciation_of_guarantee where status = 'APPROVED' and guarantee_number = '" +
                                list1.getSelectedValue().toString() + "'))))");
                        Answer = statement.executeQuery("select max(id) from gas.renunciation_of_guarantee where status = 'APPROVED' and guarantee_number = '" + list1.getSelectedValue().toString() + "'");
                        Answer.next();
                        try {
                            Desktop.getDesktop().browse(new URL("http://dgp.raiffeisen.ru/renunciations/edit/" + Answer.getString("max")).toURI());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        GetErrorList();                    }
                    else if(Answer.getString("application_type").equals("CLAIM"))//если свалилось на требовании
                    {
                        statement.execute("update gps.guarantee set status = " +
                                "case when amount=0 or end_date <= current_date then 'FINISHED' else 'ACTIVE' end " +
                                "where status = 'ERROR' and application_type = 'CLAIM' and ID =" +
                                "(select max(id) from gps.guarantee g where number = " +
                                "(select number from gps.guarantee g  where g.id = " +
                                "(select guarantee_id from gas.guarantee_claim where status = 'APPROVED' and id = " +
                                "(select max(id) from gas.guarantee_claim where status = 'APPROVED' and guarantee_number = '" +
                                list1.getSelectedValue().toString()+")");
                        Answer = statement.executeQuery("select max(id) from gas.guarantee_claim where status = 'APPROVED' and guarantee_number = '" + list1.getSelectedValue().toString() + "'");
                        Answer.next();
                        try {
                            Desktop.getDesktop().browse(new URL("http://dgp.raiffeisen.ru/requirements/edit/" + Answer.getString("max")).toURI());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        GetErrorList();                     }
                    else //если не знаем стадию на которой померло
                    {
                        JOptionPane.showMessageDialog(null, "Неизвестная ошибка!"+Answer.getString("application_type"));
                    }


                    statement.close();
                    conn.close();

                } catch (SQLException throwables) {
                    JOptionPane.showMessageDialog(null, throwables);
                }
            }
        });//кнопка "Починить"
        перейтиВDGPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URL("http://dgp.raiffeisen.ru").toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });//Кнопка перейти в DGP, пытается открыть DGP в браузере
    }
}
