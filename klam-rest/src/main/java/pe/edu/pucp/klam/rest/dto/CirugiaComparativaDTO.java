package pe.edu.pucp.klam.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class CirugiaComparativaDTO {
    private int idCirugia;
    private String fecha;
    private String procedimiento;
    private String paciente;
    private String documentoPaciente;
    private String medico;
    private String tipoCliente;
    private String cliente;
    private List<LineaComparativaDTO> insumos = new ArrayList<>();

    public CirugiaComparativaDTO() {
    }

    public CirugiaComparativaDTO(int idCirugia, String fecha, String procedimiento, String paciente,
                                 String documentoPaciente, String medico, String tipoCliente, String cliente,
                                 List<LineaComparativaDTO> insumos) {
        this.idCirugia = idCirugia;
        this.fecha = fecha;
        this.procedimiento = procedimiento;
        this.paciente = paciente;
        this.documentoPaciente = documentoPaciente;
        this.medico = medico;
        this.tipoCliente = tipoCliente;
        this.cliente = cliente;
        this.insumos = insumos;
    }

    public int getIdCirugia() { return idCirugia; }
    public String getFecha() { return fecha; }
    public String getProcedimiento() { return procedimiento; }
    public String getPaciente() { return paciente; }
    public String getDocumentoPaciente() { return documentoPaciente; }
    public String getMedico() { return medico; }
    public String getTipoCliente() { return tipoCliente; }
    public String getCliente() { return cliente; }
    public List<LineaComparativaDTO> getInsumos() { return insumos; }
}