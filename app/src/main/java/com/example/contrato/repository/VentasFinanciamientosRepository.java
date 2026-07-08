package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasFinanciamientos;
import java.sql.*;
import java.util.Objects;

public class VentasFinanciamientosRepository {

    public boolean huboCambios(VentasFinanciamientos a, VentasFinanciamientos n) {
        return !Objects.equals(a.tipoPeriodo, n.tipoPeriodo)
                || !Objects.equals(a.fechaPrimerPago, n.fechaPrimerPago)
                || !Objects.equals(a.montoAFinanciar, n.montoAFinanciar)
                || !Objects.equals(a.numeroPagos, n.numeroPagos)
                || !Objects.equals(a.tasaInteres, n.tasaInteres);
    }

    // aqui empieza 104

    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Financiamientos_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 4.2
    public void insert(VentasFinanciamientos f) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Financiamientos_Insert(?,?,?,?,?,?,?,?)}")) {
            cs.setLong(1, f.idFinanciamiento);
            cs.setLong(2, f.idContrato);
            cs.setString(3, f.tipoPeriodo);
            cs.setDate(4, f.fechaPrimerPago);
            cs.setObject(5, f.montoAFinanciar);
            cs.setObject(6, f.numeroPagos);
            cs.setObject(7, f.tasaInteres);
            cs.setLong(8, f.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 4.3 — SP desactiva e inserta en una sola vuelta
    public void replaceByContrato(VentasFinanciamientos f, long idUsuario) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Financiamientos_ReplaceByContrato(?,?,?,?,?,?,?)}")) {
            cs.setLong(1, f.idContrato);
            cs.setString(2, f.tipoPeriodo);
            cs.setDate(3, f.fechaPrimerPago);
            cs.setObject(4, f.montoAFinanciar);
            cs.setObject(5, f.numeroPagos);
            cs.setObject(6, f.tasaInteres);
            cs.setLong(7, idUsuario);
            cs.executeUpdate();
        }
    }

    // 4.4
    public VentasFinanciamientos getByContratoId(long idContrato) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Financiamientos_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
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
                    f.estatus = rs.getString("Estatus");
                    return f;
                }
            }
        }
        return null;
    }

    // 4.5
    public void deleteByContratoId(long idContrato, long idUsuario) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Financiamientos_DeleteByContratoId(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuario);
            cs.executeUpdate();
        }
    }
}



