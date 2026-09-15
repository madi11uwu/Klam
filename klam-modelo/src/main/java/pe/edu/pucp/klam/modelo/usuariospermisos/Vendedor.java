package pe.edu.pucp.klam.modelo.usuariospermisos;

public class Vendedor extends UsuarioPlataforma{
    private String id_vendedor;
    private double comisionAcumulada;

    public Vendedor(final Vendedor vendedor) {
        if(vendedor==null){
            throw new IllegalArgumentException("vendedor no puede ser nulo");
        }
        super(vendedor);
        setId_vendedor(vendedor.getId_vendedor());
        setComisionAcumulada(vendedor.getComisionAcumulada());
    }

    public String getId_vendedor() {
        return id_vendedor;
    }

    public void setId_vendedor(String id_vendedor) {
        if(id_vendedor==null || id_vendedor.isEmpty()){
            throw new IllegalArgumentException("id_vendedor no puede ser nulo o vacío");
        }
        this.id_vendedor = id_vendedor;
    }

    public double getComisionAcumulada() {
        return comisionAcumulada;
    }

    public void setComisionAcumulada(double comisionAcumulada) {
        if(comisionAcumulada<0){
            throw new IllegalArgumentException("comisionAcumulada no puede ser negativo");
        }
        this.comisionAcumulada = comisionAcumulada;
    }
}
