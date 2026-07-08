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
                    || !Objects.equals(a.montoDescuento, parseMontoSeguro(n.monto)))
                return true;
        }
        return false;
    }

    private Double parseMontoSeguro(String monto) {
        if (monto == null || monto.isEmpty()) return 0.0;
        try {
            String limpio = monto.replaceAll("[^\\d.]", "");
            return limpio.isEmpty() ? 0.0 : Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0.0;
         }
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



