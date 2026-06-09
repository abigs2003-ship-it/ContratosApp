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
    /*
    public void update(VentasDescuentos descuento, long idUsuarioModificacion) throws SQLException {
        String sqlDesactivar = "UPDATE PMT_App_Ventas_Descuentos SET Estatus = 'X', IdUsuarioModificacion  = ?, FechaModificacion = GETDATE() WHERE IdContrato = ? AND Estatus    = 'A' ";

        try (Connection conexion = DbConnection.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                // 1. Marcar registros activos como inactivos (historial)
                try (PreparedStatement ps = conexion.prepareStatement(sqlDesactivar)) {
                    ps.setLong(1, idUsuarioModificacion);
                    ps.setLong(2, descuento.idContrato);
                    ps.executeUpdate();
                }

                // 2. Insertar el nuevo registro con estatus 'A'
                descuento.idDescuento = obtenerSiguienteIdConConexion(conexion);
                String sqlInsertar = """INSERT INTO PMT_App_Ventas_Descuentos
                        (IdDescuento, IdContrato, MontoDescuento, Descripcion,
                         FechaAlta, IdUsuarioAlta, Estatus)
                    VALUES (?, ?, ?, ?, GETDATE(), ?, 'A')
                    """;
                try (PreparedStatement ps = conexion.prepareStatement(sqlInsertar)) {
                    ps.setLong(1, descuento.idDescuento);
                    ps.setLong(2, descuento.idContrato);
                    ps.setObject(3, descuento.montoDescuento);
                    ps.setString(4, descuento.descripcion);
                    ps.setLong(5, descuento.idUsuarioAlta);
                    ps.executeUpdate();
                }

                conexion.commit();
            } catch (SQLException e) {
                // Revertir cambios si ocurre un error
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    // Solo devuelve registros activos (Estatus = 'A') para mostrar en pantalla
    public List<VentasDescuentos> obtenerPorIdContrato(long idContrato) throws SQLException {
        List<VentasDescuentos> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM PMT_App_Ventas_Descuentos
             WHERE IdContrato = ?
               AND Estatus    = 'A'
            """;
        try (Connection conexion = DbConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasDescuentos descuento = new VentasDescuentos();
                    descuento.idDescuento  = rs.getLong("IdDescuento");
                    descuento.idContrato   = rs.getLong("IdContrato");
                    descuento.montoDescuento = rs.getDouble("MontoDescuento");
                    descuento.descripcion  = rs.getString("Descripcion");
                    descuento.fechaAlta    = rs.getTimestamp("FechaAlta");
                    descuento.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    lista.add(descuento);
                }
            }
        }
        return lista;
    }

    // Devuelve todo el historial (registros 'A' y 'X') para auditoría
    public List<VentasDescuentos> obtenerHistorialPorIdContrato(long idContrato) throws SQLException {
        List<VentasDescuentos> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM PMT_App_Ventas_Descuentos
             WHERE IdContrato = ?
             ORDER BY IdDescuento DESC
            """;
        try (Connection conexion = DbConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasDescuentos descuento = new VentasDescuentos();
                    descuento.idDescuento           = rs.getLong("IdDescuento");
                    descuento.idContrato            = rs.getLong("IdContrato");
                    descuento.montoDescuento        = rs.getDouble("MontoDescuento");
                    descuento.descripcion           = rs.getString("Descripcion");
                    descuento.fechaAlta             = rs.getTimestamp("FechaAlta");
                    descuento.idUsuarioAlta         = rs.getLong("IdUsuarioAlta");
                    descuento.estatus               = rs.getString("Estatus");
                    descuento.idUsuarioModificacion = rs.getLong("IdUsuarioModificacion");
                    lista.add(descuento);
                }
            }
        }
        return lista;
    }

*/

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
