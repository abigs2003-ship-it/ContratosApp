package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasTitulares;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasTitularesRepository {

    /**
     * Gets the next incrementing ID for a new titular.
     */
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdTitular), 0) + 1 AS NextId FROM PMT_App_Ventas_Titulares";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasTitulares t) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Titulares (IdTitular, IdContrato, Nombre, Paterno, Materno, TipoTitular, IdUsuarioAlta, FechaAlta, FechaCumpleaños, Ocupacion, Parentesco) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, t.idTitular);
            ps.setLong(2, t.idContrato);
            ps.setString(3, t.nombre);
            ps.setString(4, t.paterno);
            ps.setString(5, t.materno);
            ps.setString(6, t.tipoTitular);
            ps.setLong(7, t.idUsuarioAlta);
            ps.setTimestamp(8, t.fechaAlta);
            ps.setDate(9, t.fechaCumpleaños);
            ps.setString(10, t.ocupacion);
            ps.setLong(11, t.parentesco);
            ps.executeUpdate();
        }
    }

    public List<VentasTitulares> getByContratoId(long idContrato) throws SQLException {
        List<VentasTitulares> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Titulares WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasTitulares t = new VentasTitulares();
                    t.idTitular = rs.getLong("IdTitular");
                    t.idContrato = rs.getLong("IdContrato");
                    t.nombre = rs.getString("Nombre");
                    t.paterno = rs.getString("Paterno");
                    t.materno = rs.getString("Materno");
                    t.tipoTitular = rs.getString("TipoTitular");
                    t.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    t.fechaAlta = rs.getTimestamp("FechaAlta");
                    t.fechaCumpleaños = rs.getDate("FechaCumpleaños");
                    t.ocupacion = rs.getString("Ocupacion");
                    t.parentesco = rs.getLong("Parentesco");
                    list.add(t);
                }
            }
        }
        return list;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Titulares WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
