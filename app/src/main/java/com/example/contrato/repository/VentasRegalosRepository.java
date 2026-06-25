package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasRegalos;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasRegalosRepository {
    public boolean huboCambios(List<VentasRegalos> actuales, List<String> nuevos) {
        if (actuales.size() != nuevos.size()) return true;
        List<String> descripcionesActuales = new ArrayList<>();
        for (VentasRegalos r : actuales) descripcionesActuales.add(r.descripcion);
        for (String nuevo : nuevos) {
            if (!descripcionesActuales.remove(nuevo)) return true;
        }
        return false;
    }
    //104

    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Regalos_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 10.2
    public void insert(VentasRegalos r) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Regalos_Insert(?,?,?,?)}")) {
            cs.setLong(1,   r.idRegalo);
            cs.setLong(2,   r.idContrato);
            cs.setString(3, r.descripcion);
            cs.setLong(4,   r.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 10.3
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Regalos_DesactivarPorContrato(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 10.4
    public List<VentasRegalos> getByContratoId(long idContrato) throws SQLException {
        List<VentasRegalos> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Regalos_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
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
    }}
    /*

    //Aqui empieza cintas

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
        String sql = "INSERT INTO PMT_App_Ventas_Regalos (IdRegalo, IdContrato, Descripcion, FechaAlta, IdUsuarioAlta, Estatus, IdUsuarioModificacion, FechaModificacion) VALUES (?, ?, ?, GETDATE(), ?, 'A', NULL, NULL)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.idRegalo);
            ps.setLong(2, r.idContrato);
            ps.setString(3, r.descripcion);
            ps.setLong(4, r.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    // Desactiva todos los registros activos de un contrato (usar antes del loop de insert)
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Regalos SET Estatus = 'C', IdUsuarioModificacion = ?, FechaModificacion = GETDATE() WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conexion = DbConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, idUsuarioModificacion);
            ps.setLong(2, idContrato);
            ps.executeUpdate();
        }
    }

    public List<VentasRegalos> getByContratoId(long idContrato) throws SQLException {
        List<VentasRegalos> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Regalos WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasRegalos r = new VentasRegalos();
                    r.idRegalo      = rs.getLong("IdRegalo");
                    r.idContrato    = rs.getLong("IdContrato");
                    r.descripcion   = rs.getString("Descripcion");
                    r.fechaAlta     = rs.getTimestamp("FechaAlta");
                    r.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(r);
                }
            }
        }
        return list;
    }
//
}
*/