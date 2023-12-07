package src.main.java.src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;

public class Account{
    private String Name;
    private String Password;
    private String CPF;
    private int Key;

    //private static final String INSERT_SQL = "INSERT INTO Account(Nome, CPF, Saldo, Key) VALUES(?, ?, ?, ?)";

    public void setName(String Name){
        this.Name = Name;
    }

    public void setPassword(String Password){
        this.Password = Password;
    }

    public void setCPF(String CPF){
        this.CPF = CPF;
    }

    public void setID(int Key){
        this.Key = Key;
    }

    public Connection connect() {
        // SQLite connection string
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String url = "jdbc:sqlite:D://IFMG//SD//Trabalho_Banco//SD-Bank//base.sqlite";
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }

        return conn;
    }

    public void pushDB(String Name, String Password, String CPF, int Key){
        String sql = "INSERT INTO Account(Nome, CPF, Saldo, Key) VALUES(?, ?, ?, ?)";

        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setString(1, Name);
                pstmt.setString(2, Password);
                pstmt.setString(3, CPF);
                pstmt.setInt(4, Key);

                pstmt.executeUpdate();

                conn.commit();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        } 
    }

    public static void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}