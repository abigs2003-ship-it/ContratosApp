package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasDescuentos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasDescuentosRepository {



    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdDescuento), 0) + 1 AS NextId FROM PMT_App_Ventas_Descuentos";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasDescuentos d) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Descuentos (IdDescuento, IdContrato, MontoDescuento, Descripcion, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, d.idDescuento);
            ps.setLong(2, d.idContrato);
            ps.setObject(3, d.montoDescuento);
            ps.setString(4, d.descripcion);
            ps.setTimestamp(5, d.fechaAlta);
            ps.setLong(6, d.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public List<VentasDescuentos> getByContratoId(long idContrato) throws SQLException {
        List<VentasDescuentos> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Descuentos WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasDescuentos d = new VentasDescuentos();
                    d.idDescuento = rs.getLong("IdDescuento");
                    d.idContrato = rs.getLong("IdContrato");
                    d.montoDescuento = rs.getDouble("MontoDescuento");
                    d.descripcion = rs.getString("Descripcion");
                    d.fechaAlta = rs.getTimestamp("FechaAlta");
                    d.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(d);
                }
            }
        }
        return list;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Descuentos WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
