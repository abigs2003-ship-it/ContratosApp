package com.example.contrato.repository;

import static java.lang.Long.parseLong;

import com.example.contrato.ContratoModelo;
import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasTitulares;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

            // Si hay una nueva firma en memoria, consideramos que hubo cambios
            if (n.imagenFirmaBase64 != null && !n.imagenFirmaBase64.isEmpty()) return true;
            if (n.imagenINEFrente != null && !n.imagenINEFrente.isEmpty() || n.imagenPasaporte != null && !n.imagenPasaporte.isEmpty()) return true;


            if (!Objects.equals(a.nombre,    n.nombre)
                    || !Objects.equals(a.paterno,   n.paterno)
                    || !Objects.equals(a.materno,   n.materno)
                    || !Objects.equals(a.ocupacion, n.ocupacion)
                    ||  a.parentesco != parseLong(n.parentesco)
                    ||  a.fechaCumpleaños != parseSqlDate(n.cumple)
            )
                return true;
        }
        return false;
    }
    private Date parseSqlDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            java.util.Date utilDate = sdf.parse(dateStr);
            if (utilDate != null) return new Date(utilDate.getTime());
        } catch (Exception e) {
            try {
                SimpleDateFormat sdfUS = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                java.util.Date utilDate = sdfUS.parse(dateStr);
                if (utilDate != null) return new Date(utilDate.getTime());
            } catch (Exception e2) { e2.printStackTrace(); }
        }
        return null;
    }
    // 104

    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 7.2 — ahora con ArchivoINEFrente, ArchivoINEReverso, ArchivoPasaporte
    public void insert(VentasTitulares t) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_Insert(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
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
            cs.setString(13, t.archivoFirma);
            cs.setString(14, t.archivoINEFrente);
            cs.setString(15, t.archivoINEReverso);
            cs.setString(16, t.archivoPasaporte);
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

    // 7.5 — ahora también lee ArchivoINEFrente, ArchivoINEReverso, ArchivoPasaporte
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
                    t.archivoFirma   = rs.getString("ArchivoFirma");
                    t.archivoINEFrente  = rs.getString("ArchivoINEFrente");
                    t.archivoINEReverso = rs.getString("ArchivoINEReverso");
                    t.archivoPasaporte  = rs.getString("ArchivoPasaporte");

                    list.add(t);
                }
            }
        }
        return list;
    }

    // Persiste la ruta/URL definitiva que regresa el backend tras la primera subida
    public void actualizaArchivo(long idTitular, String tipoArchivo, String ruta) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Titulares_ActualizaArchivo(?,?,?)}")) {
            cs.setLong(1, idTitular);
            cs.setString(2, tipoArchivo); // 'FIRMA' | 'INE_FRENTE' | 'INE_REVERSO' | 'PASAPORTE'
            cs.setString(3, ruta);
            cs.executeUpdate();
        }
    }
}