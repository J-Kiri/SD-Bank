package src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;

public class Account{
    private String Name;
    private String Password;
    private String CPF;
    private double Balance; 
    private int Key;

    //private static final String INSERT_SQL = "INSERT INTO Account(Nome, CPF, Saldo, Key) VALUES(?, ?, ?, ?)";

    public void setName(String Name){
        this.Name = Name;
    }

    public String getName(){
        return Name;
    }

    public void setPassword(String Password){
        this.Password = Password;
    }

    public String getPassword(){
        return Password;
    }

    public void setCPF(String CPF){
        this.CPF = CPF;
    }

    public String getCPF(){
        return CPF;
    }

    public void setBalance(double Balance){
        this.Balance = Balance;
    }

    public double getBalance(){
        return Balance;
    }

    public void setID(int Key){
        this.Key = Key;
    }

    public int getID(){
        return Key;
    }

    public Connection connect() {
        // SQLite connection string
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String url = "jdbc:sqlite:base.sqlite";
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }

        return conn;
    }

    public void pushDB(Account account) {
        String sql = "INSERT INTO Account(Name, Password, CPF, Balance, Key) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getPassword());
            pstmt.setString(3, account.getCPF());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setInt(5, account.getID());

            pstmt.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            System.out.println("Error pushing data to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void close(Statement statement, Connection conn, PreparedStatement pstmt) {
        try{
            if(statement != null){
                statement.close();
            }

            if(conn != null){
                conn.close();
            }

            if(pstmt != null){
                pstmt.close();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}