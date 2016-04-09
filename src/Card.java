
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.ImageIcon;



/**
 *
 * @author Nick
 */
public class Card implements Serializable
{
    enum Suit {SPADES, CLUBS, HEARTS, DIAMONDS}
    enum Rank {DUECE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE}
    Suit suit;
    Rank rank;
    ImageIcon image;
    public final static ImageIcon BACK = new ImageIcon("cards/b1fv.png");
    static ArrayList<Card> deck = new ArrayList();
    
    public Card(Suit suit, Rank rank)
    {
        this.suit = suit;
        this.rank = rank;
        image = new ImageIcon("cards/" + rank + "_OF_" + suit + ".png");
    }
    
    public static void createDeck(int n)
    {
        deck.clear();
        for(int i = 0; i<n; i++)
            for(Suit suit : Suit.values())
                for(Rank rank : Rank.values())
                    deck.add(new Card(suit,rank));
    }
    
    public static void shuffleDeck()
    {
        Collections.shuffle(deck);
    }
    
    public static Card takeCard()
    {
        return deck.remove(0);
    }
    
    @Override
    public String toString()
    {
        return rank + "_OF_" + suit;
    }
}
