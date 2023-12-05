package src;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLiteConnectionFactory;

public class Account{
    private String Name;
    private String Password;
    private String CPF;
    private int Key;

    private static final String INSERT_SQL = "INSERT INTO Account(Nome, CPF, Saldo, Key) VALUES(?, ?, ?, ?)";

    connection = SQLiteConnectionFactory.getConnection();

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

    public void connectionDB(Connection connection) {
        this.connection = connection;
    }

    public int pushDB(String Name, String Password, String CPF, int Key){
        int rowsInserted = 0;
        PreparedStatement ps = null;

        try {
            ps = this.connection.prepareStatement(INSERT_SQL);
            ps.setString(1, Name);
            ps.setString(2, Password);
            ps.setString(3, CPF);
            ps.setInt(4, Key);
            rowsInserted = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
        return rowsInserted;
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