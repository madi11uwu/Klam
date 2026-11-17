package pe.edu.pucp.klam.rest.dto;

public class ResumenUsoInsumoDTO {
    private String nombre;
    private String medida;
    private String tipoCirugia;
    private int cantidadUtilizada;
    private int frecuencia;

    public ResumenUsoInsumoDTO() {
    }

    public ResumenUsoInsumoDTO(String nombre, String medida, String tipoCirugia, int cantidadUtilizada, int frecuencia) {
        this.nombre = nombre;
        this.medida = medida;
        this.tipoCirugia = tipoCirugia;
        this.cantidadUtilizada = cantidadUtilizada;
        this.frecuencia = frecuencia;
    }

    public String getNombre() { return nombre; }
    public String getMedida() { return medida; }
    public String getTipoCirugia() { return tipoCirugia; }
    public int getCantidadUtilizada() { return cantidadUtilizada; }
    public int getFrecuencia() { return frecuencia; }
}