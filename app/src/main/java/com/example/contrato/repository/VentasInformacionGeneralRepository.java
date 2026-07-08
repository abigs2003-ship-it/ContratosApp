package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasInformacionGeneral;

import java.sql.*;
import java.util.Objects;

public class VentasInformacionGeneralRepository {
    private String nz(String v) { return v == null ? "" : v; }
    private void mapResultSet(VentasInformacionGeneral g, ResultSet rs) throws SQLException {
        g.idDatosVenta = rs.getLong("IdDatosVenta");
        g.idContrato = rs.getLong("IdContrato");
        g.tipoDir = rs.getString("TipoDir");
        g.calle = rs.getString("Calle");
        g.noExt = rs.getString("NoExt");
        g.noInt = rs.getString("NoInt");
        g.poBox = rs.getString("POBox");
        g.box = rs.getString("BOX");
        g.cmr = rs.getString("CMR");
        g.apo = rs.getString("APO");
        g.colonia = rs.getString("Colonia");
        g.delegacion = rs.getString("Delegacion");
        g.ciudad = rs.getString("Ciudad");
        g.estado = rs.getString("Estado");
        g.pais = rs.getString("Pais");
        g.cp = rs.getString("CP");
        g.linea1 = rs.getString("Linea1");
        g.linea2 = rs.getString("Linea2");
        g.linea3 = rs.getString("Linea3");
        g.linea4 = rs.getString("Linea4");
        g.linea5 = rs.getString("Linea5");
        g.telefonoDefault = rs.getString("TelefonoDefault");
        g.ladaCasa1 = rs.getString("LadaCasa1");
        g.telefonoCasa1 = rs.getString("TelefonoCasa1");
        g.whatsAppCasa1 = rs.getBoolean("WhatsAppCasa1");
        g.ladaCasa2 = rs.getString("LadaCasa2");
        g.telefonoCasa2 = rs.getString("TelefonoCasa2");
        g.whatsAppCasa2 = rs.getBoolean("WhatsAppCasa2");
        g.ladaCelular1 = rs.getString("LadaCelular1");
        g.telefonoCelular1 = rs.getString("TelefonoCelular1");
        g.whatsAppCelular1 = rs.getBoolean("WhatsAppCelular1");
        g.ladaCelular2 = rs.getString("LadaCelular2");
        g.telefonoCelular2 = rs.getString("TelefonoCelular2");
        g.whatsAppCelular2 = rs.getBoolean("WhatsAppCelular2");
        g.ladaMensajes = rs.getString("LadaMensajes");
        g.telefonoMensajes = rs.getString("TelefonoMensajes");
        g.whatsAppMensajes = rs.getBoolean("WhatsAppMensajes");
        g.ladaOficina1 = rs.getString("LadaOficina1");
        g.telefonoOficina1 = rs.getString("TelefonoOficina1");
        g.whatsAppOficina1 = rs.getBoolean("WhatsAppOficina1");
        g.ladaOficina2 = rs.getString("LadaOficina2");
        g.telefonoOficina2 = rs.getString("TelefonoOficina2");
        g.whatsAppOficina2 = rs.getBoolean("WhatsAppOficina2");
        g.ladaCelular3 = rs.getString("LadaCelular3");
        g.telefonoCelular3 = rs.getString("TelefonoCelular3");
        g.whatsAppCelular3 = rs.getBoolean("WhatsAppCelular3");
        g.nacionalidad = rs.getString("Nacionalidad");
        g.email1 = rs.getString("Email1");
        g.email2 = rs.getString("Email2");
        g.email3 = rs.getString("Email3");
        g.email4 = rs.getString("Email4");
        g.fechaAlta = rs.getTimestamp("FechaAlta");
        g.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
        g.estatus = rs.getString("Estatus");
    }


