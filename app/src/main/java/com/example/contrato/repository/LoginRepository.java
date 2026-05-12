package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import java.sql.*;

public class LoginRepository {

    public static class LoginResult {
        public long empleadoId;
        public String nombreCompleto;

        public LoginResult(long id, String nombre) {
            this.empleadoId = id;
            this.nombreCompleto = nombre;
        }
    }

    public LoginResult login(String usuario, String password) throws SQLException {
        // Note: The user specified USE [Contratos], but we use the connection defined in DbConnection.
        // If the SP is in a different database, we might need to adjust the connection string or call it with database prefix.
        String sql = "{call dbo.sp_Login(?, ?)}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setString(1, usuario);
            cs.setString(2, password);
            
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("EmpleadoId");
                    String nombre = rs.getString("NombreCompleto");
                    return new LoginResult(id, nombre);
                }
            }
        }
        return null;
    }
}
