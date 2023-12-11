package src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Scanner;
import java.util.Random;

//import org.jgroups.blocks.cs.ReceiverAdapter;
import org.jgroups.*;
import java.util.*;

public class BankSys extends ReceiverAdapter{
    static int key = 0;
    static int option = 0;

    static String name;
    static String password;
    static String cpf;
    static Double balance;
    static int id;

    private boolean login = false;

    
    static int account_key = 0;
    static Double account_balance = 0.0;
    static String account_name = "";
    static String account_cpf = "";

    // Random r = new Random();
    static Account account = new Account();

    static String sql;
    static PreparedStatement ps, ps2;

    private JChannel channel;
    
    static Scanner keyboard = new Scanner(System.in);

    public BankSys() throws Exception{
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("BankCluster");
    }

    private void sendMessage(String message) {
        try {
            Message msg = new Message(null, null, message);
            channel.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receive(Message msg){
        try{
            String receivedMessage = (String) msg.getObject();
            // System.out.println("Received Message: " + receivedMessage);

            String[] parts = receivedMessage.split(":");
            String command = parts[0];

            switch(command){
                case "LOGIN":
                    handleLogin(parts[1], parts[2]);
                    break;
            }

            if (receivedMessage.equals("LOGIN_SUCCESS")) {
                System.out.println("Login bem-sucedido!");
                login = true;
            } else if (receivedMessage.equals("INVALID_PASSWORD")) {
                System.out.println("Senha incorreta. Tente novamente.");
            } else if (receivedMessage.equals("ACCOUNT_NOT_FOUND")) {
                System.out.println("Conta não encontrada. Tente novamente.");
            }
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLogin(String key, String password) throws Exception {
        int account_ID = Integer.parseInt(key);
        String account_password = password;
        boolean result;

        Connection conn = account.connect();
        sql = "SELECT Password, Key FROM Account WHERE Password = ? AND Key = ?";
    
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account_password);
            ps.setInt(2, account_ID);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                sendMessage("INVALID_PASSWORD");
                return;
            }
    
            result = rs.getBoolean("Password");
            if (!result) {
                sendMessage("INVALID_PASSWORD");
                return;
            }
    
            result = rs.getBoolean("Key");
            if (!result) {
                sendMessage("ACCOUNT_NOT_FOUND");
                return;
            }
    
            // System.out.println("Key: " + account_ID);
            // System.out.println("Password: " + account_password);
    
            sql = "SELECT Name, CPF, Balance FROM Account WHERE Key = ?";
    
            try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                ps2.setInt(1, account_ID);
                rs = ps2.executeQuery();
    
                if (rs.next()) {
                    account_name = rs.getString("Name");
                    account_cpf = rs.getString("CPF");
                    account_balance = rs.getDouble("Balance");
    
                    account.setName(account_name);
                    account.setPassword(account_password);
                    account.setCPF(account_cpf);
                    account.setBalance(account_balance);
                    account.setID(account_ID);
    
                    sendMessage("LOGIN_SUCCESS");
                }
            }
        }
    }
    

    private void handleloginRequest() throws Exception, SQLException{
        boolean result;
        
        while(!login){
            try {   //nextInt nao pode ler /n. Ler Line e transformar em int
                System.out.println("Chave da conta: ");
                key = Integer.parseInt(keyboard.nextLine());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            
            System.out.println("Senha da conta: ");
            password = keyboard.nextLine();
            System.out.println(" ");

            String loginRequest = "LOGIN:" + key + ":" + password;
            sendMessage(loginRequest);
        }
    }

    private void handleregister() throws Exception{

    }
    public static void main(String[] args) throws Exception {
        boolean result;
        BankSys bankSystem = new BankSys();

        Connection conn = account.connect();
        
        try (Scanner keyboard = new Scanner(System.in)) {
            while(bankSystem.login == false){
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
                        bankSystem.handleloginRequest();
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

            
            if(bankSystem.login == true){
                account_key = account.getID();
                account_name = account.getName();

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
                        Double montante;

                        sql = "SELECT SUM(Balance) as TotalMontante FROM Account";

                        ps = conn.prepareStatement(sql);
                        try(ResultSet rs = ps.executeQuery()){
                            if (rs.next()) {
                                // Obtém o valor da coluna "TotalBalance"
                                montante = rs.getDouble("TotalMontante");
                                System.out.println("Total dos saldos: R$" + montante);
                            }
                        }    
                        
                        break;
                    case 2:
                        int transfer_account = 0;
                        int own_account = account.getID();

                        Double transfer_value = 0.0, transfer_balance = 0.0;
                        Double own_balance;

                        String sql1;
                        String sql2;

                        try{   //nextInt nao pode ler /n. Ler Line e transformar em int
                            System.out.println("Digite a conta para que voce quer transferir: ");
                            transfer_account = Integer.parseInt(keyboard.nextLine());
                        }catch(NumberFormatException e) {
                            e.printStackTrace();
                        }

                        try{   //nextInt nao pode ler /n. Ler Line e transformar em int
                            System.out.println("Digite o valor da transferência: R$");
                            transfer_value = Double.parseDouble(keyboard.nextLine());
                        }catch(NumberFormatException e) {
                            e.printStackTrace();
                        }

                        sql1 = "SELECT Balance FROM Account WHERE Key = ?";
                        ps = conn.prepareStatement(sql1);
                        ps.setInt(1, own_account);
                        ResultSet rs1 = ps.executeQuery();

                        if(rs1.next()){
                            own_balance = rs1.getDouble("Balance");
                            own_balance -= transfer_value;
                        }else{
                            System.out.println("Erro ao recuperar saldo da própria conta.");
                            break;
                        }

                        sql2 = "SELECT Balance FROM Account WHERE Key = ?";

                        ps2 = conn.prepareStatement(sql2);
                        ps2.setInt(1, transfer_account);

                        ResultSet rs2 = ps2.executeQuery();

                        if(rs2.next()){
                            transfer_balance = rs2.getDouble("Balance");
                            transfer_balance += transfer_value;
                        }else{
                            System.out.println("Erro ao recuperar saldo da conta de destino.");
                            break;
                        }

                        sql1 = "UPDATE Account SET Balance = ? WHERE Key = ?";

                        ps = conn.prepareStatement(sql1);
                        ps.setDouble(1, own_balance);
                        ps.setInt(2, own_account);

                        int rowsAffected1 = ps.executeUpdate();

                        sql2 = "UPDATE Account SET Balance = ? WHERE Key = ?";

                        ps2 = conn.prepareStatement(sql2);
                        ps2.setDouble(1, transfer_balance);
                        ps2.setInt(2, transfer_account);

                        int rowsAffected2 = ps2.executeUpdate();

                        if (rowsAffected1 > 0 && rowsAffected2 > 0) {
                            System.out.println("Transferência concluída com sucesso. Saldos atualizados.");
                        } else {
                            System.out.println("Erro ao realizar a transferência.");
                        }

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