    public boolean huboCambios(VentasInformacionGeneral a, VentasInformacionGeneral n) {
        return !Objects.equals(a.tipoDir, n.tipoDir)
                || !Objects.equals(a.pais, n.pais)
                || !Objects.equals(a.nacionalidad, n.nacionalidad)
                || !Objects.equals(a.calle, n.calle)
                || !Objects.equals(a.noExt, n.noExt)
                || !Objects.equals(a.noInt, n.noInt)
                || !Objects.equals(a.colonia, n.colonia)
                || !Objects.equals(a.delegacion, n.delegacion)
                || !Objects.equals(a.ciudad, n.ciudad)
                || !Objects.equals(a.estado, n.estado)
                || !Objects.equals(a.cp, n.cp)
                || !Objects.equals(a.poBox, n.poBox)
                || !Objects.equals(a.box, n.box)
                || !Objects.equals(a.cmr, n.cmr)
                || !Objects.equals(a.apo, n.apo)
                || !Objects.equals(a.linea1, n.linea1)
                || !Objects.equals(a.linea2, n.linea2)
                || !Objects.equals(a.linea3, n.linea3)
                || !Objects.equals(a.linea4, n.linea4)
                || !Objects.equals(a.linea5, n.linea5)
                || !Objects.equals(a.telefonoDefault, n.telefonoDefault)
                || !Objects.equals(a.ladaCasa1, n.ladaCasa1)
                || !Objects.equals(a.telefonoCasa1, n.telefonoCasa1)
                || a.whatsAppCasa1 != n.whatsAppCasa1
                || !Objects.equals(a.ladaCasa2, n.ladaCasa2)
                || !Objects.equals(a.telefonoCasa2, n.telefonoCasa2)
                || a.whatsAppCasa2 != n.whatsAppCasa2
                || !Objects.equals(a.ladaCelular1, n.ladaCelular1)
                || !Objects.equals(a.telefonoCelular1, n.telefonoCelular1)
                || a.whatsAppCelular1 != n.whatsAppCelular1
                || !Objects.equals(a.ladaCelular2, n.ladaCelular2)
                || !Objects.equals(a.telefonoCelular2, n.telefonoCelular2)
                || a.whatsAppCelular2 != n.whatsAppCelular2
                || !Objects.equals(a.ladaCelular3, n.ladaCelular3)
                || !Objects.equals(a.telefonoCelular3, n.telefonoCelular3)
                || a.whatsAppCelular3 != n.whatsAppCelular3
                || !Objects.equals(a.ladaMensajes, n.ladaMensajes)
                || !Objects.equals(a.telefonoMensajes, n.telefonoMensajes)
                || a.whatsAppMensajes != n.whatsAppMensajes
                || !Objects.equals(a.ladaOficina1, n.ladaOficina1)
                || !Objects.equals(a.telefonoOficina1, n.telefonoOficina1)
                || a.whatsAppOficina1 != n.whatsAppOficina1
                || !Objects.equals(a.ladaOficina2, n.ladaOficina2)
                || !Objects.equals(a.telefonoOficina2, n.telefonoOficina2)
                || a.whatsAppOficina2 != n.whatsAppOficina2
                || !Objects.equals(a.email1, n.email1)
                || !Objects.equals(a.email2, n.email2)
                || !Objects.equals(a.email3, n.email3)
                || !Objects.equals(a.email4, n.email4);
    }

