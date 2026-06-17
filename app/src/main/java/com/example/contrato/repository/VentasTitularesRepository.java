package com.example.contrato.repository;

import static java.lang.Long.parseLong;

import com.example.contrato.ContratoModelo;
import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasTitulares;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VentasTitularesRepository {
    public boolean huboCambios(List<VentasTitulares> actuales, List<ContratoModelo.Persona> nuevos, String tipo) {
        List<VentasTitulares> filtrados = new ArrayList<>();
        for (VentasTitulares t : actuales) {
            if (tipo.equals(t.tipoTitular)) filtrados.add(t);
        }
        if (filtrados.size() != nuevos.size()) return true;
        for (int i = 0; i < filtrados.size(); i++) {
            VentasTitulares a = filtrados.get(i);
            ContratoModelo.Persona n = nuevos.get(i);
            if (!Objects.equals(a.nombre,    n.nombre)
                    || !Objects.equals(a.paterno,   n.paterno)
                    || !Objects.equals(a.materno,   n.materno)
                    || !Objects.equals(a.ocupacion, n.ocupacion)
                    ||  a.parentesco != parseLong(n.parentesco))
                return true;
        }
        return false;
    }

    // 104
    /*
    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 7.2
    public void insert(VentasTitulares t) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_Insert(?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setLong(1,    t.idTitular);
            cs.setLong(2,    t.idContrato);
            cs.setString(3,  t.nombre);
            cs.setString(4,  t.paterno);
            cs.setString(5,  t.materno);
            cs.setString(6,  t.tipoTitular);
            cs.setInt(7,     t.tipoRegistro);
            cs.setInt(8,     t.ordenTitulares);
            cs.setLong(9,    t.idUsuarioAlta);
            cs.setDate(10,   t.fechaCumpleaños);
            cs.setString(11, t.ocupacion);
            cs.setLong(12,   t.parentesco);
            cs.executeUpdate();
        }
    }

    // 7.3
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_DesactivarPorContrato(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 7.4
    public void desactivarPorTipo(long idContrato, String tipo, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_DesactivarPorTipo(?,?,?)}")) {
            cs.setLong(1,   idContrato);
            cs.setString(2, tipo);
            cs.setLong(3,   idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 7.5
    public List<VentasTitulares> getByContratoId(long idContrato) throws SQLException {
        List<VentasTitulares> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    VentasTitulares t = new VentasTitulares();
                    t.idTitular      = rs.getLong("IdTitular");
                    t.idContrato     = rs.getLong("IdContrato");
                    t.nombre         = rs.getString("Nombre");
                    t.paterno        = rs.getString("Paterno");
                    t.materno        = rs.getString("Materno");
                    t.tipoTitular    = rs.getString("TipoTitular");
                    t.tipoRegistro   = rs.getInt("TipoRegistro");
                    t.ordenTitulares = rs.getInt("OrdenTitulares");
                    t.idUsuarioAlta  = rs.getLong("IdUsuarioAlta");
                    t.fechaAlta      = rs.getTimestamp("FechaAlta");
                    t.fechaCumpleaños = rs.getDate("FechaCumpleaños");
                    t.ocupacion      = rs.getString("Ocupacion");
                    t.parentesco     = rs.getLong("Parentesco");
                    t.estatus        = rs.getString("Estatus");
                    list.add(t);
                }
            }
        }
        return list;
    }
*/

//cintas
    // /*
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdTitular), 0) + 1 AS NextId FROM PMT_App_Ventas_Titulares";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    public void insert(VentasTitulares t) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Titulares " +
                "(IdTitular, IdContrato, Nombre, Paterno, Materno, TipoTitular, TipoRegistro, OrdenTitulares, " +
                "IdUsuarioAlta, FechaAlta, FechaCumpleaños, Ocupacion, Parentesco, " +
                "Estatus, IdUsuarioModificacion, FechaModificacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, 'A', NULL, NULL)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1,    t.idTitular);
            ps.setLong(2,    t.idContrato);
            ps.setString(3,  t.nombre);
            ps.setString(4,  t.paterno);
            ps.setString(5,  t.materno);
            ps.setString(6,  t.tipoTitular);
            ps.setInt(7,     t.tipoRegistro);
            ps.setInt(8,     t.ordenTitulares);
            ps.setLong(9,    t.idUsuarioAlta);
            // índice 10 = FechaAlta → GETDATE() en el SQL
            ps.setDate(10,   t.fechaCumpleaños);
            ps.setString(11, t.ocupacion);
            ps.setLong(12,   t.parentesco);
            ps.executeUpdate();
        }
    }



    // Solo devuelve registros activos (Estatus = 'A') para mostrar en pantalla
    public List<VentasTitulares> getByContratoId(long idContrato) throws SQLException {
        List<VentasTitulares> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Titulares WHERE IdContrato=? AND Estatus='A' " +
                "ORDER BY TipoTitular, OrdenTitulares";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasTitulares t = new VentasTitulares();
                    t.idTitular       = rs.getLong("IdTitular");
                    t.idContrato      = rs.getLong("IdContrato");
                    t.nombre          = rs.getString("Nombre");
                    t.paterno         = rs.getString("Paterno");
                    t.materno         = rs.getString("Materno");
                    t.tipoTitular     = rs.getString("TipoTitular");
                    t.tipoRegistro    = rs.getInt("TipoRegistro");
                    t.ordenTitulares  = rs.getInt("OrdenTitulares");
                    t.idUsuarioAlta   = rs.getLong("IdUsuarioAlta");
                    t.fechaAlta       = rs.getTimestamp("FechaAlta");
                    t.fechaCumpleaños = rs.getDate("FechaCumpleaños");
                    t.ocupacion       = rs.getString("Ocupacion");
                    t.parentesco      = rs.getLong("Parentesco");
                    t.estatus         = rs.getString("Estatus");
                    list.add(t);
                }
            }
        }
        return list;
    }


    public void desactivarPorTipo(long idContrato, String tipo, long idUsuarioModificacion) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Titulares " +
                "SET Estatus='C', IdUsuarioModificacion=?, FechaModificacion=GETDATE() " +
                "WHERE IdContrato=? AND TipoTitular=? AND Estatus='A'";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuarioModificacion);
            ps.setLong(2, idContrato);
            ps.setString(3, tipo);
            ps.executeUpdate();
        }
    }
   // */

}
