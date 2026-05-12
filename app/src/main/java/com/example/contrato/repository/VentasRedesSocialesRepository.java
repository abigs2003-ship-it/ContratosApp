package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasRedesSociales;
import java.sql.*;

public class VentasRedesSocialesRepository {

    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdRedSocial), 0) + 1 AS NextId FROM PMT_App_Ventas_Redes_Sociales";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasRedesSociales r) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Redes_Sociales (IdRedSocial, IdContrato, UsuarioInstagram, UsuarioFacebook, UsuarioTwitter, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.idRedSocial);
            ps.setLong(2, r.idContrato);
            ps.setString(3, r.usuarioInstagram);
            ps.setString(4, r.usuarioFacebook);
            ps.setString(5, r.usuarioTwitter);
            ps.setTimestamp(6, r.fechaAlta);
            ps.setLong(7, r.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public void update(VentasRedesSociales r) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Redes_Sociales SET UsuarioInstagram=?, UsuarioFacebook=?, UsuarioTwitter=? WHERE IdContrato=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.usuarioInstagram);
            ps.setString(2, r.usuarioFacebook);
            ps.setString(3, r.usuarioTwitter);
            ps.setLong(4, r.idContrato);
            ps.executeUpdate();
        }
    }

    public VentasRedesSociales getByContratoId(long idContrato) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Redes_Sociales WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasRedesSociales r = new VentasRedesSociales();
                    r.idRedSocial = rs.getLong("IdRedSocial");
                    r.idContrato = rs.getLong("IdContrato");
                    r.usuarioInstagram = rs.getString("UsuarioInstagram");
                    r.usuarioFacebook = rs.getString("UsuarioFacebook");
                    r.usuarioTwitter = rs.getString("UsuarioTwitter");
                    r.fechaAlta = rs.getTimestamp("FechaAlta");
                    r.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    return r;
                }
            }
        }
        return null;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Redes_Sociales WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
