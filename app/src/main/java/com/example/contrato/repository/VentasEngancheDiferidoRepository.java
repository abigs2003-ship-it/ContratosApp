package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasEngancheDiferido;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasEngancheDiferidoRepository {

    /**
     * Gets the next incrementing ID for a new payment record.
     */
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdPago), 0) + 1 AS NextId FROM PMT_App_Ventas_EngancheDiferido";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasEngancheDiferido p) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_EngancheDiferido (IdPago, IdContrato, CantidadPago, FechaPago, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, p.idPago);
            ps.setLong(2, p.idContrato);
            ps.setObject(3, p.cantidadPago);
            ps.setDate(4, p.fechaPago);
            ps.setTimestamp(5, p.fechaAlta);
            ps.setLong(6, p.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public List<VentasEngancheDiferido> getByContratoId(long idContrato) throws SQLException {
        List<VentasEngancheDiferido> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_EngancheDiferido WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasEngancheDiferido p = new VentasEngancheDiferido();
                    p.idPago = rs.getLong("IdPago");
                    p.idContrato = rs.getLong("IdContrato");
                    p.cantidadPago = rs.getDouble("CantidadPago");
                    p.fechaPago = rs.getDate("FechaPago");
                    p.fechaAlta = rs.getTimestamp("FechaAlta");
                    p.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(p);
                }
            }
        }
        return list;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_EngancheDiferido WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
