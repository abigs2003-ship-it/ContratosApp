package com.example.contrato.repository;

import static java.lang.Double.parseDouble;

import com.example.contrato.ContratoModelo;
import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasEngancheDiferido;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class VentasEngancheDiferidoRepository {

    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};
    public boolean huboCambios(List<VentasEngancheDiferido> actuales, List<ContratoModelo.PagoDiferido> nuevos) {
        if (actuales.size() != nuevos.size()) return true;
        for (int i = 0; i < actuales.size(); i++) {
            VentasEngancheDiferido a = actuales.get(i);
            ContratoModelo.PagoDiferido n = nuevos.get(i);
            if (!Objects.equals(a.cantidadPago, parseMonto(n.monto))
                    || !Objects.equals(String.valueOf(a.fechaPago), convertirMesANumero(n.fecha)))
                return true;
        }
        return false;
    }
    private double parseMonto(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            String clean = value.replaceAll("[^\\d.]", "");
            return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    private String convertirMesANumero(String s) {
        if (s == null || s.length() != 11) return "";

        try {
            if (esIngles()) {
                String mesPalabra = s.substring(0, 3);

                for (int i = 0; i < MESES_EN.length; i++) {
                    if (mesPalabra.equalsIgnoreCase(MESES_EN[i])) {
                        String mesNumero = String.format(Locale.US, "%02d", i + 1);

                        // jan/15/2025 → 01/15/2025
                        return mesNumero + s.substring(3);
                    }
                }

            } else {
                String mesPalabra = s.substring(3, 6);

                for (int i = 0; i < MESES_ES.length; i++) {
                    if (mesPalabra.equalsIgnoreCase(MESES_ES[i])) {
                        String mesNumero = String.format(Locale.US, "%02d", i + 1);

                        // 15/may/2025 → 15/05/2025
                        return s.substring(0, 3) + mesNumero + s.substring(6);
                    }
                }
            }
        } catch (Exception ignored) {}

        return "";
    }
    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }
    // Solo devuelve registros activos (Estatus = 'A') para mostrar en pantalla

    // aqui empieza 104
    /*
    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_EngancheDiferido_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 3.2
    public void insert(VentasEngancheDiferido p) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_EngancheDiferido_Insert(?,?,?,?,?)}")) {
            cs.setLong(1, p.idPago);
            cs.setLong(2, p.idContrato);
            cs.setObject(3, p.cantidadPago);
            cs.setDate(4, p.fechaPago);
            cs.setLong(5, p.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 3.3
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_EngancheDiferido_DesactivarPorContrato(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 3.4
    public List<VentasEngancheDiferido> getByContratoId(long idContrato) throws SQLException {
        List<VentasEngancheDiferido> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_EngancheDiferido_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    VentasEngancheDiferido p = new VentasEngancheDiferido();
                    p.idPago        = rs.getLong("IdPago");
                    p.idContrato    = rs.getLong("IdContrato");
                    p.cantidadPago  = rs.getDouble("CantidadPago");
                    p.fechaPago     = rs.getDate("FechaPago");
                    p.fechaAlta     = rs.getTimestamp("FechaAlta");
                    p.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(p);
                }
            }
        }
        return list;
    }
*/
    //aqui empieza citas

    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdPago), 0) + 1 AS NextId FROM PMT_App_Ventas_EngancheDiferido";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public List<VentasEngancheDiferido> getByContratoId(long idContrato) throws SQLException {
        List<VentasEngancheDiferido> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_EngancheDiferido WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasEngancheDiferido p = new VentasEngancheDiferido();
                    p.idPago       = rs.getLong("IdPago");
                    p.idContrato   = rs.getLong("IdContrato");
                    p.cantidadPago = rs.getDouble("CantidadPago");
                    p.fechaPago    = rs.getDate("FechaPago");
                    p.fechaAlta    = rs.getTimestamp("FechaAlta");
                    p.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(p);
                }
            }
        }
        return list;
    }

    public void insert(VentasEngancheDiferido p) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_EngancheDiferido (IdPago, IdContrato, CantidadPago, FechaPago, FechaAlta, IdUsuarioAlta, Estatus, IdUsuarioModificacion, FechaModificacion) VALUES (?, ?, ?, ?, GETDATE(), ?, 'A', NULL, NULL)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, p.idPago);
            ps.setLong(2, p.idContrato);
            ps.setObject(3, p.cantidadPago);
            ps.setDate(4, p.fechaPago);
            ps.setLong(5, p.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    // Desactiva todos los registros activos de un contrato (usar antes del loop de insert)
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_EngancheDiferido SET Estatus = 'C', IdUsuarioModificacion = ?, FechaModificacion = GETDATE() WHERE IdContrato = ? AND Estatus = 'A'";
        try (Connection conexion = DbConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setLong(1, idUsuarioModificacion);
            ps.setLong(2, idContrato);
            ps.executeUpdate();
        }
    }

// */
}
