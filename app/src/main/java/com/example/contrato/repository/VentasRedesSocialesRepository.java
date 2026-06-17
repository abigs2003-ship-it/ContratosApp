package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasRedesSociales;
import java.sql.*;
import java.util.Objects;

public class VentasRedesSocialesRepository {

    // marca el registro activo como 'C' sin eliminar físicamente
    public boolean huboCambios(VentasRedesSociales a, VentasRedesSociales n) {
        return !Objects.equals(a.usuarioInstagram, n.usuarioInstagram)
                || !Objects.equals(a.usuarioFacebook,  n.usuarioFacebook)
                || !Objects.equals(a.usuarioTwitter,   n.usuarioTwitter);
    }

    // 104

    /*
    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_RedesSociales_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 9.2
    public void insert(VentasRedesSociales r) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_RedesSociales_Insert(?,?,?,?,?,?)}")) {
            cs.setLong(1,   r.idRedSocial);
            cs.setLong(2,   r.idContrato);
            cs.setString(3, r.usuarioInstagram);
            cs.setString(4, r.usuarioFacebook);
            cs.setString(5, r.usuarioTwitter);
            cs.setLong(6,   r.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 9.3
    public void deleteByContratoId(long idContrato, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_RedesSociales_DeleteByContratoId(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 9.4
    public VentasRedesSociales getByContratoId(long idContrato) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_RedesSociales_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    VentasRedesSociales r = new VentasRedesSociales();
                    r.idRedSocial      = rs.getLong("IdRedSocial");
                    r.idContrato       = rs.getLong("IdContrato");
                    r.usuarioInstagram = rs.getString("UsuarioInstagram");
                    r.usuarioFacebook  = rs.getString("UsuarioFacebook");
                    r.usuarioTwitter   = rs.getString("UsuarioTwitter");
                    r.fechaAlta        = rs.getTimestamp("FechaAlta");
                    r.idUsuarioAlta    = rs.getLong("IdUsuarioAlta");
                    return r;
                }
            }
        }
        return null;
    }
*/
    //Cintas
    // /*
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
        String sql = "INSERT INTO PMT_App_Ventas_Redes_Sociales (IdRedSocial, IdContrato, UsuarioInstagram, UsuarioFacebook, UsuarioTwitter, FechaAlta, IdUsuarioAlta, Estatus, IdUsuarioModificacion, FechaModificacion) VALUES (?, ?, ?, ?, ?, GETDATE(), ?, 'A', NULL, NULL)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.idRedSocial);
            ps.setLong(2, r.idContrato);
            ps.setString(3, r.usuarioInstagram);
            ps.setString(4, r.usuarioFacebook);
            ps.setString(5, r.usuarioTwitter);
            ps.setLong(6, r.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public void deleteByContratoId(long idContrato, long idUsuarioModificacion) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Redes_Sociales SET Estatus = 'C', IdUsuarioModificacion = ?, FechaModificacion = GETDATE() WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuarioModificacion);
            ps.setLong(2, idContrato);
            ps.executeUpdate();
        }
    }
    // Solo devuelve el registro activo (Estatus = 'A') para mostrar en pantalla
    public VentasRedesSociales getByContratoId(long idContrato) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Redes_Sociales WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasRedesSociales r = new VentasRedesSociales();
                    r.idRedSocial      = rs.getLong("IdRedSocial");
                    r.idContrato       = rs.getLong("IdContrato");
                    r.usuarioInstagram = rs.getString("UsuarioInstagram");
                    r.usuarioFacebook  = rs.getString("UsuarioFacebook");
                    r.usuarioTwitter   = rs.getString("UsuarioTwitter");
                    r.fechaAlta        = rs.getTimestamp("FechaAlta");
                    r.idUsuarioAlta    = rs.getLong("IdUsuarioAlta");
                    return r;
                }
            }
        }
        return null;
    }
//*/
}