    // aqui empieza 104

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
        return "{call sp_App_InfoGeneral_Insert(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
    }

    private String buildReplaceCall() {
        return "{call sp_App_InfoGeneral_ReplaceByContrato(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
    }


    private void fillCallableStatement(CallableStatement cs, VentasInformacionGeneral g,
                                       long idDatosVenta, long idUsuarioAlta) throws SQLException {
        cs.setLong(1, idDatosVenta);
        cs.setLong(2, g.idContrato);
        cs.setString(3, nz(g.tipoDir));
        cs.setString(4, nz(g.calle));
        cs.setString(5, nz(g.noExt));
        cs.setString(6, nz(g.noInt));
        cs.setString(7, nz(g.poBox));
        cs.setString(8, nz(g.box));
        cs.setString(9, nz(g.cmr));
        cs.setString(10, nz(g.apo));
        cs.setString(11, nz(g.colonia));
        cs.setString(12, nz(g.delegacion));
        cs.setString(13, nz(g.ciudad));
        cs.setString(14, nz(g.estado));
        cs.setString(15, nz(g.pais));
        cs.setString(16, nz(g.cp));
        cs.setString(17, nz(g.linea1));
        cs.setString(18, nz(g.linea2));
        cs.setString(19, nz(g.linea3));
        cs.setString(20, nz(g.linea4));
        cs.setString(21, nz(g.linea5));
        cs.setString(22, nz(g.telefonoDefault));
        cs.setString(23, nz(g.ladaCasa1));
        cs.setString(24, nz(g.telefonoCasa1));
        cs.setBoolean(25, g.whatsAppCasa1);
        cs.setString(26, nz(g.ladaCasa2));
        cs.setString(27, nz(g.telefonoCasa2));
        cs.setBoolean(28, g.whatsAppCasa2);
        cs.setString(29, nz(g.ladaOficina1));
        cs.setString(30, nz(g.telefonoOficina1));
        cs.setBoolean(31, g.whatsAppOficina1);
        cs.setString(32, nz(g.ladaOficina2));
        cs.setString(33, nz(g.telefonoOficina2));
        cs.setBoolean(34, g.whatsAppOficina2);
        cs.setString(35, nz(g.ladaCelular1));
        cs.setString(36, nz(g.telefonoCelular1));
        cs.setBoolean(37, g.whatsAppCelular1);
        cs.setString(38, nz(g.ladaCelular2));
        cs.setString(39, nz(g.telefonoCelular2));
        cs.setBoolean(40, g.whatsAppCelular2);
        cs.setString(41, nz(g.ladaCelular3));
        cs.setString(42, nz(g.telefonoCelular3));
        cs.setBoolean(43, g.whatsAppCelular3);
        cs.setString(44, nz(g.ladaMensajes));
        cs.setString(45, nz(g.telefonoMensajes));
        cs.setBoolean(46, g.whatsAppMensajes);
        cs.setString(47, nz(g.nacionalidad));
        cs.setString(48, nz(g.email1));
        cs.setString(49, nz(g.email2));
        cs.setString(50, nz(g.email3));
        cs.setString(51, nz(g.email4));
        cs.setLong(52, idUsuarioAlta);
    }

    private void fillReplaceCallableStatement(CallableStatement cs,
                                              VentasInformacionGeneral g,
                                              long idUsuario) throws SQLException {

        cs.setLong(1, g.idContrato);
        cs.setString(2, nz(g.tipoDir));
        cs.setString(3, nz(g.calle));
        cs.setString(4, nz(g.noExt));
        cs.setString(5, nz(g.noInt));
        cs.setString(6, nz(g.poBox));
        cs.setString(7, nz(g.box));
        cs.setString(8, nz(g.cmr));
        cs.setString(9, nz(g.apo));
        cs.setString(10, nz(g.colonia));
        cs.setString(11, nz(g.delegacion));
        cs.setString(12, nz(g.ciudad));
        cs.setString(13, nz(g.estado));
        cs.setString(14, nz(g.pais));
        cs.setString(15, nz(g.cp));
        cs.setString(16, nz(g.linea1));
        cs.setString(17, nz(g.linea2));
        cs.setString(18, nz(g.linea3));
        cs.setString(19, nz(g.linea4));
        cs.setString(20, nz(g.linea5));
        cs.setString(21, nz(g.telefonoDefault));

        // Casa
        cs.setString(22, nz(g.ladaCasa1));
        cs.setString(23, nz(g.telefonoCasa1));
        cs.setBoolean(24, g.whatsAppCasa1);

        cs.setString(25, nz(g.ladaCasa2));
        cs.setString(26, nz(g.telefonoCasa2));
        cs.setBoolean(27, g.whatsAppCasa2);

        // Celular 1
        cs.setString(28, nz(g.ladaCelular1));
        cs.setString(29, nz(g.telefonoCelular1));
        cs.setBoolean(30, g.whatsAppCelular1);

        // Celular 2
        cs.setString(31, nz(g.ladaCelular2));
        cs.setString(32, nz(g.telefonoCelular2));
        cs.setBoolean(33, g.whatsAppCelular2);

        // Celular 3
        cs.setString(34, nz(g.ladaCelular3));
        cs.setString(35, nz(g.telefonoCelular3));
        cs.setBoolean(36, g.whatsAppCelular3);

        // Mensajes
        cs.setString(37, nz(g.ladaMensajes));
        cs.setString(38, nz(g.telefonoMensajes));
        cs.setBoolean(39, g.whatsAppMensajes);

        // Oficina 1
        cs.setString(40, nz(g.ladaOficina1));
        cs.setString(41, nz(g.telefonoOficina1));
        cs.setBoolean(42, g.whatsAppOficina1);

        // Oficina 2
        cs.setString(43, nz(g.ladaOficina2));
        cs.setString(44, nz(g.telefonoOficina2));
        cs.setBoolean(45, g.whatsAppOficina2);

        // Restantes
        cs.setString(46, nz(g.nacionalidad));
        cs.setString(47, nz(g.email1));
        cs.setString(48, nz(g.email2));
        cs.setString(49, nz(g.email3));
        cs.setString(50, nz(g.email4));
        cs.setLong(51, idUsuario);
    }
}

