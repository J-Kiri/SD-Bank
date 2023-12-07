package src.main.java.src;

import java.sql.Connection;
import java.sql.SQLException;

public class BankSys{
    public static void main(String[] args) throws SQLException {
        Account account1 = new Account();

        account1.setName("Joao");
        account1.setPassword("123abc");
        account1.setCPF("12345678910");
        ///account1.setId(1)

        Connection conn = account1.connect();
        if(conn != null){
            account1.pushDB("Joao", "123abc", "12345678910", 1);
        }
    }
}