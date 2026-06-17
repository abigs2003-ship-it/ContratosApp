package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasInformacionGeneral;

import java.sql.*;
import java.util.Objects;

public class VentasInformacionGeneralRepository {
    // Baja lógica: marca el registro activo como 'C' sin eliminar físicamente

    private void mapResultSet(VentasInformacionGeneral g, ResultSet rs) throws SQLException {
        g.idDatosVenta = rs.getLong("IdDatosVenta");
        g.idContrato   = rs.getLong("IdContrato");
        g.tipoDir      = rs.getString("TipoDir");
        g.calle        = rs.getString("Calle");
        g.noExt        = rs.getString("NoExt");
        g.noInt        = rs.getString("NoInt");
        g.poBox        = rs.getString("POBox");
        g.box          = rs.getString("BOX");
        g.cmr          = rs.getString("CMR");
        g.apo          = rs.getString("APO");
        g.colonia      = rs.getString("Colonia");
        g.delegacion   = rs.getString("Delegacion");
        g.ciudad       = rs.getString("Ciudad");
        g.estado       = rs.getString("Estado");
        g.pais         = rs.getString("Pais");
        g.cp           = rs.getString("CP");
        g.linea1       = rs.getString("Linea1");
        g.linea2       = rs.getString("Linea2");
        g.linea3       = rs.getString("Linea3");
        g.linea4       = rs.getString("Linea4");
        g.linea5       = rs.getString("Linea5");
        g.telefonoDefault   = rs.getString("TelefonoDefault");
        g.ladaCasa1         = rs.getString("LadaCasa1");
        g.telefonoCasa1     = rs.getString("TelefonoCasa1");
        g.whatsAppCasa1     = rs.getBoolean("WhatsAppCasa1");
        g.ladaCasa2         = rs.getString("LadaCasa2");
        g.telefonoCasa2     = rs.getString("TelefonoCasa2");
        g.whatsAppCasa2     = rs.getBoolean("WhatsAppCasa2");
        g.ladaCelular1      = rs.getString("LadaCelular1");
        g.telefonoCelular1  = rs.getString("TelefonoCelular1");
        g.whatsAppCelular1  = rs.getBoolean("WhatsAppCelular1");
        g.ladaCelular2      = rs.getString("LadaCelular2");
        g.telefonoCelular2  = rs.getString("TelefonoCelular2");
        g.whatsAppCelular2  = rs.getBoolean("WhatsAppCelular2");
        g.ladaMensajes      = rs.getString("LadaMensajes");
        g.telefonoMensajes  = rs.getString("TelefonoMensajes");
        g.whatsAppMensajes  = rs.getBoolean("WhatsAppMensajes");
        g.ladaOficina1      = rs.getString("LadaOficina1");
        g.telefonoOficina1  = rs.getString("TelefonoOficina1");
        g.whatsAppOficina1  = rs.getBoolean("WhatsAppOficina1");
        g.ladaOficina2      = rs.getString("LadaOficina2");
        g.telefonoOficina2  = rs.getString("TelefonoOficina2");
        g.whatsAppOficina2  = rs.getBoolean("WhatsAppOficina2");
        g.nacionalidad      = rs.getString("Nacionalidad");
        g.email1            = rs.getString("Email1");
        g.email2            = rs.getString("Email2");
        g.email3            = rs.getString("Email3");
        g.email4            = rs.getString("Email4");
        g.fechaAlta         = rs.getTimestamp("FechaAlta");
        g.idUsuarioAlta     = rs.getLong("IdUsuarioAlta");
        g.estatus           = rs.getString("Estatus");
    }


