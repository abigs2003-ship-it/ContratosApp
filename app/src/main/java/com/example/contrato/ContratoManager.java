package com.example.contrato;

import java.util.ArrayList;
import java.util.List;

public class ContratoManager {
    private static ContratoManager instance;
    private List<ContratoModelo> contracts;

    private ContratoManager() {
        contracts = new ArrayList<>();
        // Mock data
        ContratoModelo m1 = new ContratoModelo();
        m1.setClientName("Juan Perez");
        m1.setCreationDate("10/02/2024 10:00");
        m1.setModifiedDate("10/02/2024 10:00");
        
        ContratoModelo.Person p1 = new ContratoModelo.Person("Juan", "Perez", "Gomez", "Ingeniero", "Titular", "01/01/1980");
        m1.getTitulares().add(p1);
        
        m1.setPrecioBruto("50000");
        contracts.add(m1);
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

    public void addContract(ContratoModelo contract) {
        contracts.add(0, contract);
    }

    public void updateContract(ContratoModelo updatedContract) {
        boolean found = false;
        for (int i = 0; i < contracts.size(); i++) {
            if (contracts.get(i).getId().equals(updatedContract.getId())) {
                contracts.set(i, updatedContract);
                found = true;
                break;
            }
        }
        if (!found) {
            addContract(updatedContract);
        }
    }
}
