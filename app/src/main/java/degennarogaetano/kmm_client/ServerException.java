package degennarogaetano.kmm_client;

/**
 * Estende Exception. Utilizzata per lanciare eccezioni quando si ricevono delle informazioni che segnalano il fallimento
 * dell'elaborazione di un'operazione su Server.
 *
 * @author de Gennaro Gaetano, Farinola Francesco
 *
 */
public class ServerException extends Exception
{
    /**
     * Richiama il metodo della superclasse {@link java.lang.Exception#Exception(String)}
     *
     * @param msg messaggio che descrive l'eccezione avvenuta.
     */
    public ServerException(String msg)
    {
        super(msg);
    }
}
