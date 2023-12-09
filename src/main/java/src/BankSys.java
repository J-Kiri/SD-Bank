package src.main.java.src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Random;


public class BankSys{
    public void search(){

    }
    public static void main(String[] args) throws SQLException {
        try (Scanner keyboard = new Scanner(System.in)) {
            int key = 0;
            int option = 0;

            String name;
            String password;
            String cpf;
            double balance;
            int id;

            boolean result;
            boolean login = false;

            
            int account_key = 0;
            double account_balance = 0;
            String account_name = "";
            String account_cpf = "";

            // Random r = new Random();
            Account account = new Account();
            Connection conn = account.connect();

            String sql;
            PreparedStatement ps;

            while(login == false){
                System.out.println("    -   SD Bank   -   ");
                System.out.println(" ");
                System.out.println("[ 1 - Fazer login     ]");
                System.out.println("[ 2 - Registrar conta ]");
                System.out.println("[ 3 - Encerrar sessão ]");

                try {   //nextInt nao pode ler /n. Ler Line e transformar em int
                    System.out.println("Opção: ");
                    option = Integer.parseInt(keyboard.nextLine());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                switch(option){
                    case 1:
                        do{ 
                            try {   //nextInt nao pode ler /n. Ler Line e transformar em int
                                System.out.println("Chave da conta: ");
                                key = Integer.parseInt(keyboard.nextLine());
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            
                            System.out.println("Senha da conta: ");
                            password = keyboard.nextLine();
                            System.out.println(" ");

                            sql = "SELECT Password, Key FROM Account WHERE Password = ? AND Key = ?";

                            ps = conn.prepareStatement(sql);
                            ps.setString(1, password);
                            ps.setInt(2, key);
                            ResultSet rs = ps.executeQuery();
                            
                            if((result = rs.getBoolean(1)) == false){
                                System.out.println("Senha Incorreta");
                                System.out.println(" ");
                            }else if((result = rs.getBoolean(2)) == false){
                                System.out.println("Conta não encontrada");
                                System.out.println(" ");
                            }
                        }while(result == false);

                        System.out.println("Key: " + key);
                        System.out.println("Password: " + password);

                        sql = "SELECT Name, CPF, Balance FROM Account WHERE Key = ?";

                        ps = conn.prepareStatement(sql);
                        ps.setInt(1, key);
                        try(ResultSet rs = ps.executeQuery()){
                            account_name = rs.getString("Name");
                            account_cpf = rs.getString("CPF");
                            account_balance = rs.getDouble("Balance");
                        }                        

                        account.setName(account_name);
                        account.setPassword(password);
                        account.setCPF(account_cpf);
                        account.setBalance(account_balance); 
                        account.setID(key);

                        login = true;

                        break;
                    case 2:
                        Random r = new Random();
                        int low = 1000;
                        int high = 9999;

                        System.out.println("Nome do titular da conta: ");
                        name = keyboard.nextLine();
                        account.setName(name);

                        System.out.println("Senha da conta: ");
                        password = keyboard.nextLine();
                        account.setPassword(password);
                        
                        System.out.println("CPF do titular da conta");
                        cpf = keyboard.nextLine();
                        account.setCPF(cpf);
                        System.out.println(" ");
                        
                        // double balance = keyboard.nextDouble();
                        balance = 1000.00;
                        account.setBalance(balance);      
                        
                        do{ //Se ja tiver o ID no banco de dados, gerar outro
                            id = r.nextInt(high-low) + low;
                            sql = "SELECT Key FROM Account WHERE Key = ?";

                            ps = conn.prepareStatement(sql);
                            ps.setInt(1, id);
                            ResultSet rs = ps.executeQuery();
                            
                            result = rs.getBoolean(1);
                            
                        }while(result == true);
                        
                        account.setID(id);

                        if(conn != null){
                            account.pushDB(account);
                        }
                    

                        break;
                    case 3:
                        System.exit(1);
                }
            }

            
            if(login == true){
                account_key = account.getID();
                account_name = account.getName();
                sql = "SELECT Name FROM Account WHERE Key = ?";

                System.out.println(" ");
                System.out.println(" ");
                System.out.println(" ");
                System.out.println(" ");

                System.out.println("       Olá " + account_name + " - SD Bank");
                System.out.println(" ");
                System.out.println("[   1 - Para consultar montante     ]");
                System.out.println("[   2 - Para realizar transferencia ]");
                System.out.println("[   3 - Encerrar sessão             ]");
                System.out.println(" ");
                try {   //nextInt nao pode ler /n. Ler Line e transformar em int
                    System.out.println("Opção: ");
                    option = Integer.parseInt(keyboard.nextLine());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                switch(option){
                    case 1:
                        account_balance = account.getBalance();

                        break;
                    case 2:
                        break;
                    case 3:
                        System.exit(1);
                }
            }else{
                System.out.println(" ");
                System.out.println("Login ERROR: Login não realizado");
                System.out.println(" ");
            }
        }
    }
}