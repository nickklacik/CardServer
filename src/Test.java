
import java.util.ArrayList;
/**
 *
 * @author Nick and Justin
 */
public class Test 
{
    public static void main(String[] args)
    {
//        Card.createDeck(3);
//        Card.shuffleDeck();
//        System.out.println(Card.deck);
        
        Server server = new Server(1996);
        server.startServer();
    }
}
