package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

// Listas reales (MySQL) que necesita el formulario de cirugía
public class OpcionesCirugiaDTO {
    private List<OpcionDTO> clientes = new ArrayList<>();
    private List<OpcionDTO> equipos = new ArrayList<>();
    private List<BandejaDTO> bandejas = new ArrayList<>();
    private List<String> procedimientos = new ArrayList<>();

    public OpcionesCirugiaDTO() {
    }

    public List<OpcionDTO> getClientes() {
        return clientes;
    }

    public void setClientes(List<OpcionDTO> clientes) {
        this.clientes = clientes;
    }

    public List<OpcionDTO> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<OpcionDTO> equipos) {
        this.equipos = equipos;
    }

    public List<BandejaDTO> getBandejas() {
        return bandejas;
    }

    public void setBandejas(List<BandejaDTO> bandejas) {
        this.bandejas = bandejas;
    }

    public List<String> getProcedimientos() {
        return procedimientos;
    }

    public void setProcedimientos(List<String> procedimientos) {
        this.procedimientos = procedimientos;
    }
}
