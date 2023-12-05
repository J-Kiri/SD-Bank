package src;

import java.sql.SQLException;

public class Service{
    public static void main(String[] args) throws SQLException {
        Account account1 = new Account();

        account1.setName("Joao");
        account1.setPassword("123abc");
        account1.setCPF("12345678910");
        ///account1.setId(1)

        account1.pushDB("Joao", "123abc", "12345678910", 1);
    }
}