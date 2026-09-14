package pe.edu.pucp.klam.modelo;

public abstract class Registro {
    private int id;
    private boolean activo;
    public Registro (){}

    public Registro (final Registro registro){
        if (registro == null){
            throw new IllegalArgumentException("Registro no puede ser nulo");
        }
        this.id = registro.id;
        this.activo = registro.activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
