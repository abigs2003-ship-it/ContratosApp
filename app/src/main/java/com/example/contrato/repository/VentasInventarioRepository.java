package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VentasInventarioRepository {
    public boolean huboCambios(VentasInventario a, VentasInventario n) {
        return !Objects.equals(a.unidad,                   n.unidad)
                || !Objects.equals(a.temporada,                n.temporada)
                || !Objects.equals(a.tipoVenta,                n.tipoVenta)
                || !Objects.equals(a.tipoOcupacion,            n.tipoOcupacion)
                ||  a.aniosComprados        !=                 n.aniosComprados
                ||  a.primerAnioUso         !=                 n.primerAnioUso
                || !Objects.equals(a.monedaVenta,              n.monedaVenta)
                || !Objects.equals(a.tipoCambioVenta,          n.tipoCambioVenta)
                || !Objects.equals(a.precioBruto,              n.precioBruto)
                || !Objects.equals(a.montoCta,                 n.montoCta)
                ||  a.noContratosMontoCta   !=                 n.noContratosMontoCta
                || !Objects.equals(a.precioNeto,               n.precioNeto)
                || !Objects.equals(a.tipoPago,                 n.tipoPago)
                || !Objects.equals(a.engancheTotal,            n.engancheTotal)
                || !Objects.equals(a.engancheTotalPorcentaje,  n.engancheTotalPorcentaje)
                || !Objects.equals(a.enganchePagarSala,        n.enganchePagarSala)
                || !Objects.equals(a.enganchePagarSalaPorcentaje, n.enganchePagarSalaPorcentaje)
                || !Objects.equals(a.descuentos,               n.descuentos)
                ||  a.noDescuentos          !=                 n.noDescuentos
                || !Objects.equals(a.engancheDiferido,         n.engancheDiferido)
                ||  a.noPagosEngancheDiferido !=               n.noPagosEngancheDiferido
                || !Objects.equals(a.saldoEnganche,            n.saldoEnganche)
                || !Objects.equals(a.montoFinanciar,           n.montoFinanciar)
                || !Objects.equals(a.costoContrato,            n.costoContrato)
                || !Objects.equals(a.totalPagoSala,            n.totalPagoSala)
                || !Objects.equals(a.costoMembresia,           n.costoMembresia)
                || !Objects.equals(a.tipoPagoDiferido,           n.tipoPagoDiferido)
                || !Objects.equals(a.primerPagoDiferido,        n.primerPagoDiferido)
                || !Objects.equals(a.comentariosRegalos,       n.comentariosRegalos);
    }
    private VentasInventario mapResultSet(ResultSet rs) throws SQLException {
        VentasInventario i = new VentasInventario();
        i.idCondicionesVenta       = rs.getLong("IdCondicionesVenta");
        i.idContrato               = rs.getLong("IdContrato");
        i.unidad                   = rs.getString("Unidad");
        i.temporada                = rs.getString("Temporada");
        i.tipoVenta                = rs.getString("TipoVenta");
        i.tipoOcupacion            = rs.getString("TipoOcupacion");
        i.aniosComprados           = rs.getInt("AñosComprados");
        i.primerAnioUso            = rs.getLong("PrimerAñoUso");
        i.monedaVenta              = rs.getString("MonedaVenta");
        i.tipoCambioVenta          = rs.getDouble("TipoCambioVenta");
        i.precioBruto              = rs.getDouble("PrecioBruto");
        i.montoCta                 = rs.getDouble("MontoCta");
        i.noContratosMontoCta      = rs.getLong("NoContratosMontoCta");
        i.precioNeto               = rs.getDouble("PrecioNeto");
        i.tipoPago                 = rs.getString("TipoPago");
        i.engancheTotal            = rs.getDouble("EngancheTotal");
        i.engancheTotalPorcentaje  = rs.getDouble("EngancheTotalPorcentaje");
        i.enganchePagarSala        = rs.getDouble("EnganchePagarSala");
        i.enganchePagarSalaPorcentaje = rs.getDouble("EnganchePagarSalaPorcentaje");
        i.descuentos               = rs.getDouble("Descuentos");
        i.noDescuentos             = rs.getLong("NoDescuentos");
        i.engancheDiferido         = rs.getDouble("EngancheDiferido");
        i.noPagosEngancheDiferido  = rs.getLong("NoPagosEngancheDiferido");
        i.saldoEnganche            = rs.getDouble("SaldoEnganche");
        i.montoFinanciar           = rs.getDouble("MontoFinanciar");
        i.costoContrato            = rs.getDouble("CostoContrato");
        i.totalPagoSala            = rs.getDouble("TotalPagoSala");
        i.costoMembresia           = rs.getDouble("CostoMembresia");
        i.comentariosRegalos       = rs.getString("ComentariosRegalos");
        i.fechaAlta                = rs.getTimestamp("FechaAlta");
        i.idUsuarioAlta            = rs.getLong("IdUsuarioAlta");
        i.estatus                  = rs.getString("Estatus");
        i.tipoPagoDiferido         =rs.getString("TipoPagoDiferido");
        i.primerPagoDiferido       = rs.getDate("PrimerPagoDiferido");
        return i;
    }


    private void cargarIdsCatalogos(Connection conn, VentasInventario i) throws SQLException {

        String sql = "{call sp_App_ObtieneIdsCatalogos(?,?,?)}";

        try (CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, i.unidad);
            cs.setString(2, i.temporada);
            cs.setString(3, i.tipoOcupacion);

            try (ResultSet rs = cs.executeQuery()) {

                if (rs.next()) {

                    i.idUnidad = rs.getString("IdUnidad");

                    long temporada = rs.getLong("IdTemporada");
                    i.idTemporada = rs.wasNull() ? null : temporada;

                    i.idTipoOcupacion = rs.getString("IdTipoOcupacion");
                }
            }
        }
    }
    public List<String> getUnidades() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "{call dbo.sp_App_Unidades}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(rs.getString("Unidad"));
        }
        return lista;
    }


    public String getTipoCambio() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "{call dbo.sp_Sel_App_Tipo_Cambio}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) lista.add(rs.getString("TipoCambio"));
        }
        return lista.get(0);
    }

    public String getEstatusContrato(String xref) throws SQLException {
        String sql = "{call dbo.sp_App_Checa_Contrato(?)}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, xref);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getString("EstatusContrato");
            }
        }
        return null;
    }
