package tenis.interfaces;

/**
 * Contrato para toda clase que requiera identificar de manera unívoca a sus objetos.
 * Lo usa {@code Repositorio} para buscar y eliminar por id.
 */
public interface Identificable {

    /**
     * @return identificador único del objeto
     */
    int getId();
}