    public boolean huboCambios(VentasInformacionGeneral a, VentasInformacionGeneral n) {
        return !Objects.equals(a.tipoDir,          n.tipoDir)
                || !Objects.equals(a.pais,             n.pais)
                || !Objects.equals(a.nacionalidad,     n.nacionalidad)
                || !Objects.equals(a.calle,            n.calle)
                || !Objects.equals(a.noExt,            n.noExt)
                || !Objects.equals(a.noInt,            n.noInt)
                || !Objects.equals(a.colonia,          n.colonia)
                || !Objects.equals(a.delegacion,       n.delegacion)
                || !Objects.equals(a.ciudad,           n.ciudad)
                || !Objects.equals(a.estado,           n.estado)
                || !Objects.equals(a.cp,               n.cp)
                || !Objects.equals(a.poBox,            n.poBox)
                || !Objects.equals(a.box,              n.box)
                || !Objects.equals(a.cmr,              n.cmr)
                || !Objects.equals(a.apo,              n.apo)
                || !Objects.equals(a.linea1,           n.linea1)
                || !Objects.equals(a.linea2,           n.linea2)
                || !Objects.equals(a.linea3,           n.linea3)
                || !Objects.equals(a.linea4,           n.linea4)
                || !Objects.equals(a.linea5,           n.linea5)
                || !Objects.equals(a.telefonoDefault,  n.telefonoDefault)
                || !Objects.equals(a.ladaCasa1,        n.ladaCasa1)
                || !Objects.equals(a.telefonoCasa1,    n.telefonoCasa1)
                ||  a.whatsAppCasa1 !=               n.whatsAppCasa1
                || !Objects.equals(a.ladaCasa2,        n.ladaCasa2)
                || !Objects.equals(a.telefonoCasa2,    n.telefonoCasa2)
                ||  a.whatsAppCasa2 !=               n.whatsAppCasa2
                || !Objects.equals(a.ladaCelular1,     n.ladaCelular1)
                || !Objects.equals(a.telefonoCelular1, n.telefonoCelular1)
                ||  a.whatsAppCelular1 !=            n.whatsAppCelular1
                || !Objects.equals(a.ladaCelular2,     n.ladaCelular2)
                || !Objects.equals(a.telefonoCelular2, n.telefonoCelular2)
                ||  a.whatsAppCelular2 !=            n.whatsAppCelular2
                || !Objects.equals(a.ladaMensajes,     n.ladaMensajes)
                || !Objects.equals(a.telefonoMensajes, n.telefonoMensajes)
                ||  a.whatsAppMensajes !=            n.whatsAppMensajes
                || !Objects.equals(a.ladaOficina1,     n.ladaOficina1)
                || !Objects.equals(a.telefonoOficina1, n.telefonoOficina1)
                ||  a.whatsAppOficina1 !=            n.whatsAppOficina1
                || !Objects.equals(a.ladaOficina2,     n.ladaOficina2)
                || !Objects.equals(a.telefonoOficina2, n.telefonoOficina2)
                ||  a.whatsAppOficina2 !=            n.whatsAppOficina2
                || !Objects.equals(a.email1,           n.email1)
                || !Objects.equals(a.email2,           n.email2)
                || !Objects.equals(a.email3,           n.email3)
                || !Objects.equals(a.email4,           n.email4);
    }

