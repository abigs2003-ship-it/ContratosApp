package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasContrato;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasContratoRepository {

    /**
     * Gets the next incrementing ID for a new contract.
     * Returns 1 if the table is empty.
     */
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdContrato), 0) + 1 AS NextId FROM PMT_App_Ventas_Contrato";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasContrato c) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Contrato (IdContrato, FechaAlta, IdUsuarioAlta, FechaModificacion, Estatus, Idioma) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, c.idContrato);
            ps.setTimestamp(2, c.fechaAlta);
            ps.setLong(3, c.idUsuarioAlta);
            ps.setTimestamp(4, c.fechaModificacion);
            ps.setString(5, c.estatus);
            ps.setString(6, c.idioma);
            ps.executeUpdate();
        }
    }

    public void update(VentasContrato c) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Contrato SET FechaModificacion=?, Estatus=?, Idioma=? WHERE IdContrato=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, c.fechaModificacion);
            ps.setString(2, c.estatus);
            ps.setString(3, c.idioma);
            ps.setLong(4, c.idContrato);
            ps.executeUpdate();
        }
    }

    public VentasContrato getById(long id) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Contrato WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasContrato c = new VentasContrato();
                    c.idContrato = rs.getLong("IdContrato");
                    c.fechaAlta = rs.getTimestamp("FechaAlta");
                    c.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    c.fechaModificacion = rs.getTimestamp("FechaModificacion");
                    c.estatus = rs.getString("Estatus");
                    c.idioma = rs.getString("Idioma");
                    return c;
                }
            }
        }
        return null;
    }

    public List<VentasContrato> getAll() throws SQLException {
        List<VentasContrato> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Contrato ORDER BY FechaModificacion DESC";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VentasContrato c = new VentasContrato();
                c.idContrato = rs.getLong("IdContrato");
                c.fechaAlta = rs.getTimestamp("FechaAlta");
                c.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                c.fechaModificacion = rs.getTimestamp("FechaModificacion");
                c.estatus = rs.getString("Estatus");
                c.idioma = rs.getString("Idioma");
                list.add(c);
            }
        }
        return list;
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Contrato WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
