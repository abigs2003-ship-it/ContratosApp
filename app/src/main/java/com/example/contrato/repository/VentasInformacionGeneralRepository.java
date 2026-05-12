package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasInformacionGeneral;
import java.sql.*;

public class VentasInformacionGeneralRepository {

    /**
     * Gets the next incrementing ID for a new general information record.
     */
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdDatosVenta), 0) + 1 AS NextId FROM PMT_App_Ventas_Informacion_General";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasInformacionGeneral g) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Informacion_General (IdDatosVenta, IdContrato, TipoDir, Calle, NoExt, NoInt, POBox, BOX, CMR, APO, Colonia, Delegacion, Ciudad, Estado, Pais, CP, Linea1, Linea2, Linea3, Linea4, Linea5, TelefonoDefault, LadaCasa1, TelefonoCasa1, WhatsAppCasa1, LadaCasa2, TelefonoCasa2, WhatsAppCasa2, LadaCelular1, TelefonoCelular1, WhatsAppCelular1, LadaCelular2, TelefonoCelular2, WhatsAppCelular2, LadaMensajes, TelefonoMensajes, WhatsAppMensajes, LadaOficina1, TelefonoOficina1, WhatsAppOficina1, LadaOficina2, TelefonoOficina2, WhatsAppOficina2, Nacionalidad, Email1, Email2, Email3, Email4, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillPreparedStatement(ps, g);
            ps.setTimestamp(49, g.fechaAlta);
            ps.setLong(50, g.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public void update(VentasInformacionGeneral g) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Informacion_General SET TipoDir=?, Calle=?, NoExt=?, NoInt=?, POBox=?, BOX=?, CMR=?, APO=?, Colonia=?, Delegacion=?, Ciudad=?, Estado=?, Pais=?, CP=?, Linea1=?, Linea2=?, Linea3=?, Linea4=?, Linea5=?, TelefonoDefault=?, LadaCasa1=?, TelefonoCasa1=?, WhatsAppCasa1=?, LadaCasa2=?, TelefonoCasa2=?, WhatsAppCasa2=?, LadaCelular1=?, TelefonoCelular1=?, WhatsAppCelular1=?, LadaCelular2=?, TelefonoCelular2=?, WhatsAppCelular2=?, LadaMensajes=?, TelefonoMensajes=?, WhatsAppMensajes=?, LadaOficina1=?, TelefonoOficina1=?, WhatsAppOficina1=?, LadaOficina2=?, TelefonoOficina2=?, WhatsAppOficina2=?, Nacionalidad=?, Email1=?, Email2=?, Email3=?, Email4=? WHERE IdContrato=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.tipoDir);
            ps.setString(2, g.calle);
            ps.setString(3, g.noExt);
            ps.setString(4, g.noInt);
            ps.setString(5, g.poBox);
            ps.setString(6, g.box);
            ps.setString(7, g.cmr);
            ps.setString(8, g.apo);
            ps.setString(9, g.colonia);
            ps.setString(10, g.delegacion);
            ps.setString(11, g.ciudad);
            ps.setString(12, g.estado);
            ps.setString(13, g.pais);
            ps.setString(14, g.cp);
            ps.setString(15, g.linea1);
            ps.setString(16, g.linea2);
            ps.setString(17, g.linea3);
            ps.setString(18, g.linea4);
            ps.setString(19, g.linea5);
            ps.setString(20, g.telefonoDefault);
            ps.setString(21, g.ladaCasa1);
            ps.setString(22, g.telefonoCasa1);
            ps.setBoolean(23, g.whatsAppCasa1);
            ps.setString(24, g.ladaCasa2);
            ps.setString(25, g.telefonoCasa2);
            ps.setBoolean(26, g.whatsAppCasa2);
            ps.setString(27, g.ladaCelular1);
            ps.setString(28, g.telefonoCelular1);
            ps.setBoolean(29, g.whatsAppCelular1);
            ps.setString(30, g.ladaCelular2);
            ps.setString(31, g.telefonoCelular2);
            ps.setBoolean(32, g.whatsAppCelular2);
            ps.setString(33, g.ladaMensajes);
            ps.setString(34, g.telefonoMensajes);
            ps.setBoolean(35, g.whatsAppMensajes);
            ps.setString(36, g.ladaOficina1);
            ps.setString(37, g.telefonoOficina1);
            ps.setBoolean(38, g.whatsAppOficina1);
            ps.setString(39, g.ladaOficina2);
            ps.setString(40, g.telefonoOficina2);
            ps.setBoolean(41, g.whatsAppOficina2);
            ps.setString(42, g.nacionalidad);
            ps.setString(43, g.email1);
            ps.setString(44, g.email2);
            ps.setString(45, g.email3);
            ps.setString(46, g.email4);
            ps.setLong(47, g.idContrato);
            ps.executeUpdate();
        }
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
    }

    public VentasInformacionGeneral getByContratoId(long idContrato) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Informacion_General WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasInformacionGeneral g = new VentasInformacionGeneral();
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
                    g.nacionalidad = rs.getString("Nacionalidad");
                    g.email1 = rs.getString("Email1");
                    g.email2 = rs.getString("Email2");
                    g.email3 = rs.getString("Email3");
                    g.email4 = rs.getString("Email4");
                    g.fechaAlta = rs.getTimestamp("FechaAlta");
                    g.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    return g;
                }
            }
        }
        return null;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Informacion_General WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
