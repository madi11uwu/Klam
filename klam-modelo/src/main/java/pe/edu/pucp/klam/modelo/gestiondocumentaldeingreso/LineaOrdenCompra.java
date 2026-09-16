package pe.edu.pucp.klam.modelo.gestiondocumentaldeingreso;

import pe.edu.pucp.klam.modelo.documentacionfinanzas.LineaDocumento;

public class LineaOrdenCompra extends LineaDocumento {

    private String itemReferencia;

    public LineaOrdenCompra(final LineaOrdenCompra lineaOrdenCompra) {
        if(lineaOrdenCompra==null){
            throw new IllegalArgumentException("La linea de orden de compra no puede ser nula");
        }
        super(lineaOrdenCompra);
        setItemReferencia(lineaOrdenCompra.getItemReferencia());
    }

    public String getItemReferencia() {
        return itemReferencia;
    }

    public void setItemReferencia(String itemReferencia) {
        this.itemReferencia = itemReferencia;
    }

}
