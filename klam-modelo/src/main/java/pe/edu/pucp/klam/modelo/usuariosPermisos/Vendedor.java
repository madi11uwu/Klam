package pe.edu.pucp.klam.modelo.usuariosPermisos;

public class Vendedor extends UsuarioPlataforma {

    private static final double PORCENTAJE_COMISION = 0.05; // 5%, ajustar según regla de negocio real

    private String id_vendedor;
    private double comisionAcumulada;

    public Vendedor() {
        super();
        this.id_vendedor = "";
        this.comisionAcumulada = 0.0;
        setRol("VENDEDOR");
    }

    public Vendedor(int id_usuario, String username, String passwordHash, String email,
                     String nombres, String apellidos, boolean activo,
                     String id_vendedor, double comisionAcumulada) {
        super(id_usuario, username, passwordHash, email, nombres, apellidos, "VENDEDOR", activo);
        this.id_vendedor = id_vendedor;
        this.comisionAcumulada = comisionAcumulada;
    }

    public Vendedor(Vendedor otro) {
        super(otro);
        this.id_vendedor = otro.id_vendedor;
        this.comisionAcumulada = otro.comisionAcumulada;
    }

    public String getIdVendedor() {
        return id_vendedor;
    }

    public void setIdVendedor(String id_vendedor) {
        this.id_vendedor = id_vendedor;
    }

    public double getComisionAcumulada() {
        return comisionAcumulada;
    }

    public void setComisionAcumulada(double comisionAcumulada) {
        this.comisionAcumulada = comisionAcumulada;
    }

    // Depende del módulo de ventas/cirugías (aún no implementado).
    public void consultarVentas() {
        throw new UnsupportedOperationException("Metodo no implementado aun.");
    }

    public double calcularComision(double montoBase) {
        double comision = montoBase * PORCENTAJE_COMISION;
        this.comisionAcumulada += comision;
        return comision;
    }

    @Override
    public String toString() {
        return "Vendedor{" +
                "id_vendedor='" + id_vendedor + '\'' +
                ", comisionAcumulada=" + comisionAcumulada +
                ", " + super.toString() +
                '}';
    }
}
