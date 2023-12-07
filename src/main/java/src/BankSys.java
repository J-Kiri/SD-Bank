package src.main.java.src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Random;


public class BankSys{
    public void register() throws SQLException {
        Account account = new Account();

        Connection conn = account.connect();

        try (Scanner keyboard = new Scanner(System.in)) {
            Random r = new Random();
            int low = 1000;
            int high = 9999;

            System.out.println("Nome do titular da conta: ");
            String name = keyboard.nextLine();
            account.setName(name);
            System.out.println(" ");

            System.out.println("Senha da conta: ");
            String password = keyboard.nextLine();
            account.setPassword(password);
            System.out.println(" ");
            
            System.out.println("CPF do titular da conta");
            String cpf = keyboard.nextLine();
            account.setCPF(cpf);
            System.out.println(" ");
            
            // double balance = keyboard.nextDouble();
            double balance = 1000.00;
            account.setBalance(balance);            

            boolean result;
            int id;

            do{
                id = r.nextInt(high-low) + low;
                String sql = "SELECT Key FROM Account WHERE Key = ?";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                
                result = rs.getBoolean(1);
                
            }while(result == true);
            

            account.setID(id);
        }

        if(conn != null){
            account.pushDB(account);
        }

    }

    public void search(){
        
    }
    public static void main(String[] args) throws SQLException {
        Account account = new Account();

        Connection conn = account.connect();
    }
}