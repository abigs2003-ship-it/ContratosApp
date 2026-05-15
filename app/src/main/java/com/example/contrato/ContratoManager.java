package com.example.contrato;

import java.util.ArrayList;
import java.util.List;

public class ContratoManager {
    private static ContratoManager instance;
    private List<ContratoModelo> Contratos;

    private ContratoManager() {
        Contratos = new ArrayList<>();
    }

    public static synchronized ContratoManager getInstance() {
        if (instance == null) {
            instance = new ContratoManager();
        }
        return instance;
    }

    public List<ContratoModelo> getContratos() {
        return Contratos;
    }

    public void anadeContrato(ContratoModelo Contrato) {
        Contratos.add(0, Contrato);
    }

    public void actualizaContrato(ContratoModelo updatedContrato) {
        boolean found = false;
        for (int i = 0; i < Contratos.size(); i++) {
            if (Contratos.get(i).getId().equals(updatedContrato.getId())) {
                Contratos.set(i, updatedContrato);
                found = true;
                break;
            }
        }
        if (!found) {
            anadeContrato(updatedContrato);
        }
    }
}
