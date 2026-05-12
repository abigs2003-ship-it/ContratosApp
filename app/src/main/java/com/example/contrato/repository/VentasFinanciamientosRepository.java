package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasFinanciamientos;
import java.sql.*;

public class VentasFinanciamientosRepository {

    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdFinanciamiento), 0) + 1 AS NextId FROM PMT_App_Ventas_Financiamientos";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasFinanciamientos f) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Financiamientos (IdFinanciamiento, IdContrato, TipoPeriodo, FechaPrimerPago, MontoAFinanciar, NumeroPagos, TasaInteres, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, f.idFinanciamiento);
            ps.setLong(2, f.idContrato);
            ps.setString(3, f.tipoPeriodo);
            ps.setDate(4, f.fechaPrimerPago);
            ps.setObject(5, f.montoAFinanciar);
            ps.setObject(6, f.numeroPagos);
            ps.setObject(7, f.tasaInteres);
            ps.setTimestamp(8, f.fechaAlta);
            ps.setLong(9, f.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public void update(VentasFinanciamientos f) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Financiamientos SET TipoPeriodo=?, FechaPrimerPago=?, MontoAFinanciar=?, NumeroPagos=?, TasaInteres=? WHERE IdContrato=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.tipoPeriodo);
            ps.setDate(2, f.fechaPrimerPago);
            ps.setObject(3, f.montoAFinanciar);
            ps.setObject(4, f.numeroPagos);
            ps.setObject(5, f.tasaInteres);
            ps.setLong(6, f.idContrato);
            ps.executeUpdate();
        }
    }

    public VentasFinanciamientos getByContratoId(long idContrato) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Financiamientos WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasFinanciamientos f = new VentasFinanciamientos();
                    f.idFinanciamiento = rs.getLong("IdFinanciamiento");
                    f.idContrato = rs.getLong("IdContrato");
                    f.tipoPeriodo = rs.getString("TipoPeriodo");
                    f.fechaPrimerPago = rs.getDate("FechaPrimerPago");
                    f.montoAFinanciar = rs.getDouble("MontoAFinanciar");
                    f.numeroPagos = rs.getInt("NumeroPagos");
                    f.tasaInteres = rs.getDouble("TasaInteres");
                    f.fechaAlta = rs.getTimestamp("FechaAlta");
                    f.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    return f;
                }
            }
        }
        return null;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Financiamientos WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
