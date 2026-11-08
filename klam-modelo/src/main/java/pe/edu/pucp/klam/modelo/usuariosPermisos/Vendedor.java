package pe.edu.pucp.klam.modelo.usuariosPermisos;


public class Vendedor extends UsuarioPlataforma {

    private static final double PORCENTAJE_COMISION = 0.035; // 5%

    private double comisionAcumulada;

    public Vendedor() {
        super();
        this.comisionAcumulada = 0.0;
        setRol("VENDEDOR");
    }

    public Vendedor(int id_usuario, String username, String passwordHash, String email,
                     String nombres, String apellidos, boolean activo, double comisionAcumulada) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "VENDEDOR", activo);
        this.comisionAcumulada = comisionAcumulada;
    }

    public Vendedor(Vendedor otro) {
        super(otro);
        this.comisionAcumulada = otro.comisionAcumulada;
    }

    public double getComisionAcumulada() {
        return comisionAcumulada;
    }

    public void setComisionAcumulada(double comisionAcumulada) {
        this.comisionAcumulada = comisionAcumulada;
    }

    public double calcularComision(double montoBase) {
        double comision = montoBase * PORCENTAJE_COMISION;
        this.comisionAcumulada += comision;
        return comision;
    }

}
