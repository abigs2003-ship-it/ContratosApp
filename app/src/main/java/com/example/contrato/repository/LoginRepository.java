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

        String sql = "{call dbo.sp_Sel_Valida_Usuario(?, ?)}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setString(1, usuario);
            cs.setString(2, password);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    String idStr = rs.getString("IdUsuario");

                    // Si no es número, login inválido
                    if (idStr == null || !idStr.matches("\\d+")) {
                        return null;
                    }

                    long id = Long.parseLong(idStr);
                    String nombre = rs.getString("Nombre");

                    return new LoginResult(id, nombre);
                }

            }
        }
        return null;
    }
}
