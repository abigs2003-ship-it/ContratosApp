package com.example.contrato.repository;

import com.example.contrato.ContratoModelo;
import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasDetalleFinanciamiento;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VentasDetalleFinanciamientoRepository {

    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_DetalleFinanciamiento_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    public void insert(VentasDetalleFinanciamiento d) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call sp_App_DetalleFinanciamiento_Insert(?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setLong  (1,  d.idDetalleFinanciamiento);
            cs.setLong  (2,  d.idContrato);
            cs.setLong  (3,  d.no);
            cs.setDate  (4,  d.fechaPago);
            cs.setDouble(5,  d.monto);
            cs.setDouble(6,  d.capital);
            cs.setDouble(7,  d.interes);
            cs.setDouble(8,  d.capAcumulado);
            cs.setDouble(9,  d.saldo);
            cs.setLong  (10, d.idUsuarioAlta);
            cs.setString(11, "A");          // estatus siempre "A" al insertar
            cs.executeUpdate();
        }
    }

    /** Cancela lógicamente todos los registros activos del contrato. */
    public void cancelarByContratoId(long idContrato, long idUsuario) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call sp_App_DetalleFinanciamiento_CancelarByContratoId(?,?)}")) {
            cs.setLong(1, idContrato);
            cs.setLong(2, idUsuario);
            cs.executeUpdate();
        }
    }

    public List<VentasDetalleFinanciamiento> getByContratoId(long idContrato) throws SQLException {
        List<VentasDetalleFinanciamiento> lista = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{call sp_App_DetalleFinanciamiento_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Cancela activos e inserta nueva lista. */
    public void replaceByContrato(long idContrato,
                                  List<VentasDetalleFinanciamiento> filas,
                                  long idUsuario) throws SQLException {
        cancelarByContratoId(idContrato, idUsuario);
        for (VentasDetalleFinanciamiento d : filas) {
            d.idDetalleFinanciamiento = getNextId();
            d.idUsuarioAlta           = idUsuario;
            insert(d);
        }
    }

    /** Devuelve true si la cantidad de filas activas difiere o algún campo clave cambió. */
    public boolean huboCambios(List<VentasDetalleFinanciamiento> actuales,
                               List<ContratoModelo.FilaAmortizacion> nuevas) {
        // Solo filas activas
        List<VentasDetalleFinanciamiento> activas = new ArrayList<>();
        for (VentasDetalleFinanciamiento a : actuales) {
            if ("A".equals(a.estatus)) activas.add(a);
        }
        if (activas.size() != nuevas.size()) return true;
        for (int i = 0; i < activas.size(); i++) {
            VentasDetalleFinanciamiento a = activas.get(i);
            ContratoModelo.FilaAmortizacion n = nuevas.get(i);
            if (a.no != n.no
                    || Math.abs(a.monto        - n.monto)        > 0.001
                    || Math.abs(a.capital      - n.capital)      > 0.001
                    || Math.abs(a.interes      - n.interes)      > 0.001
                    || Math.abs(a.capAcumulado - n.capAcumulado) > 0.001
                    || Math.abs(a.saldo        - n.saldo)        > 0.001) {
                return true;
            }
        }
        return false;
    }


    private VentasDetalleFinanciamiento mapear(ResultSet rs) throws SQLException {
        VentasDetalleFinanciamiento d = new VentasDetalleFinanciamiento();
        d.idDetalleFinanciamiento = rs.getLong     ("IdDetalleFinanciamiento");
        d.idContrato              = rs.getLong     ("IdContrato");
        d.no                      = rs.getLong     ("No");
        d.fechaPago               = rs.getDate     ("FechaPago");
        d.monto                   = rs.getDouble   ("Monto");
        d.capital                 = rs.getDouble   ("Capital");
        d.interes                 = rs.getDouble   ("Interes");
        d.capAcumulado            = rs.getDouble   ("CapAcumulado");
        d.saldo                   = rs.getDouble   ("Saldo");
        d.idUsuarioAlta           = rs.getLong     ("IdUsuarioAlta");
        d.fechaAlta               = rs.getTimestamp("FechaAlta");
        d.idUsuarioModificacion   = rs.getLong     ("IdUsuarioModificacion");
        d.fechaModificacion       = rs.getTimestamp("FechaModificacion");
        d.estatus                 = rs.getString   ("Estatus");
        return d;
    }
}