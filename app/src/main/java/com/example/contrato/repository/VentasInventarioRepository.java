package com.example.contrato.repository;

import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentasInventarioRepository {

    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdCondicionesVenta), 0) + 1 AS NextId FROM PMT_App_Ventas_Datos_Inventario";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }


    public List<String> getUnidades() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "{call dbo.sp_App_Unidades}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("Unidad"));
                }
            }
        }
        return lista;
    }
    public String getTipoCambio() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "{call dbo.sp_Sel_App_Tipo_Cambio}";
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("TipoCambio"));
                }
            }
        }
        return lista.get(0);
    }

    //checa si contrato existe y da el estatus del contrato
    public String getEstatusContrato(String xref) throws SQLException {
        String sql = "{call dbo.sp_App_Checa_Contrato(?)}";

        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, xref);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("EstatusContrato");
                }
            }
        }

        return null;
    }


    public void insert(VentasInventario i) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Datos_Inventario (IdCondicionesVenta, IdContrato, Unidad, Temporada, TipoVenta, AñosComprados, PrimerAñoUso, MonedaVenta, TipoCambioVenta, PrecioBruto, MontoCta, NoContratosMontoCta, PrecioNeto, TipoPago, EngancheTotal, EngancheTotalPorcentaje, EnganchePagarSala, EnganchePagarSalaPorcentaje, Descuentos, NoDescuentos, EngancheDiferido, NoPagosEngancheDiferido, SaldoEnganche, MontoFinanciar, CostoContrato, TotalPagoSala, CostoMembresia, ComentariosRegalos, FechaAlta, IdUsuarioAlta, tipoOcupacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, i.idCondicionesVenta);
            ps.setLong(2, i.idContrato);
            ps.setString(3, i.unidad);
            ps.setString(4, i.temporada);
            ps.setString(5, i.tipoVenta);
            ps.setInt(6, i.aniosComprados);
            ps.setLong(7, i.primerAnioUso);
            ps.setString(8, i.monedaVenta);
            ps.setObject(9, i.tipoCambioVenta);
            ps.setObject(10, i.precioBruto);
            ps.setObject(11, i.montoCta);
            ps.setLong(12, i.noContratosMontoCta);
            ps.setObject(13, i.precioNeto);
            ps.setString(14, i.tipoPago);
            ps.setObject(15, i.engancheTotal);
            ps.setObject(16, i.engancheTotalPorcentaje);
            ps.setObject(17, i.enganchePagarSala);
            ps.setObject(18, i.enganchePagarSalaPorcentaje);
            ps.setObject(19, i.descuentos);
            ps.setLong(20, i.noDescuentos);
            ps.setObject(21, i.engancheDiferido);
            ps.setLong(22, i.noPagosEngancheDiferido);
            ps.setObject(23, i.saldoEnganche);
            ps.setObject(24, i.montoFinanciar);
            ps.setObject(25, i.costoContrato);
            ps.setObject(26, i.totalPagoSala);
            ps.setObject(27, i.costoMembresia);
            ps.setString(28, i.comentariosRegalos);
            ps.setTimestamp(29, i.fechaAlta);
            ps.setLong(30, i.idUsuarioAlta);
            ps.setString(31, i.tipoOcupacion);
            ps.executeUpdate();
        }
    }

    public VentasInventario getByContratoId(long idContrato) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Datos_Inventario WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasInventario i = new VentasInventario();
                    i.idCondicionesVenta = rs.getLong("IdCondicionesVenta");
                    i.idContrato = rs.getLong("IdContrato");
                    i.unidad = rs.getString("Unidad");
                    i.temporada = rs.getString("Temporada");
                    i.tipoVenta = rs.getString("TipoVenta");
                    i.aniosComprados = rs.getInt("AñosComprados");
                    i.primerAnioUso = rs.getLong("PrimerAñoUso");
                    i.monedaVenta = rs.getString("MonedaVenta");
                    i.tipoCambioVenta = rs.getDouble("TipoCambioVenta");
                    i.precioBruto = rs.getDouble("PrecioBruto");
                    i.montoCta = rs.getDouble("MontoCta");
                    i.noContratosMontoCta = rs.getLong("NoContratosMontoCta");
                    i.precioNeto = rs.getDouble("PrecioNeto");
                    i.tipoPago = rs.getString("TipoPago");
                    i.engancheTotal = rs.getDouble("EngancheTotal");
                    i.engancheTotalPorcentaje = rs.getDouble("EngancheTotalPorcentaje");
                    i.enganchePagarSala = rs.getDouble("EnganchePagarSala");
                    i.enganchePagarSalaPorcentaje = rs.getDouble("EnganchePagarSalaPorcentaje");
                    i.descuentos = rs.getDouble("Descuentos");
                    i.noDescuentos = rs.getLong("NoDescuentos");
                    i.engancheDiferido = rs.getDouble("EngancheDiferido");
                    i.noPagosEngancheDiferido = rs.getLong("NoPagosEngancheDiferido");
                    i.saldoEnganche = rs.getDouble("SaldoEnganche");
                    i.montoFinanciar = rs.getDouble("MontoFinanciar");
                    i.costoContrato = rs.getDouble("CostoContrato");
                    i.totalPagoSala = rs.getDouble("TotalPagoSala");
                    i.costoMembresia = rs.getDouble("CostoMembresia");
                    i.comentariosRegalos = rs.getString("ComentariosRegalos");
                    i.fechaAlta = rs.getTimestamp("FechaAlta");
                    i.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    i.tipoOcupacion = rs.getString(("TipoOcupacion"));

                    return i;
                }
            }
        }
        return null;
    }

    public void update(VentasInventario i) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Datos_Inventario SET Unidad=?, Temporada=?, TipoVenta=?, AñosComprados=?, PrimerAñoUso=?, MonedaVenta=?, TipoCambioVenta=?, PrecioBruto=?, MontoCta=?, NoContratosMontoCta=?, PrecioNeto=?, TipoPago=?, EngancheTotal=?, EngancheTotalPorcentaje=?, EnganchePagarSala=?, EnganchePagarSalaPorcentaje=?, Descuentos=?, NoDescuentos=?, EngancheDiferido=?, NoPagosEngancheDiferido=?, SaldoEnganche=?, MontoFinanciar=?, CostoContrato=?, TotalPagoSala=?, CostoMembresia=?, ComentariosRegalos=?, TipoOcupacion=? WHERE IdCondicionesVenta=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, i.unidad);
            ps.setString(2, i.temporada);
            ps.setString(3, i.tipoVenta);
            ps.setInt(4, i.aniosComprados);
            ps.setLong(5, i.primerAnioUso);
            ps.setString(6, i.monedaVenta);
            ps.setObject(7, i.tipoCambioVenta);
            ps.setObject(8, i.precioBruto);
            ps.setObject(9, i.montoCta);
            ps.setLong(10, i.noContratosMontoCta);
            ps.setObject(11, i.precioNeto);
            ps.setString(12, i.tipoPago);
            ps.setObject(13, i.engancheTotal);
            ps.setObject(14, i.engancheTotalPorcentaje);
            ps.setObject(15, i.enganchePagarSala);
            ps.setObject(16, i.enganchePagarSalaPorcentaje);
            ps.setObject(17, i.descuentos);
            ps.setLong(18, i.noDescuentos);
            ps.setObject(19, i.engancheDiferido);
            ps.setLong(20, i.noPagosEngancheDiferido);
            ps.setObject(21, i.saldoEnganche);
            ps.setObject(22, i.montoFinanciar);
            ps.setObject(23, i.costoContrato);
            ps.setObject(24, i.totalPagoSala);
            ps.setObject(25, i.costoMembresia);
            ps.setString(26, i.comentariosRegalos);
            ps.setString(27, i.tipoOcupacion);
            ps.setLong(28, i.idCondicionesVenta);
            ps.executeUpdate();
        }
    }

    public void deleteByContratoId(long idContrato) throws SQLException {
        String sql = "DELETE FROM PMT_App_Ventas_Datos_Inventario WHERE IdContrato=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idContrato);
            ps.executeUpdate();
        }
    }
}