    // aqui empieza 104
    /*
    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_InfoGeneral_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 5.2
    public void insert(VentasInformacionGeneral g) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(buildInsertCall())) {
            fillCallableStatement(cs, g, g.idDatosVenta, g.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 5.3 — SP desactiva e inserta en una transaccion
    public void replaceByContrato(VentasInformacionGeneral g, long idUsuario) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(buildReplaceCall())) {
            fillReplaceCallableStatement(cs, g, idUsuario);
            cs.executeUpdate();
        }
    }

    // 5.4
    public VentasInformacionGeneral getByContratoId(long idContrato) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_InfoGeneral_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    VentasInformacionGeneral g = new VentasInformacionGeneral();
                    mapResultSet(g, rs);
                    return g;
                }
            }
        }
        return null;
    }

    // 5.5


    private String buildInsertCall() {
        return "{call sp_App_InfoGeneral_Insert(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
    }

    private String buildReplaceCall() {
        return "{call sp_App_InfoGeneral_ReplaceByContrato(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
    }


    private void fillCallableStatement(CallableStatement cs, VentasInformacionGeneral g,
                                       long idDatosVenta, long idUsuarioAlta) throws SQLException {
        cs.setLong(1,    idDatosVenta);
        cs.setLong(2,    g.idContrato);
        cs.setString(3,  g.tipoDir);
        cs.setString(4,  g.calle);
        cs.setString(5,  g.noExt);
        cs.setString(6,  g.noInt);
        cs.setString(7,  g.poBox);
        cs.setString(8,  g.box);
        cs.setString(9,  g.cmr);
        cs.setString(10, g.apo);
        cs.setString(11, g.colonia);
        cs.setString(12, g.delegacion);
        cs.setString(13, g.ciudad);
        cs.setString(14, g.estado);
        cs.setString(15, g.pais);
        cs.setString(16, g.cp);
        cs.setString(17, g.linea1);
        cs.setString(18, g.linea2);
        cs.setString(19, g.linea3);
        cs.setString(20, g.linea4);
        cs.setString(21, g.linea5);
        cs.setString(22, g.telefonoDefault);
        cs.setString(23, g.ladaCasa1);
        cs.setString(24, g.telefonoCasa1);
        cs.setBoolean(25, g.whatsAppCasa1);
        cs.setString(26, g.ladaCasa2);
        cs.setString(27, g.telefonoCasa2);
        cs.setBoolean(28, g.whatsAppCasa2);
        cs.setString(29, g.ladaCelular1);
        cs.setString(30, g.telefonoCelular1);
        cs.setBoolean(31, g.whatsAppCelular1);
        cs.setString(32, g.ladaCelular2);
        cs.setString(33, g.telefonoCelular2);
        cs.setBoolean(34, g.whatsAppCelular2);
        cs.setString(35, g.ladaMensajes);
        cs.setString(36, g.telefonoMensajes);
        cs.setBoolean(37, g.whatsAppMensajes);
        cs.setString(38, g.ladaOficina1);
        cs.setString(39, g.telefonoOficina1);
        cs.setBoolean(40, g.whatsAppOficina1);
        cs.setString(41, g.ladaOficina2);
        cs.setString(42, g.telefonoOficina2);
        cs.setBoolean(43, g.whatsAppOficina2);
        cs.setString(44, g.nacionalidad);
        cs.setString(45, g.email1);
        cs.setString(46, g.email2);
        cs.setString(47, g.email3);
        cs.setString(48, g.email4);
        cs.setLong(49,   idUsuarioAlta);
    }


    private void fillReplaceCallableStatement(CallableStatement cs,
                                              VentasInformacionGeneral g,
                                              long idUsuario) throws SQLException {
        cs.setLong(1,    g.idContrato);
        cs.setString(2,  g.tipoDir);
        cs.setString(3,  g.calle);
        cs.setString(4,  g.noExt);
        cs.setString(5,  g.noInt);
        cs.setString(6,  g.poBox);
        cs.setString(7,  g.box);
        cs.setString(8,  g.cmr);
        cs.setString(9,  g.apo);
        cs.setString(10, g.colonia);
        cs.setString(11, g.delegacion);
        cs.setString(12, g.ciudad);
        cs.setString(13, g.estado);
        cs.setString(14, g.pais);
        cs.setString(15, g.cp);
        cs.setString(16, g.linea1);
        cs.setString(17, g.linea2);
        cs.setString(18, g.linea3);
        cs.setString(19, g.linea4);
        cs.setString(20, g.linea5);
        cs.setString(21, g.telefonoDefault);
        cs.setString(22, g.ladaCasa1);
        cs.setString(23, g.telefonoCasa1);
        cs.setBoolean(24, g.whatsAppCasa1);
        cs.setString(25, g.ladaCasa2);
        cs.setString(26, g.telefonoCasa2);
        cs.setBoolean(27, g.whatsAppCasa2);
        cs.setString(28, g.ladaCelular1);
        cs.setString(29, g.telefonoCelular1);
        cs.setBoolean(30, g.whatsAppCelular1);
        cs.setString(31, g.ladaCelular2);
        cs.setString(32, g.telefonoCelular2);
        cs.setBoolean(33, g.whatsAppCelular2);
        cs.setString(34, g.ladaMensajes);
        cs.setString(35, g.telefonoMensajes);
        cs.setBoolean(36, g.whatsAppMensajes);
        cs.setString(37, g.ladaOficina1);
        cs.setString(38, g.telefonoOficina1);
        cs.setBoolean(39, g.whatsAppOficina1);
        cs.setString(40, g.ladaOficina2);
        cs.setString(41, g.telefonoOficina2);
        cs.setBoolean(42, g.whatsAppOficina2);
        cs.setString(43, g.nacionalidad);
        cs.setString(44, g.email1);
        cs.setString(45, g.email2);
        cs.setString(46, g.email3);
        cs.setString(47, g.email4);
        cs.setLong(48,   idUsuario);
    }

*/
    //Aqui empieza cintas
   // /*

    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdDatosVenta), 0) + 1 AS NextId FROM PMT_App_Ventas_Informacion_General";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // Reutiliza la conexión activa para no bloquear la transacción
    private long getNextIdConConexion(Connection conn) throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdDatosVenta), 0) + 1 AS NextId FROM PMT_App_Ventas_Informacion_General";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    public void insert(VentasInformacionGeneral g) throws SQLException {
        String sql =
                "INSERT INTO PMT_App_Ventas_Informacion_General (" +
                        "IdDatosVenta, IdContrato, TipoDir, Calle, NoExt, NoInt, POBox, BOX, CMR, APO," +
                        "Colonia, Delegacion, Ciudad, Estado, Pais, CP," +
                        "Linea1, Linea2, Linea3, Linea4, Linea5," +
                        "TelefonoDefault, LadaCasa1, TelefonoCasa1, WhatsAppCasa1," +
                        "LadaCasa2, TelefonoCasa2, WhatsAppCasa2," +
                        "LadaCelular1, TelefonoCelular1, WhatsAppCelular1," +
                        "LadaCelular2, TelefonoCelular2, WhatsAppCelular2," +
                        "LadaMensajes, TelefonoMensajes, WhatsAppMensajes," +
                        "LadaOficina1, TelefonoOficina1, WhatsAppOficina1," +
                        "LadaOficina2, TelefonoOficina2, WhatsAppOficina2," +
                        "Nacionalidad, Email1, Email2, Email3, Email4," +
                        "FechaAlta, IdUsuarioAlta, Estatus, IdUsuarioModificacion, FechaModificacion) " +
                        "VALUES (" +
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                        "?, ?, ?, ?, ?, ?," +
                        "?, ?, ?, ?, ?," +
                        "?, ?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?, ?, ?," +
                        "GETDATE(), ?, 'A', NULL, NULL)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillPreparedStatement(ps, g);
            ps.setLong(49, g.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    // Cancela el registro activo e inserta uno nuevo — todo en una sola transacción
    public void replaceByContrato(VentasInformacionGeneral g, long idUsuario) throws SQLException {
        String sqlDesactivar =
                "UPDATE PMT_App_Ventas_Informacion_General " +
                        "SET Estatus='C', IdUsuarioModificacion=?, FechaModificacion=GETDATE() " +
                        "WHERE IdContrato=? AND Estatus='A'";

        String sqlInsert =
                "INSERT INTO PMT_App_Ventas_Informacion_General (" +
                        "IdDatosVenta, IdContrato, TipoDir, Calle, NoExt, NoInt, POBox, BOX, CMR, APO," +
                        "Colonia, Delegacion, Ciudad, Estado, Pais, CP," +
                        "Linea1, Linea2, Linea3, Linea4, Linea5," +
                        "TelefonoDefault, LadaCasa1, TelefonoCasa1, WhatsAppCasa1," +
                        "LadaCasa2, TelefonoCasa2, WhatsAppCasa2," +
                        "LadaCelular1, TelefonoCelular1, WhatsAppCelular1," +
                        "LadaCelular2, TelefonoCelular2, WhatsAppCelular2," +
                        "LadaMensajes, TelefonoMensajes, WhatsAppMensajes," +
                        "LadaOficina1, TelefonoOficina1, WhatsAppOficina1," +
                        "LadaOficina2, TelefonoOficina2, WhatsAppOficina2," +
                        "Nacionalidad, Email1, Email2, Email3, Email4," +
                        "FechaAlta, IdUsuarioAlta, Estatus, IdUsuarioModificacion, FechaModificacion) " +
                        "VALUES (" +
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                        "?, ?, ?, ?, ?, ?," +
                        "?, ?, ?, ?, ?," +
                        "?, ?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?," +
                        "?, ?, ?, ?, ?," +
                        "GETDATE(), ?, 'A', NULL, NULL)";

        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cancelar registro activo (historial)
                try (PreparedStatement ps = conn.prepareStatement(sqlDesactivar)) {
                    ps.setLong(1, idUsuario);
                    ps.setLong(2, g.idContrato);
                    ps.executeUpdate();
                }

                // 2. Obtener siguiente ID usando la MISMA conexión (evita el lock)
                g.idDatosVenta  = getNextIdConConexion(conn);
                g.idUsuarioAlta = idUsuario;

                // 3. Insertar nuevo registro activo
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    fillPreparedStatement(ps, g);
                    ps.setLong(49, idUsuario);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                // Revertir si ocurre un error
                conn.rollback();
                throw e;
            }
        }
    }

    // Solo devuelve el registro activo (Estatus = 'A') para mostrar en pantalla
    public VentasInformacionGeneral getByContratoId(long idContrato) throws SQLException {
        String sql =
                "SELECT * FROM PMT_App_Ventas_Informacion_General " +
                        "WHERE IdContrato=? AND Estatus='A'";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasInformacionGeneral g = new VentasInformacionGeneral();
                    mapResultSet(g, rs);
                    return g;
                }
            }
        }
        return null;
    }
    private void fillPreparedStatement(PreparedStatement ps, VentasInformacionGeneral g) throws SQLException {
        ps.setLong(1, g.idDatosVenta);
        ps.setLong(2, g.idContrato);
        ps.setString(3, g.tipoDir);
        ps.setString(4, g.calle);
        ps.setString(5, g.noExt);
        ps.setString(6, g.noInt);
        ps.setString(7, g.poBox);
        ps.setString(8, g.box);
        ps.setString(9, g.cmr);
        ps.setString(10, g.apo);
        ps.setString(11, g.colonia);
        ps.setString(12, g.delegacion);
        ps.setString(13, g.ciudad);
        ps.setString(14, g.estado);
        ps.setString(15, g.pais);
        ps.setString(16, g.cp);
        ps.setString(17, g.linea1);
        ps.setString(18, g.linea2);
        ps.setString(19, g.linea3);
        ps.setString(20, g.linea4);
        ps.setString(21, g.linea5);
        ps.setString(22, g.telefonoDefault);
        ps.setString(23, g.ladaCasa1);
        ps.setString(24, g.telefonoCasa1);
        ps.setBoolean(25, g.whatsAppCasa1);
        ps.setString(26, g.ladaCasa2);
        ps.setString(27, g.telefonoCasa2);
        ps.setBoolean(28, g.whatsAppCasa2);
        ps.setString(29, g.ladaCelular1);
        ps.setString(30, g.telefonoCelular1);
        ps.setBoolean(31, g.whatsAppCelular1);
        ps.setString(32, g.ladaCelular2);
        ps.setString(33, g.telefonoCelular2);
        ps.setBoolean(34, g.whatsAppCelular2);
        ps.setString(35, g.ladaMensajes);
        ps.setString(36, g.telefonoMensajes);
        ps.setBoolean(37, g.whatsAppMensajes);
        ps.setString(38, g.ladaOficina1);
        ps.setString(39, g.telefonoOficina1);
        ps.setBoolean(40, g.whatsAppOficina1);
        ps.setString(41, g.ladaOficina2);
        ps.setString(42, g.telefonoOficina2);
        ps.setBoolean(43, g.whatsAppOficina2);
        ps.setString(44, g.nacionalidad);
        ps.setString(45, g.email1);
        ps.setString(46, g.email2);
        ps.setString(47, g.email3);
        ps.setString(48, g.email4);
        // índice 49 = IdUsuarioAlta → se setea fuera de este método
    }
  // */
}