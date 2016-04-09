
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Nick and Justin
 */
public class ServerClient implements Runnable
{
    Server server;
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String name;
    static int n;
    Card[] hand = new Card[2];
    int cash;
    boolean dealer, bigBlind, smallBlind, woot, folded;
    
    public ServerClient(Socket socket, Server server)
    {
        this.socket = socket;
        this.server = server;
        
        try 
        {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException ex) 
        {
            System.err.println("Failed to set up input and output streams");
        }
    }

    @Override
    public void run()
    {
        System.out.println("connected and started");
        Object obj;
        String s;
        Integer bet;
        while(socket.isConnected())
        {
            obj = null;
            try {
                obj = in.readObject();
            } catch (IOException ex) {
                Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(obj instanceof String)
            {
                s = (String) obj;
                if(s.startsWith("/setName "))
                {
                    s = s.substring(9);
                    if(server.checkName(s))
                    {
                        try {
                            out.writeObject("Name exists, to set name type \"/setName \'name\'\"");
                        } catch (IOException ex) {
                            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if(name == null)
                            setName("Client " + n++);
                        else if(!name.startsWith("Client "))
                            setName("Client " + n++);
                    }
                    else
                    {
                        setName(s);
                    }
                }
                else if(s.startsWith("/woot"))
                {
                    woot = true;
                    server.message(name + " has wooted\n");
                }
                else if(s.startsWith("/fold"))
                    folded = true;
                else if(s.startsWith("/drill"))
                    server.drill(name);
                else
                    server.message(s);
            }
            else if(obj instanceof Integer)
            {
                bet = (Integer) obj;
                cash -= bet;
                server.addToPot(bet);
                server.message(name + " has bet $" + bet + "\n");
            }
        }
    }
    
    public void dealHand()
    {
        hand[0] = Card.takeCard();
        hand[1] = Card.takeCard();
        try {
            out.writeObject(hand);
        } catch (IOException ex) {
////            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendCash(Integer n)
    {
        cash += n;
        try {
            out.writeObject(n);
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setName(String name)
    {
        this.name = name;
        try {
            out.writeObject("/name " + name);
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String toString()
    {
        return "Name: " + name;
    }
}
