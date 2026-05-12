package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasRegalos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasRegalosRepository {

    /**
     * Gets the next incrementing ID for a new regalo.
     */
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdRegalo), 0) + 1 AS NextId FROM PMT_App_Ventas_Regalos";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasRegalos r) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Regalos (IdRegalo, IdContrato, Descripcion, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.idRegalo);
            ps.setLong(2, r.idContrato);
            ps.setString(3, r.descripcion);
            ps.setTimestamp(4, r.fechaAlta);
            ps.setLong(5, r.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public List<VentasRegalos> getByContratoId(long idContrato) throws SQLException {
        List<VentasRegalos> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Regalos WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasRegalos r = new VentasRegalos();
                    r.idRegalo = rs.getLong("IdRegalo");
                    r.idContrato = rs.getLong("IdContrato");
                    r.descripcion = rs.getString("Descripcion");
                    r.fechaAlta = rs.getTimestamp("FechaAlta");
                    r.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(r);
                }
            }
        }
        return list;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Regalos WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
