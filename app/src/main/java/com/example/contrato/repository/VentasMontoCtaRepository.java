package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasMontoCta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasMontoCtaRepository {

    /**
     * Gets the next incrementing ID for a new monto cta record.
     */
    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdMontoCta), 0) + 1 AS NextId FROM PMT_App_Ventas_Monto_Cta";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasMontoCta m) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Monto_Cta (IdMontoCta, IdContrato, Xref, FechaAlta, IdUsuarioAlta) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, m.idMontoCta);
            ps.setLong(2, m.idContrato);
            ps.setString(3, m.xref);
            ps.setTimestamp(4, m.fechaAlta);
            ps.setLong(5, m.idUsuarioAlta);
            ps.executeUpdate();
        }
    }

    public List<VentasMontoCta> getByContratoId(long idContrato) throws SQLException {
        List<VentasMontoCta> list = new ArrayList<>();
        String sql = "SELECT * FROM PMT_App_Ventas_Monto_Cta WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentasMontoCta m = new VentasMontoCta();
                    m.idMontoCta = rs.getLong("IdMontoCta");
                    m.idContrato = rs.getLong("IdContrato");
                    m.xref = rs.getString("Xref");
                    m.fechaAlta = rs.getTimestamp("FechaAlta");
                    m.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Monto_Cta WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