//Aqui empieza 104

public long getNextId() throws SQLException {
    try (Connection conn = DbConnection.getConnection();
         CallableStatement cs = conn.prepareCall("{call sp_App_Inventario_GetNextId}");
         ResultSet rs = cs.executeQuery()) {
        if (rs.next()) return rs.getLong("NextId");
    }
    return 1;
}
public void insert(VentasInventario i) throws SQLException {
    try (Connection conn = DbConnection.getConnection();
         CallableStatement cs = conn.prepareCall(buildInsertCall())) {
        fillCallableStatement(cs, i);
        cs.executeUpdate();
    }
}

    // 6.3 — SP handles deactivate + insert in one transaction
    public void replaceByContrato(VentasInventario i, long idUsuario) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(buildReplaceCall())) {
            fillReplaceCallableStatement(cs, i, idUsuario);
            cs.executeUpdate();
        }
    }


    public VentasInventario getByContratoId(long idContrato) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Inventario_GetByContratoId(?)}")) {
            cs.setLong(1, idContrato);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        }
        return null;
    }



    // ── Helpers ────────────────────────────────────────────────────────────────

    private String buildInsertCall() {
        // sp_App_Inventario_Insert has 31 params
        return "{call sp_App_Inventario_Insert(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
    }

    private String buildReplaceCall() {
        // sp_App_Inventario_ReplaceByContrato has 30 params (no IdCondicionesVenta)
        return "{call sp_App_Inventario_ReplaceByContrato(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
    }


    private void fillCallableStatement(CallableStatement cs, VentasInventario i) throws SQLException {
        cs.setLong(1,    i.idCondicionesVenta);
        cs.setLong(2,    i.idContrato);
        cs.setString(3,  i.unidad);
        cs.setString(4,  i.temporada);
        cs.setString(5,  i.tipoVenta);
        cs.setString(6,  i.tipoOcupacion);
        cs.setInt(7,     i.aniosComprados);
        cs.setLong(8,    i.primerAnioUso);
        cs.setString(9,  i.monedaVenta);
        cs.setObject(10, i.tipoCambioVenta);
        cs.setObject(11, i.precioBruto);
        cs.setObject(12, i.montoCta);
        cs.setLong(13,   i.noContratosMontoCta);
        cs.setObject(14, i.precioNeto);
        cs.setString(15, i.tipoPago);
        cs.setObject(16, i.engancheTotal);
        cs.setObject(17, i.engancheTotalPorcentaje);
        cs.setObject(18, i.enganchePagarSala);
        cs.setObject(19, i.enganchePagarSalaPorcentaje);
        cs.setObject(20, i.descuentos);
        cs.setLong(21,   i.noDescuentos);
        cs.setObject(22, i.engancheDiferido);
        cs.setLong(23,   i.noPagosEngancheDiferido);
        cs.setObject(24, i.saldoEnganche);
        cs.setObject(25, i.montoFinanciar);
        cs.setObject(26, i.costoContrato);
        cs.setObject(27, i.totalPagoSala);
        cs.setObject(28, i.costoMembresia);
        cs.setString(29, i.comentariosRegalos);
        cs.setLong(30,   i.idUsuarioAlta);
        cs.setString(31, i.tipoPagoDiferido);
        cs.setDate(32,   i.primerPagoDiferido);
    }


    private void fillReplaceCallableStatement(CallableStatement cs,
                                              VentasInventario i,
                                              long idUsuario) throws SQLException {
        cs.setLong(1,    i.idContrato);
        cs.setString(2,  i.unidad);
        cs.setString(3,  i.temporada);
        cs.setString(4,  i.tipoVenta);
        cs.setString(5,  i.tipoOcupacion);
        cs.setInt(6,     i.aniosComprados);
        cs.setLong(7,    i.primerAnioUso);
        cs.setString(8,  i.monedaVenta);
        cs.setObject(9,  i.tipoCambioVenta);
        cs.setObject(10, i.precioBruto);
        cs.setObject(11, i.montoCta);
        cs.setLong(12,   i.noContratosMontoCta);
        cs.setObject(13, i.precioNeto);
        cs.setString(14, i.tipoPago);
        cs.setObject(15, i.engancheTotal);
        cs.setObject(16, i.engancheTotalPorcentaje);
        cs.setObject(17, i.enganchePagarSala);
        cs.setObject(18, i.enganchePagarSalaPorcentaje);
        cs.setObject(19, i.descuentos);
        cs.setLong(20,   i.noDescuentos);
        cs.setObject(21, i.engancheDiferido);
        cs.setLong(22,   i.noPagosEngancheDiferido);
        cs.setObject(23, i.saldoEnganche);
        cs.setObject(24, i.montoFinanciar);
        cs.setObject(25, i.costoContrato);
        cs.setObject(26, i.totalPagoSala);
        cs.setObject(27, i.costoMembresia);
        cs.setString(28, i.comentariosRegalos);
        cs.setLong(29,   idUsuario);
        cs.setString(30, i.tipoPagoDiferido);
        cs.setDate(31,   i.primerPagoDiferido);
    }


}