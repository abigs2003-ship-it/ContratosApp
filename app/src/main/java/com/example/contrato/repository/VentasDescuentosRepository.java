package com.example.contrato.repository;

import static java.lang.Double.parseDouble;

import com.example.contrato.ContratoModelo;
import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasDescuentos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VentasDescuentosRepository {


    public boolean huboCambios(List<VentasDescuentos> actuales, List<ContratoModelo.DescuentoDetalle> nuevos) {
        if (actuales.size() != nuevos.size()) return true;
        for (int i = 0; i < actuales.size(); i++) {
            VentasDescuentos a = actuales.get(i);
            ContratoModelo.DescuentoDetalle n = nuevos.get(i);
            if (!Objects.equals(a.descripcion, n.descripcion)
                    || !Objects.equals(a.montoDescuento, parseDouble(n.monto)))
                return true;
        }
        return false;
    }
    // aqui empieza 104


    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Descuentos_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 2.2
    public void insert(VentasDescuentos d) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Descuentos_Insert(?,?,?,?,?)}")) {
            cs.setLong(1, d.idDescuento);
            cs.setLong(2, d.idContrato);
            cs.setObject(3, d.montoDescuento);
            cs.setString(4, d.descripcion);
            cs.setLong(5, d.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 2.3
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Descuentos_DesactivarPorContrato(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 2.4
    public void update(VentasDescuentos descuento, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Descuentos_Update(?,?,?,?,?)}")) {
            cs.setLong(1, descuento.idContrato);
            cs.setObject(2, descuento.montoDescuento);
            cs.setString(3, descuento.descripcion);
            cs.setLong(4, descuento.idUsuarioAlta);
            cs.setLong(5, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 2.5
    public List<VentasDescuentos> getByContratoId(long idContrato) throws SQLException {
        List<VentasDescuentos> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Descuentos_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
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
}
/*

    //Aqui empieza cintas

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

    public void update(VentasDescuentos descuento, long idUsuarioModificacion) throws SQLException {
        String sqlDesactivar = "UPDATE PMT_App_Ventas_Descuentos SET Estatus = 'C', IdUsuarioModificacion  = ?, FechaModificacion = GETDATE() WHERE IdContrato = ? AND Estatus    = 'A' ";

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
                String sqlInsertar = "INSERT INTO PMT_App_Ventas_Descuentos (IdDescuento, IdContrato, MontoDescuento, Descripcion, FechaAlta, IdUsuarioAlta, Estatus) VALUES (?, ?, ?, ?, GETDATE(), ?, 'A')";
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

    // Reutiliza una conexión existente para ejecutarse dentro de la transacción de actualizar()
    private long obtenerSiguienteIdConConexion(Connection conexion) throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdDescuento), 0) + 1 AS SiguienteId FROM PMT_App_Ventas_Descuentos";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("SiguienteId");
        }
        return 1;
    }


    public void insert(VentasDescuentos d) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Descuentos (IdDescuento, IdContrato, MontoDescuento, Descripcion, FechaAlta, IdUsuarioAlta, Estatus) VALUES (?, ?, ?, ?, GETDATE(), ?, 'A')";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, d.idDescuento);
            ps.setLong(2, d.idContrato);
            ps.setObject(3, d.montoDescuento);
            ps.setString(4, d.descripcion);
            ps.setLong(5, d.idUsuarioAlta);
            ps.executeUpdate();
        }
    }


    // Desactiva todos los registros activos de un contrato (para usar antes del loop)
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Descuentos SET Estatus = 'C', IdUsuarioModificacion = ?, FechaModificacion = GETDATE() WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conexion = DbConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, idUsuarioModificacion);
            ps.setLong(2, idContrato);
            ps.executeUpdate();
        }
    }

    public List<VentasDescuentos> getByContratoId(long idContrato) throws SQLException {
        List<VentasDescuentos> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Descuentos WHERE IdContrato = ? AND Estatus = 'A'";
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
    }*/


