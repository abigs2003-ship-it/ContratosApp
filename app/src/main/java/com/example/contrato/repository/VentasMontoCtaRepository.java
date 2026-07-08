package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasMontoCta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasMontoCtaRepository {
    public boolean huboCambios(List<VentasMontoCta> actuales, List<String> nuevos) {
        if (actuales.size() != nuevos.size()) return true;
        List<String> xrefsActuales = new ArrayList<>();
        for (VentasMontoCta m : actuales) xrefsActuales.add(m.xref);
        for (String nuevo : nuevos) {
            if (!xrefsActuales.remove(nuevo)) return true;
        }
        return false;
    }

    // aqui empieza 104

    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_MontoCta_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 8.2
    public void insert(VentasMontoCta m) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_MontoCta_Insert(?,?,?,?)}")) {
            cs.setLong(1,   m.idMontoCta);
            cs.setLong(2,   m.idContrato);
            cs.setString(3, m.xref);
            cs.setLong(4,   m.idUsuarioAlta);
            cs.executeUpdate();
        }
    }

    // 8.3
    public void desactivarPorContrato(long idContrato, long idUsuarioModificacion) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_MontoCta_DesactivarPorContrato(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuarioModificacion);
            cs.executeUpdate();
        }
    }

    // 8.4
    public List<VentasMontoCta> getByContratoId(long idContrato) throws SQLException {
        List<VentasMontoCta> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_MontoCta_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    VentasMontoCta m = new VentasMontoCta();
                    m.idMontoCta    = rs.getLong("IdMontoCta");
                    m.idContrato    = rs.getLong("IdContrato");
                    m.xref          = rs.getString("Xref");
                    m.fechaAlta     = rs.getTimestamp("FechaAlta");
                    m.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    list.add(m);
                }
            }
        }
        return list;
    }

}
