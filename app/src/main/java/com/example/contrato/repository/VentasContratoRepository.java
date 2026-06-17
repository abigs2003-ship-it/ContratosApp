package com.example.contrato.repository;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.example.contrato.ContratoModelo;
import com.example.contrato.data.DbConnection;
import com.example.contrato.model.VentasContrato;
import com.example.contrato.model.VentasTitulares;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VentasContratoRepository {

    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};

    private String convertirMesANombreString(String s) {

        String resultado = "";
        if (s.length() == 10) {
            try {

                if (esIngles()) {
                    String mesStr = s.substring(0, 2);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_EN[mes - 1];
                        String fechaFinal = mesPalabra + s.substring(2); // "Mar/15/2025"
                        resultado = fechaFinal;
                    }
                } else {
                    String mesStr = s.substring(3, 5);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_ES[mes - 1];
                        String fechaFinal = s.substring(0, 3) + mesPalabra + s.substring(5);
                        resultado = fechaFinal; // "15/mar/2025"
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        return resultado;
    }
    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }

    private String formateaMontos(String texto) {

        String resultado = "";
        texto.replace("$", "")
                .replace(",", "")
                .trim();

        boolean terminaConPunto = texto.endsWith(".");

        if (!texto.isEmpty()) {
            double numero = Double.parseDouble(texto);

            DecimalFormat formato =
                    (DecimalFormat) NumberFormat.getInstance(Locale.US);

            if (texto.contains(".")) {
                formato.applyPattern("$#,##0.##");
            } else {
                formato.applyPattern("$#,##0");
            }

            String formateado = formato.format(numero);

            if (terminaConPunto) {
                formateado += ".";
            }
            resultado = formateado;

        }
        return resultado;}
    private String mapIdiomaFromDb(String dbIdioma) {
        if (dbIdioma == null) return "Español";
        if (dbIdioma.equalsIgnoreCase("ING")) return "English";
        return "Español";
    }
    /*
    // aqui empieza sp
    public long getNextId() throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_GetNextId}");
             ResultSet rs = cs.executeQuery()) {
            if (rs.next()) return rs.getLong("NextId");
        }
        return 1;
    }

    // 1.2
    public void insert(VentasContrato c) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_Insert(?,?,?,?,?,?)}")) {
            cs.setLong(1, c.idContrato);
            cs.setTimestamp(2, c.fechaAlta);
            cs.setLong(3, c.idUsuarioAlta);
            cs.setTimestamp(4, c.fechaModificacion);
            cs.setString(5, c.estatus);
            cs.setString(6, c.idioma);
            cs.executeUpdate();
        }
    }

    // 1.3
    public void update(VentasContrato c) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_Update(?,?,?,?)}")) {
            cs.setLong(1, c.idContrato);
            cs.setTimestamp(2, c.fechaModificacion);
            cs.setString(3, c.estatus);
            cs.setString(4, c.idioma);
            cs.executeUpdate();
        }
    }

    // 1.4
    public VentasContrato getById(long id) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_GetById(?)}")) {
            cs.setLong(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    VentasContrato c = new VentasContrato();
                    c.idContrato        = rs.getLong("IdContrato");
                    c.fechaAlta         = rs.getTimestamp("FechaAlta");
                    c.idUsuarioAlta     = rs.getLong("IdUsuarioAlta");
                    c.fechaModificacion = rs.getTimestamp("FechaModificacion");
                    c.estatus           = rs.getString("Estatus");
                    c.idioma            = rs.getString("Idioma");
                    return c;
                }
            }
        }
        return null;
    }

    // 1.5
    public List<VentasContrato> getByUserId(long usuarioId) throws SQLException {
        List<VentasContrato> lista = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_GetByUserId(?)}")) {
            cs.setLong(1, usuarioId);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    VentasContrato c = new VentasContrato();
                    c.idContrato        = rs.getLong("IdContrato");
                    c.fechaAlta         = rs.getTimestamp("FechaAlta");
                    c.idUsuarioAlta     = rs.getLong("IdUsuarioAlta");
                    c.fechaModificacion = rs.getTimestamp("FechaModificacion");
                    c.estatus           = rs.getString("Estatus");
                    c.idioma            = rs.getString("Idioma");
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    // 1.6
    public List<VentasContrato> getAll() throws SQLException {
        List<VentasContrato> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_GetAll}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                VentasContrato c = new VentasContrato();
                c.idContrato        = rs.getLong("IdContrato");
                c.fechaAlta         = rs.getTimestamp("FechaAlta");
                c.idUsuarioAlta     = rs.getLong("IdUsuarioAlta");
                c.fechaModificacion = rs.getTimestamp("FechaModificacion");
                c.estatus           = rs.getString("Estatus");
                c.idioma            = rs.getString("Idioma");
                list.add(c);
            }
        }
        return list;
    }

    // 1.7
    public void delete(long id) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_Delete(?)}")) {
            cs.setLong(1, id);
            cs.executeUpdate();
        }
    }

    // 1.8
    public List<ContratoModelo> getResumenByUserId(long usuarioId) throws SQLException {
        List<ContratoModelo> models = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_GetResumenByUserId(?)}")) {
            cs.setLong(1, usuarioId);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    ContratoModelo m = new ContratoModelo();
                    m.setId(String.valueOf(rs.getLong("IdContrato")));
                    m.setEstatus(rs.getString("Estatus"));
                    m.setIdioma(mapIdiomaFromDb(rs.getString("Idioma")));

                    Timestamp fechaAlta = rs.getTimestamp("FechaAlta");
                    Timestamp fechaMod  = rs.getTimestamp("FechaModificacion");
                    if (fechaAlta != null) m.setFechaCreacion(sdf.format(fechaAlta));
                    if (fechaMod  != null) m.setFechaModificacion(sdf.format(fechaMod));

                    String nombre  = rs.getString("Nombre");
                    String paterno = rs.getString("Paterno");
                    if (nombre != null) {
                        m.setClientName((nombre + " " + (paterno != null ? paterno : "")).trim());
                    } else {
                        m.setClientName("Contrato #" + rs.getLong("IdContrato"));
                    }
                    models.add(m);
                }
            }
        }
        return models;
    }

    // 1.9 —
    public ContratoModelo getContratoCompleto(long idContrato) throws SQLException {
        Log.d("CONTRATO_DB", "getContratoCompleto id=" + idContrato);
        SimpleDateFormat sdf         = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy",       Locale.getDefault());

        ContratoModelo m = new ContratoModelo();
        m.setId(String.valueOf(idContrato));

        try (Connection conn = DbConnection.getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_App_Contrato_GetCompleto(?)}")) {
            cs.setLong(1, idContrato);
            boolean hasResults = cs.execute();

            // ── Result set 1: contrato header ──────────────────────────────
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null && rs.next()) {
                        m.setModoEdicion(true);
                        m.setEstatus(rs.getString("Estatus"));
                        m.setIdioma(mapIdiomaFromDb(rs.getString("Idioma")));
                        Timestamp fechaAlta = rs.getTimestamp("FechaAlta");
                        Timestamp fechaMod  = rs.getTimestamp("FechaModificacion");
                        if (fechaAlta != null) m.setFechaCreacion(sdf.format(fechaAlta));
                        if (fechaMod  != null) m.setFechaModificacion(sdf.format(fechaMod));
                    } else {
                        m.setModoEdicion(false);
                    }
                }
            }

            // ── Result set 2: titulares + joined sections ──────────────────
            hasResults = cs.getMoreResults();
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null) {
                        VentasTitulares mainTitular = null;
                        boolean infoMapped = false;
                        java.util.Set<String> titularesYaAgregados = new java.util.HashSet<>();

                        while (rs.next()) {
                            String tipoTitular = rs.getString("TipoTitular");
                            String nombre      = rs.getString("Nombre");
                            String paterno     = rs.getString("Paterno");
                            String materno     = rs.getString("Materno");
                            String ocupacion   = rs.getString("Ocupacion");
                            long   parentesco  = rs.getLong("Parentesco");
                            java.sql.Date fechaCumple = rs.getDate("FechaCumpleaños");

                            String key = tipoTitular + "|" + nombre + "|" + paterno + "|" + materno;
                            if (!titularesYaAgregados.contains(key)) {
                                titularesYaAgregados.add(key);
                                ContratoModelo.Persona p = new ContratoModelo.Persona(
                                        nombre, paterno, materno, ocupacion,
                                        String.valueOf(parentesco),
                                        fechaCumple != null ? convertirMesANombreString(dateOnlySdf.format(fechaCumple)) : ""
                                );
                                if ("Titular".equalsIgnoreCase(tipoTitular)) {
                                    m.getTitulares().add(p);
                                    if (mainTitular == null) {
                                        mainTitular = new VentasTitulares();
                                        mainTitular.nombre  = nombre;
                                        mainTitular.paterno = paterno;
                                    }
                                } else {
                                    m.getBeneficiarios().add(p);
                                }
                            }

                            if (!infoMapped) {
                                infoMapped = true;
                                mapInfoGeneral(rs, m, dateOnlySdf);
                                mapInventario(rs, m);
                                mapFinanciamiento(rs, m, dateOnlySdf);
                                mapRedesSociales(rs, m);
                            }
                        }

                        if (mainTitular != null) {
                            m.setClientName((mainTitular.nombre + " " + (mainTitular.paterno != null ? mainTitular.paterno : "")).trim());
                        } else {
                            m.setClientName("Contrato #" + idContrato);
                        }
                    }
                }
            }

            // ── Result set 3: descuentos ───────────────────────────────────
            hasResults = cs.getMoreResults();
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null) {
                        while (rs.next()) {
                            m.getDescuentosDetalle().add(new ContratoModelo.DescuentoDetalle(
                                    formateaMontos(rs.getString("MontoDescuento")),
                                    rs.getString("Descripcion")));
                        }
                    }
                }
            }

            // ── Result set 4: enganche diferido ────────────────────────────
            hasResults = cs.getMoreResults();
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null) {
                        while (rs.next()) {
                            java.sql.Date fp = rs.getDate("FechaPago");
                            m.getPagosDiferidos().add(new ContratoModelo.PagoDiferido(
                                    formateaMontos(rs.getString("CantidadPago")),
                                    fp != null ? convertirMesANombreString(dateOnlySdf.format(fp)) : ""));
                        }
                    }
                }
            }

            // ── Result set 5: monto cuenta ─────────────────────────────────
            hasResults = cs.getMoreResults();
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null) {
                        while (rs.next()) m.getContratosMontoCuenta().add(rs.getString("Xref"));
                    }
                }
            }

            // ── Result set 6: regalos ──────────────────────────────────────
            hasResults = cs.getMoreResults();
            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs != null) {
                        while (rs.next()) m.getRegalos().add(rs.getString("Descripcion"));
                    }
                }
            }
        }

        m.setDatosListos(true);
        return m;
    }

    // ──  helpers privados────────────────────────────────────────────────────────

    private void mapInfoGeneral(ResultSet rs, ContratoModelo m,
                                SimpleDateFormat dateOnlySdf) throws SQLException {
        String pais = rs.getString("Pais");
        m.setPais(pais);
        m.setNacionalidad(rs.getString("Nacionalidad"));
        m.setTipoDir(rs.getString("TipoDir"));

        String email1 = rs.getString("Email1");
        String email2 = rs.getString("Email2");
        m.setNoCorreo(email1 == null && email2 == null);

        if ("México".equalsIgnoreCase(pais)) {
            m.setMexCalle(rs.getString("Calle"));
            m.setMexNumExt(rs.getString("NoExt"));
            m.setMexNumInt(rs.getString("NoInt"));
            m.setMexColonia(rs.getString("Colonia"));
            m.setDelegacion(rs.getString("Delegacion"));
            m.setMexCiudad(rs.getString("Ciudad"));
            m.setMexEstado(rs.getString("Estado"));
            m.setMexCP(rs.getString("CP"));
        } else if (pais != null && (pais.equalsIgnoreCase("EEUU") || pais.equalsIgnoreCase("USA") || pais.contains("USA"))) {
            m.setUsaCalle(rs.getString("Calle"));
            m.setUsaCity(rs.getString("Ciudad"));
            m.setUsaState(rs.getString("Estado"));
            m.setUsaZip(rs.getString("CP"));
            m.setUsaNeighborhood(rs.getString("Colonia"));
            m.setPoBox(rs.getString("PoBox"));
            m.setBox(rs.getString("Box"));
            m.setCmr(rs.getString("Cmr"));
            m.setApo(rs.getString("Apo"));
        } else if ("Canadá".equalsIgnoreCase(pais)) {
            m.setCanCalle(rs.getString("Calle"));
            m.setCanCity(rs.getString("Ciudad"));
            m.setCanProvince(rs.getString("Estado"));
            m.setCanPostalCode(rs.getString("CP"));
        } else {
            m.setLinea1(rs.getString("Linea1"));
            m.setLinea2(rs.getString("Linea2"));
            m.setLinea3(rs.getString("Linea3"));
            m.setLinea4(rs.getString("Linea4"));
            m.setLinea5(rs.getString("Linea5"));
            m.setPaisOtro(pais);
        }

        String telDefault = rs.getString("TelefonoDefault");
        addTelefono(m, "Casa 1",    rs.getString("LadaCasa1"),    rs.getString("TelefonoCasa1"),    rs.getString("WhatsAppCasa1"),    telDefault);
        addTelefono(m, "Casa 2",    rs.getString("LadaCasa2"),    rs.getString("TelefonoCasa2"),    rs.getString("WhatsAppCasa2"),    telDefault);
        addTelefono(m, "Celular 1", rs.getString("LadaCelular1"), rs.getString("TelefonoCelular1"), rs.getString("WhatsAppCelular1"), telDefault);
        addTelefono(m, "Celular 2", rs.getString("LadaCelular2"), rs.getString("TelefonoCelular2"), rs.getString("WhatsAppCelular2"), telDefault);
        addTelefono(m, "Oficina 1", rs.getString("LadaOficina1"), rs.getString("TelefonoOficina1"), rs.getString("WhatsAppOficina1"), telDefault);
        addTelefono(m, "Oficina 2", rs.getString("LadaOficina2"), rs.getString("TelefonoOficina2"), rs.getString("WhatsAppOficina2"), telDefault);
        addTelefono(m, "Mensajes",  rs.getString("LadaMensajes"),  rs.getString("TelefonoMensajes"), rs.getString("WhatsAppMensajes"),  telDefault);

        if (email1 != null) m.getEmails().add(email1);
        if (email2 != null) m.getEmails().add(email2);
        String email3 = rs.getString("Email3"); if (email3 != null) m.getEmails().add(email3);
        String email4 = rs.getString("Email4"); if (email4 != null) m.getEmails().add(email4);
    }

    private void addTelefono(ContratoModelo m, String tipo, String lada,
                             String telefono, String whatsapp, String telDefault) {
        if (telefono != null && !telefono.isEmpty()) {
            m.getTelefonos().add(new ContratoModelo.InfoTelefono(
                    tipo, lada, telefono, "1".equals(whatsapp), tipo.equals(telDefault)));
        }
    }

    private void mapInventario(ResultSet rs, ContratoModelo m) throws SQLException {
        m.setUnidad(rs.getString("Unidad"));
        m.setTemporada(rs.getString("Temporada"));
        m.setAnioUso(rs.getString("PrimerAñoUso"));
        m.setTipoOcupacion(rs.getString("TipoOcupacion"));
        m.setNoAnios(rs.getString("AñosComprados"));
        m.setMoneda(rs.getString("MonedaVenta"));
        m.setTipoCambio(rs.getString("TipoCambioVenta"));
        m.setPrecioBruto(formateaMontos(rs.getString("PrecioBruto")));
        m.setMontoCuenta(formateaMontos(rs.getString("MontoCta")));
        m.setPrecioNeto(formateaMontos(rs.getString("PrecioNeto")));
        m.setNoContratosMC(rs.getString("NoContratosMontoCta"));
        m.setTipoPago(rs.getString("TipoPago"));
        m.setEngancheTotal(formateaMontos(rs.getString("EngancheTotal")));
        m.setEnganchePorcentaje(rs.getString("EngancheTotalPorcentaje"));
        m.setEngancheSalaMonto(formateaMontos(rs.getString("EnganchePagarSala")));
        m.setEngancheSalaPorcentaje(rs.getString("EnganchePagarSalaPorcentaje"));
        m.setVariosMonto(formateaMontos(rs.getString("Descuentos")));
        m.setNoDesc(rs.getString("NoDescuentos"));
        m.setEngDiferidoMonto(formateaMontos(rs.getString("EngancheDiferido")));
        m.setNoPagosEng(rs.getString("NoPagosEngancheDiferido"));
        m.setSaldoEnganche(formateaMontos(rs.getString("SaldoEnganche")));
        m.setMontoFinanciar(formateaMontos(rs.getString("MontoFinanciar")));
        m.setCostoContrato(formateaMontos(rs.getString("CostoContrato")));
        m.setPagoSala(formateaMontos(rs.getString("TotalPagoSala")));
        m.setCostoMembresia(formateaMontos(rs.getString("CostoMembresia")));
        m.setComentarios(rs.getString("ComentariosRegalos"));
        m.setTipoVenta(rs.getString("TipoVenta"));
        m.setTipoPagoEnganche(rs.getString("TipoPagoDiferido"));
    }

    private void mapFinanciamiento(ResultSet rs, ContratoModelo m,
                                   SimpleDateFormat dateOnlySdf) throws SQLException {
        String tipoPeriodo = rs.getString("TipoPeriodo");
        if (tipoPeriodo != null) {
            m.setTipoPeriodo(tipoPeriodo);
            java.sql.Date fechaPrimerPago = rs.getDate("FechaPrimerPago");
            m.setFechaPrimerPago(fechaPrimerPago != null
                    ? convertirMesANombreString(dateOnlySdf.format(fechaPrimerPago)) : "");
            m.setNumPagos(rs.getString("NumeroPagos"));
            m.setTasaInteres(rs.getString("TasaInteres"));
        }
    }

    private void mapRedesSociales(ResultSet rs, ContratoModelo m) throws SQLException {
        String fb = rs.getString("UsuarioFacebook");  if (fb != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Facebook",  fb));
        String ig = rs.getString("UsuarioInstagram"); if (ig != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Instagram", ig));
        String tw = rs.getString("UsuarioTwitter");   if (tw != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Twitter",   tw));
    }
     */
    //Aqui empieza cintas

    public long getNextId() throws SQLException {
        String sql = "SELECT ISNULL(MAX(IdContrato), 0) + 1 AS NextId FROM PMT_App_Ventas_Contrato";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NextId");
            }
        }
        return 1;
    }

    public void insert(VentasContrato c) throws SQLException {
        String sql = "INSERT INTO PMT_App_Ventas_Contrato (IdContrato, FechaAlta, IdUsuarioAlta, FechaModificacion, Estatus, Idioma) VALUES (?, ?, ?, ?, ?, ? )";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, c.idContrato);
            ps.setTimestamp(2, c.fechaAlta);
            ps.setLong(3, c.idUsuarioAlta);
            ps.setTimestamp(4, c.fechaModificacion);
            ps.setString(5, c.estatus);
            ps.setString(6, c.idioma);

            ps.executeUpdate();
        }
    }

    public void update(VentasContrato c) throws SQLException {
        String sql = "UPDATE PMT_App_Ventas_Contrato SET FechaModificacion=?, Estatus=?, Idioma=? WHERE IdContrato=?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, c.fechaModificacion);
            ps.setString(2, c.estatus);
            ps.setString(3, c.idioma);
            ps.setLong(4, c.idContrato);
            ps.executeUpdate();
        }
    }

    public VentasContrato getById(long id) throws SQLException {
        String sql = "SELECT * FROM PMT_App_Ventas_Contrato WHERE IdContrato = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    VentasContrato c = new VentasContrato();
                    c.idContrato = rs.getLong("IdContrato");
                    c.fechaAlta = rs.getTimestamp("FechaAlta");
                    c.idUsuarioAlta = rs.getLong("IdUsuarioAlta");
                    c.fechaModificacion = rs.getTimestamp("FechaModificacion");
                    c.estatus = rs.getString("Estatus");
                    c.idioma = rs.getString("Idioma");
                    return c;
                }
            }
        }
        return null;
    }




    //obtiene los datos necesarios para la lista de contratos en historial en un solo query
    public List<ContratoModelo> getResumenByUserId(long usuarioId) throws SQLException {
        List<ContratoModelo> models = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        String sql = "SELECT c.IdContrato, c.Estatus, c.Idioma, c.FechaAlta, c.FechaModificacion, " +
                "t.Nombre, t.Paterno " +
                "FROM PMT_App_Ventas_Contrato c " +
                "LEFT JOIN PMT_App_Ventas_Titulares t ON t.IdTitular = (" +
                "    SELECT TOP 1 IdTitular FROM PMT_App_Ventas_Titulares " +
                "    WHERE IdContrato = c.IdContrato " +
                "    AND TipoTitular = 'Titular' " +
                "    AND Estatus = 'A' " +
                "    ORDER BY IdTitular ASC" +
                ") " +
                "WHERE c.IdUsuarioAlta = ? " +
                "ORDER BY c.FechaAlta DESC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ContratoModelo m = new ContratoModelo();
                    m.setId(String.valueOf(rs.getLong("IdContrato")));
                    m.setEstatus(rs.getString("Estatus"));
                    m.setIdioma(mapIdiomaFromDb(rs.getString("Idioma")));

                    Timestamp fechaAlta = rs.getTimestamp("FechaAlta");
                    Timestamp fechaMod  = rs.getTimestamp("FechaModificacion");
                    if (fechaAlta != null) m.setFechaCreacion(sdf.format(fechaAlta));
                    if (fechaMod  != null) m.setFechaModificacion(sdf.format(fechaMod));

                    String nombre  = rs.getString("Nombre");
                    String paterno = rs.getString("Paterno");
                    if (nombre != null) {
                        m.setClientName((nombre + " " + (paterno != null ? paterno : "")).trim());
                    } else {
                        m.setClientName("Contrato #" + rs.getLong("IdContrato"));
                    }

                    models.add(m);
                }
            }
        }
        return models;
    }


    public ContratoModelo getContratoCompleto(long idContrato) throws SQLException {
        Log.d("CONTRATO_DB", "Sí entró al metodo el contrato = " + idContrato);
        SimpleDateFormat sdf         = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy",       Locale.getDefault());

        ContratoModelo m = new ContratoModelo();
        m.setId(String.valueOf(idContrato));

        VentasContrato vc = getById(idContrato);
        if (vc != null) {
            m.setModoEdicion(true);
            m.setEstatus(vc.estatus);
            m.setIdioma(mapIdiomaFromDb(vc.idioma));
            if (vc.fechaAlta         != null) m.setFechaCreacion(sdf.format(vc.fechaAlta));
            if (vc.fechaModificacion != null) m.setFechaModificacion(sdf.format(vc.fechaModificacion));
        } else {
            m.setModoEdicion(false);
        }

        String sql =
                "SELECT " +
                        "t.Nombre, t.Paterno, t.Materno, t.TipoTitular, t.Ocupacion, t.Parentesco, t.FechaCumpleaños, " +
                        "ig.Pais, ig.Nacionalidad, ig.TipoDir, ig.Email1, ig.Email2, ig.Email3, ig.Email4, " +
                        "ig.Calle, ig.NoExt, ig.NoInt, ig.Colonia, ig.Delegacion, ig.Ciudad, ig.Estado, ig.CP, " +
                        "ig.PoBox, ig.Box, ig.Cmr, ig.Apo, ig.Linea1, ig.Linea2, ig.Linea3, ig.Linea4, ig.Linea5, " +
                        "ig.LadaCasa1, ig.TelefonoCasa1, ig.WhatsAppCasa1, " +
                        "ig.LadaCasa2, ig.TelefonoCasa2, ig.WhatsAppCasa2, " +
                        "ig.LadaCelular1, ig.TelefonoCelular1, ig.WhatsAppCelular1, " +
                        "ig.LadaCelular2, ig.TelefonoCelular2, ig.WhatsAppCelular2, " +
                        "ig.LadaOficina1, ig.TelefonoOficina1, ig.WhatsAppOficina1, " +
                        "ig.LadaOficina2, ig.TelefonoOficina2, ig.WhatsAppOficina2, " +
                        "ig.LadaMensajes, ig.TelefonoMensajes, ig.WhatsAppMensajes, ig.TelefonoDefault, " +
                        "inv.Unidad, inv.Temporada, inv.TipoVenta, inv.AñosComprados, inv.PrimerAñoUso, " +
                        "inv.MonedaVenta, inv.TipoCambioVenta, inv.PrecioBruto, inv.MontoCta, inv.NoContratosMontoCta, " +
                        "inv.PrecioNeto, inv.TipoPago, inv.EngancheTotal, inv.EngancheTotalPorcentaje, " +
                        "inv.EnganchePagarSala, inv.EnganchePagarSalaPorcentaje, inv.Descuentos, inv.NoDescuentos, " +
                        "inv.EngancheDiferido, inv.NoPagosEngancheDiferido, inv.SaldoEnganche, inv.MontoFinanciar, " +
                        "inv.CostoContrato, inv.TotalPagoSala, inv.CostoMembresia, inv.ComentariosRegalos, inv.TipoOcupacion, inv.TipoPagoDiferido, " +
                        "f.TipoPeriodo, f.FechaPrimerPago, f.NumeroPagos, f.TasaInteres, " +
                        "rs.UsuarioFacebook, rs.UsuarioInstagram, rs.UsuarioTwitter " +
                        "FROM PMT_App_Ventas_Titulares t " +
                        "LEFT JOIN PMT_App_Ventas_Informacion_General ig  ON ig.IdContrato  = t.IdContrato AND ig.Estatus = 'A'" +
                        "LEFT JOIN PMT_App_Ventas_Datos_Inventario inv   ON inv.IdContrato = t.IdContrato AND inv.Estatus = 'A' " +
                        "LEFT JOIN PMT_App_Ventas_Financiamientos f      ON f.IdContrato   = t.IdContrato AND f.Estatus = 'A' " +
                        "LEFT JOIN PMT_App_Ventas_Redes_Sociales rs      ON rs.IdContrato  = t.IdContrato AND inv.Estatus = 'A' " +
                        "WHERE t.IdContrato = ? AND t.Estatus = 'A'";

        String sqlDescuentos = "SELECT MontoDescuento, Descripcion FROM PMT_App_Ventas_Descuentos WHERE IdContrato = ? AND Estatus = 'A'";
        String sqlEnganche   = "SELECT CantidadPago, FechaPago FROM PMT_App_Ventas_EngancheDiferido WHERE IdContrato = ? AND Estatus = 'A'";
        String sqlMontoCta   = "SELECT Xref FROM PMT_App_Ventas_Monto_Cta WHERE IdContrato = ? AND Estatus = 'A'";
        String sqlRegalos    = "SELECT Descripcion FROM PMT_App_Ventas_Regalos WHERE IdContrato = ? AND Estatus = 'A'";

        try (Connection conn = DbConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    VentasTitulares mainTitular = null;
                    boolean infoMapped = false;
                    java.util.Set<String> titularesYaAgregados = new java.util.HashSet<>();

                    while (rs.next()) {
                        String tipoTitular = rs.getString("TipoTitular");
                        String nombre      = rs.getString("Nombre");
                        String paterno     = rs.getString("Paterno");
                        String materno     = rs.getString("Materno");
                        String ocupacion   = rs.getString("Ocupacion");
                        long   parentesco  = rs.getLong("Parentesco");
                        java.sql.Date fechaCumple = rs.getDate("FechaCumpleaños");

                        String key = tipoTitular + "|" + nombre + "|" + paterno + "|" + materno;
                        if (!titularesYaAgregados.contains(key)) {
                            titularesYaAgregados.add(key);
                            ContratoModelo.Persona p = new ContratoModelo.Persona(
                                    nombre, paterno, materno, ocupacion,
                                    String.valueOf(parentesco),
                                    fechaCumple != null ? convertirMesANombreString(dateOnlySdf.format(fechaCumple)) : ""
                            );
                            if ("Titular".equalsIgnoreCase(tipoTitular)) {
                                m.getTitulares().add(p);
                                if (mainTitular == null) {
                                    mainTitular = new VentasTitulares();
                                    mainTitular.nombre  = nombre;
                                    mainTitular.paterno = paterno;
                                }
                            } else {
                                m.getBeneficiarios().add(p);
                            }
                        }

                        if (!infoMapped) {
                            infoMapped = true;

                            String pais = rs.getString("Pais");
                            m.setPais(pais);
                            m.setNacionalidad(rs.getString("Nacionalidad"));
                            m.setTipoDir(rs.getString("TipoDir"));
                            String email1 = rs.getString("Email1");
                            String email2 = rs.getString("Email2");
                            m.setNoCorreo(email1 == null && email2 == null);

                            if ("México".equalsIgnoreCase(pais)) {
                                m.setMexCalle(rs.getString("Calle"));
                                m.setMexNumExt(rs.getString("NoExt"));
                                m.setMexNumInt(rs.getString("NoInt"));
                                m.setMexColonia(rs.getString("Colonia"));
                                m.setDelegacion(rs.getString("Delegacion"));
                                m.setMexCiudad(rs.getString("Ciudad"));
                                m.setMexEstado(rs.getString("Estado"));
                                m.setMexCP(rs.getString("CP"));
                            } else if ("EEUU".equalsIgnoreCase(pais) || "USA".equalsIgnoreCase(pais) || (pais != null && pais.contains("USA"))) {
                                m.setUsaCalle(rs.getString("Calle"));
                                m.setUsaCity(rs.getString("Ciudad"));
                                m.setUsaState(rs.getString("Estado"));
                                m.setUsaZip(rs.getString("CP"));
                                m.setUsaNeighborhood(rs.getString("Colonia"));
                                m.setPoBox(rs.getString("PoBox"));
                                m.setBox(rs.getString("Box"));
                                m.setCmr(rs.getString("Cmr"));
                                m.setApo(rs.getString("Apo"));
                            } else if ("Canadá".equalsIgnoreCase(pais)) {
                                m.setCanCalle(rs.getString("Calle"));
                                m.setCanCity(rs.getString("Ciudad"));
                                m.setCanProvince(rs.getString("Estado"));
                                m.setCanPostalCode(rs.getString("CP"));
                            } else {
                                m.setLinea1(rs.getString("Linea1"));
                                m.setLinea2(rs.getString("Linea2"));
                                m.setLinea3(rs.getString("Linea3"));
                                m.setLinea4(rs.getString("Linea4"));
                                m.setLinea5(rs.getString("Linea5"));
                                m.setPaisOtro(pais);
                            }

                            String telDefault = rs.getString("TelefonoDefault");
                            String tc1   = rs.getString("TelefonoCasa1");    if (tc1   != null && !tc1.isEmpty())   m.getTelefonos().add(new ContratoModelo.InfoTelefono("Casa 1",    rs.getString("LadaCasa1"),    tc1,   "1".equals(rs.getString("WhatsAppCasa1")),    "Casa 1".equals(telDefault)));
                            String tc2   = rs.getString("TelefonoCasa2");    if (tc2   != null && !tc2.isEmpty())   m.getTelefonos().add(new ContratoModelo.InfoTelefono("Casa 2",    rs.getString("LadaCasa2"),    tc2,   "1".equals(rs.getString("WhatsAppCasa2")),    "Casa 2".equals(telDefault)));
                            String tcel1 = rs.getString("TelefonoCelular1"); if (tcel1 != null && !tcel1.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Celular 1", rs.getString("LadaCelular1"), tcel1, "1".equals(rs.getString("WhatsAppCelular1")), "Celular 1".equals(telDefault)));
                            String tcel2 = rs.getString("TelefonoCelular2"); if (tcel2 != null && !tcel2.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Celular 2", rs.getString("LadaCelular2"), tcel2, "1".equals(rs.getString("WhatsAppCelular2")), "Celular 2".equals(telDefault)));
                            String tof1  = rs.getString("TelefonoOficina1"); if (tof1  != null && !tof1.isEmpty())  m.getTelefonos().add(new ContratoModelo.InfoTelefono("Oficina 1", rs.getString("LadaOficina1"), tof1,  "1".equals(rs.getString("WhatsAppOficina1")), "Oficina 1".equals(telDefault)));
                            String tof2  = rs.getString("TelefonoOficina2"); if (tof2  != null && !tof2.isEmpty())  m.getTelefonos().add(new ContratoModelo.InfoTelefono("Oficina 2", rs.getString("LadaOficina2"), tof2,  "1".equals(rs.getString("WhatsAppOficina2")), "Oficina 2".equals(telDefault)));
                            String tms   = rs.getString("TelefonoMensajes");  if (tms  != null && !tms.isEmpty())   m.getTelefonos().add(new ContratoModelo.InfoTelefono("Mensajes",  rs.getString("LadaMensajes"),  tms,   "1".equals(rs.getString("WhatsAppMensajes")),  "Mensajes".equals(telDefault)));

                            if (email1 != null) m.getEmails().add(email1);
                            if (email2 != null) m.getEmails().add(email2);
                            String email3 = rs.getString("Email3"); if (email3 != null) m.getEmails().add(email3);
                            String email4 = rs.getString("Email4"); if (email4 != null) m.getEmails().add(email4);

                            m.setUnidad(rs.getString("Unidad"));
                            m.setTemporada(rs.getString("Temporada"));
                            m.setAnioUso(rs.getString("PrimerAñoUso"));
                            m.setTipoOcupacion(rs.getString("TipoOcupacion"));
                            m.setNoAnios(rs.getString("AñosComprados"));
                            m.setMoneda(rs.getString("MonedaVenta"));
                            m.setTipoCambio(rs.getString("TipoCambioVenta"));
                            m.setPrecioBruto(formateaMontos(rs.getString("PrecioBruto")));
                            m.setMontoCuenta(formateaMontos(rs.getString("MontoCta")));
                            m.setPrecioNeto(formateaMontos(rs.getString("PrecioNeto")));
                            m.setNoContratosMC(rs.getString("NoContratosMontoCta"));
                            m.setTipoPago(rs.getString("TipoPago"));
                            m.setEngancheTotal(formateaMontos(rs.getString("EngancheTotal")));
                            m.setEnganchePorcentaje(rs.getString("EngancheTotalPorcentaje"));
                            m.setEngancheSalaMonto(formateaMontos(rs.getString("EnganchePagarSala")));
                            m.setEngancheSalaPorcentaje(rs.getString("EnganchePagarSalaPorcentaje"));
                            m.setVariosMonto(formateaMontos(rs.getString("Descuentos")));
                            m.setNoDesc(rs.getString("NoDescuentos"));
                            m.setEngDiferidoMonto(formateaMontos(rs.getString("EngancheDiferido")));
                            m.setNoPagosEng(rs.getString("NoPagosEngancheDiferido"));
                            m.setSaldoEnganche(formateaMontos(rs.getString("SaldoEnganche")));
                            m.setMontoFinanciar(formateaMontos(rs.getString("MontoFinanciar")));
                            m.setCostoContrato(formateaMontos(rs.getString("CostoContrato")));
                            m.setPagoSala(formateaMontos(rs.getString("TotalPagoSala")));
                            m.setCostoMembresia(formateaMontos(rs.getString("CostoMembresia")));
                            m.setComentarios(rs.getString("ComentariosRegalos"));
                            m.setTipoVenta(rs.getString("TipoVenta"));
                            m.setTipoPagoEnganche(rs.getString("TipoPagoDiferido"));

                            String tipoPeriodo = rs.getString("TipoPeriodo");
                            if (tipoPeriodo != null) {
                                m.setTipoPeriodo(tipoPeriodo);
                                java.sql.Date fechaPrimerPago = rs.getDate("FechaPrimerPago");
                                m.setFechaPrimerPago(fechaPrimerPago != null ? convertirMesANombreString(dateOnlySdf.format(fechaPrimerPago)) : "");
                                m.setNumPagos(rs.getString("NumeroPagos"));
                                m.setTasaInteres(rs.getString("TasaInteres"));
                            }

                            String fb = rs.getString("UsuarioFacebook");  if (fb != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Facebook",  fb));
                            String ig = rs.getString("UsuarioInstagram"); if (ig != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Instagram", ig));
                            String tw = rs.getString("UsuarioTwitter");   if (tw != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Twitter",   tw));
                        }
                    }

                    if (mainTitular != null) {
                        m.setClientName((mainTitular.nombre + " " + (mainTitular.paterno != null ? mainTitular.paterno : "")).trim());
                    } else {
                        m.setClientName("Contrato #" + idContrato);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDescuentos)) {
                ps.setLong(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) m.getDescuentosDetalle().add(new ContratoModelo.DescuentoDetalle(formateaMontos(rs.getString("MontoDescuento")), rs.getString("Descripcion")));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlEnganche)) {
                ps.setLong(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) m.getPagosDiferidos().add(new ContratoModelo.PagoDiferido(formateaMontos(rs.getString("CantidadPago")), rs.getDate("FechaPago") != null ? convertirMesANombreString(dateOnlySdf.format(rs.getDate("FechaPago"))) : ""));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlMontoCta)) {
                ps.setLong(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) m.getContratosMontoCuenta().add(rs.getString("Xref"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlRegalos)) {
                ps.setLong(1, idContrato);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) m.getRegalos().add(rs.getString("Descripcion"));
                }
            }
        }

        m.setDatosListos(true);
        return m;
    }


}