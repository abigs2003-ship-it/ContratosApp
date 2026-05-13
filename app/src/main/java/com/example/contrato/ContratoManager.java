package com.example.contrato;

import java.util.ArrayList;
import java.util.List;

public class ContratoManager {
    private static ContratoManager instance;
    private List<ContratoModelo> contracts;

    private ContratoManager() {
        contracts = new ArrayList<>();
    }

    public static synchronized ContratoManager getInstance() {
        if (instance == null) {
            instance = new ContratoManager();
        }
        return instance;
    }

    public List<ContratoModelo> getContracts() {
        return contracts;
    }

    public void anadeContrato(ContratoModelo contract) {
        contracts.add(0, contract);
    }

    public void actualizaContrato(ContratoModelo updatedContract) {
        boolean found = false;
        for (int i = 0; i < contracts.size(); i++) {
            if (contracts.get(i).getId().equals(updatedContract.getId())) {
                contracts.set(i, updatedContract);
                found = true;
                break;
            }
        }
        if (!found) {
            anadeContrato(updatedContract);
        }
    }
}
