package src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

//import org.jgroups.blocks.cs.ReceiverAdapter;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

public class BankSys implements Receiver{
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

    private void processMessage(){
        while(true){
            try{
                Message msg = channel.receive();

                if(msg != null){
                    String receivedMessage = (String) msg.getObject();
                    receive(receivedMessage);
                }

                Thread.sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receive(Message msg){
        try{
            String receivedMessage = msg.getObject().toString();
            Connection conn = account.connect();
            // System.out.println("Received Message: " + receivedMessage);

            String[] parts = receivedMessage.split(":");
            String command = parts[0];

            switch(command){
                case "LOGIN":
                    handleLogin(parts[1], parts[2]);
                    break;

                case "REGISTER":
                    handleRegister(parts[1], parts[2], parts[3], parts[4]);
                    break;

                case "TRANSFER":
                    handleTransfer(parts[1],parts[2]);
                    break;
            }

            if(receivedMessage.equals("LOGIN_SUCCESS")){
                System.out.println("Login bem-sucedido!");
                login = true;
            }else if(receivedMessage.equals("INVALID_PASSWORD")){
                System.out.println("Senha incorreta. Tente novamente.");
            }else if(receivedMessage.equals("ACCOUNT_NOT_FOUND")){
                System.out.println("Conta não encontrada. Tente novamente.");
            }

            if(receivedMessage.equals("REGISTER_SUCCESS")){
                System.out.println("Registro bem-sucedido!");
                account.pushDB(account);
            }

            if(receivedMessage.equals("TRANSFER_SUCCESS")){
                System.out.println("Transferência concluída com sucesso. Saldos atualizados.");
            }
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLogin(String key, String password) throws Exception {
        int account_ID = Integer.parseInt(key);
        String account_password = password;
        boolean result;
        String sql = "SELECT Password, Key FROM Account WHERE Password = ? AND Key = ?";
    
        try (Connection conn = account.connect();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
            ps.setString(1, account_password);
            ps.setInt(2, account_ID);

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
    
            try (PreparedStatement ps2 = conn.prepareStatement(sql);
                ResultSet rs2 = ps2.executeQuery())  {
                ps2.setInt(1, account_ID);
    
                if (rs2.next()) {
                    account_name = rs2.getString("Name");
                    account_cpf = rs2.getString("CPF");
                    account_balance = rs2.getDouble("Balance");
    
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
    
    private void handleRegister(String name, String password, String cpf, String balance) throws Exception{
        String account_name = name;
        String account_password = password;
        String account_cpf = cpf;
        double account_balance = Double.parseDouble(balance);  

        boolean result = true;

        Random r = new Random();
        int low = 1000;
        int high = 9999;

        String sql = "SELECT Key FROM Account WHERE Key = ?";
        Connection conn = account.connect();
        PreparedStatement ps = conn.prepareStatement(sql);

        while(result == true){
            id = r.nextInt(high-low) + low;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            result = rs.getBoolean(1);
        }
        
        account.setName(account_name);
        account.setPassword(account_password);
        account.setCPF(account_cpf);
        account.setBalance(account_balance); 
        account.setID(id);

        sendMessage("REGISTER_SUCCESS");
    }

    private void handleTransfer(String key, String value) throws SQLException{
        int own_account = account.getID();

        Double transfer_balance = 0.0;
        Double transfer_value = Double.parseDouble(value);
        int transfer_account = Integer.parseInt(key);

        Double own_balance = 0.0;

        int rowsAffected1 = 0;
        int rowsAffected2 = 0;

        String sql1 = "SELECT Balance FROM Account WHERE Key = ?";
        String sql2 = "SELECT Balance FROM Account WHERE Key = ?";
        String sql_update1 = "UPDATE Account SET Balance = ? WHERE Key = ?";
        String sql_update2 = "UPDATE Account SET Balance = ? WHERE Key = ?";

        try(Connection conn1 = account.connect();
            PreparedStatement ps1 = conn1.prepareStatement(sql1);

            Connection conn2 = account.connect();
            PreparedStatement ps2 = conn2.prepareStatement(sql2);

            Connection conn_update1 = account.connect();
            PreparedStatement ps_update1 = conn_update1.prepareStatement(sql_update1);

            Connection conn_update2 = account.connect();
            PreparedStatement ps_update2 = conn_update2.prepareStatement(sql_update2)) {

            ps1.setInt(1, own_account);
            ResultSet rs1 = ps1.executeQuery();

            if(rs1.next()){
                own_balance = rs1.getDouble("Balance");
                own_balance -= transfer_value;
            }else{
                System.out.println("Erro ao recuperar saldo da própria conta.");
                return;
            }

            ps2.setInt(1, transfer_account);

            ResultSet rs2 = ps2.executeQuery();

            if(rs2.next()){
                transfer_balance = rs2.getDouble("Balance");
                transfer_balance += transfer_value;
            }else{
                System.out.println("Erro ao recuperar saldo da conta de destino.");
                return;
            }

            ps_update1.setDouble(1, own_balance);
            ps_update1.setInt(2, own_account);

            rowsAffected1 = ps_update1.executeUpdate();

            ps_update2.setDouble(1, transfer_balance);
            ps_update2.setInt(2, transfer_account);

            rowsAffected2 = ps_update2.executeUpdate();
        }

        if(rowsAffected1 > 0 && rowsAffected2 > 0){
            sendMessage("TRANSFER_SUCCESS");
        }else{
            System.out.println("Erro ao realizar a transferência.");
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

    private void handleregisterRequest() throws Exception{
        System.out.println("Nome do titular da conta: ");
        name = keyboard.nextLine();
        

        System.out.println("Senha da conta: ");
        password = keyboard.nextLine();
        
        System.out.println("CPF do titular da conta");
        cpf = keyboard.nextLine();
        System.out.println(" ");
        
        // Double balance = keyboard.nextDouble();
        balance = 1000.00; 
        
        String registerRequest = "REGISTER:" + name + ":" + password + ":" + cpf + ":" + balance;
        sendMessage(registerRequest);
    }

    private void handletransferRequest() throws Exception{
        int transfer_account = 0;
        Double transfer_value = 0.0;
        
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

        String registerRequest = "TRANSFER:" + transfer_account + ":" + transfer_account;
        sendMessage(registerRequest);
    }

    private void startThread() {
        new Thread(this::processMessage).start();
    }


    public static void main(String[] args) throws Exception {
        BankSys bankSystem = new BankSys();

        String sql = "";
        
        try (Scanner keyboard = new Scanner(System.in);
            Connection conn = account.connect()) {
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
                        bankSystem.handleregisterRequest();
                        break;
                    case 3:
                        System.exit(1);
                }
            }
            
            if(bankSystem.login == true){
                while(true){
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

                            try(PreparedStatement ps = conn.prepareStatement(sql)){
                                try(ResultSet rs = ps.executeQuery()){
                                    if (rs.next()) {
                                        // Obtém o valor da coluna "TotalBalance"
                                        montante = rs.getDouble("TotalMontante");
                                        System.out.println("Total dos saldos: R$" + montante);
                                    }
                                }  
                            }  
                            
                            break;
                        case 2:
                            bankSystem.handletransferRequest();
                            break;
                        case 3:
                            System.exit(1);
                    }
                }
            }else{
                System.out.println(" ");
                System.out.println("Login ERROR: Login não realizado");
                System.out.println(" ");
            }
        }
    }
}