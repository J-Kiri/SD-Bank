package src;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Scanner;
import java.util.Random;

public class BankingClient {
    static Account account = new Account();
    static Connection conn = account.connect();

    static String sql;
    static boolean result;
    static boolean login;

    static PreparedStatement ps;
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        BankingServiceGrpc.BankingServiceBlockingStub blockingStub = BankingServiceGrpc.newBlockingStub(channel);

        // Login requests
        LoginRequest loginRequest = LoginRequest.newBuilder();
        Scanner keyboard = new Scanner(System.in);

        int key;
        String password;

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

                    .setKey(key)  // Replace with the actual key entered by the user
                    .setPassword(password)  // Replace with the actual password entered by the user
                    .build();

                    LoginResponse loginResponse = blockingStub.login(loginRequest);

                    if (loginResponse.getSuccess()) {
                        System.out.println("Login successful");
                        // Access account details using loginResponse.getMessage()
                    } else {
                        System.out.println("Login failed: " + loginResponse.getMessage());
                    }
                    // Login requests

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
                    
                    // Double balance = keyboard.nextDouble();
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

        channel.shutdown();
    }
}