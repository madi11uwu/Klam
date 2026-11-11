package pe.edu.pucp.klam.rest.dto;

import java.util.Map;

// Datos de presentación de un despacho (RF14). Combina la fila del despacho con los datos de
// negocio de su cirugía (cliente, clínica, procedimiento); la cirugía puede ser null.
public class DespachoDTO {
    private int idDespacho;
    private String codigo;
    private int idCirugia;
    private String cliente;
    private String clinica;
    private String procedimiento;
    private Integer idEquipo;
    private String equipo;
    private Integer idBandeja;
    private String kit;
    private String fechaSalida;
    private String fechaRetorno;
    private String estado;
    private boolean pendienteRetorno;

    public DespachoDTO() {
    }

    public DespachoDTO(Map<String, Object> despacho, Map<String, Object> cirugia) {
        if (despacho != null) {
            idDespacho = entero(despacho.get("idDespacho"), 0);
            codigo = texto(despacho.get("codigo"));
            idCirugia = entero(despacho.get("idCirugia"), 0);
            idEquipo = enteroONulo(despacho.get("idEquipo"));
            equipo = texto(despacho.get("equipoNombre"));
            idBandeja = enteroONulo(despacho.get("idBandeja"));
            kit = texto(despacho.get("bandejaTipo"));
            fechaSalida = fecha(despacho.get("fechaSalida"));
            fechaRetorno = fecha(despacho.get("fechaRetorno"));
            pendienteRetorno = despacho.get("fechaRetorno") == null;
            estado = pendienteRetorno ? "DESPACHADO" : "RETORNADO";
        }

        cliente = cirugia == null ? null : texto(cirugia.get("clienteNombre"));
        clinica = cirugia == null ? null : texto(cirugia.get("clinicaNombre"));
        procedimiento = cirugia == null ? null : texto(cirugia.get("tipoProcedimiento"));
    }

    public static int entero(Object valor, int porDefecto) {
        return valor instanceof Number n ? n.intValue() : porDefecto;
    }

    public static Integer enteroONulo(Object valor) {
        return valor instanceof Number n ? Integer.valueOf(n.intValue()) : null;
    }

    public static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    // LocalDateTime.toString() da ISO-8601, igual que el resto de DTO del módulo
    public static String fecha(Object valor) {
        return valor == null ? null : valor.toString();
    }

    public int getIdDespacho() { return idDespacho; }
    public void setIdDespacho(int idDespacho) { this.idDespacho = idDespacho; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getIdCirugia() { return idCirugia; }
    public void setIdCirugia(int idCirugia) { this.idCirugia = idCirugia; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getClinica() { return clinica; }
    public void setClinica(String clinica) { this.clinica = clinica; }

    public String getProcedimiento() { return procedimiento; }
    public void setProcedimiento(String procedimiento) { this.procedimiento = procedimiento; }

    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }

    public Integer getIdBandeja() { return idBandeja; }
    public void setIdBandeja(Integer idBandeja) { this.idBandeja = idBandeja; }

    public String getKit() { return kit; }
    public void setKit(String kit) { this.kit = kit; }

    public String getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(String fechaSalida) { this.fechaSalida = fechaSalida; }

    public String getFechaRetorno() { return fechaRetorno; }
    public void setFechaRetorno(String fechaRetorno) { this.fechaRetorno = fechaRetorno; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isPendienteRetorno() { return pendienteRetorno; }
    public void setPendienteRetorno(boolean pendienteRetorno) { this.pendienteRetorno = pendienteRetorno; }
}
