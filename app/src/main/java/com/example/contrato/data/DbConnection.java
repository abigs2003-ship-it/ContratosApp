package com.example.contrato.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
//cambio
    private static final String IP = "10.0.2.2";
    private static final String PORT = "1433";
    private static final String DB_NAME = "Contratos";
    private static final String USER = "Test";
    private static final String PASSWORD = "test123";

    private static final String URL = "jdbc:jtds:sqlserver://" + IP + ":" + PORT + "/" + DB_NAME;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JTDS Driver not found", e);
        }
    }
}
