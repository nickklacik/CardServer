

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;



/**
 *
 * @author Nick and Justin
 */

public class Server extends JFrame implements ActionListener
{
    ArrayList<ServerClient> clients = new ArrayList();
    
    boolean gameInProgress, running;
    private int port; 
    private ServerSocket server;
    private JTextArea output;
    private JTextField cmdLine;
    private Font font = new Font("Helvetica", Font.BOLD, 12);
    Random rand = new Random();
//    ArrayList<Card> communityCards = new ArrayList();
    Card[] commCards = new Card[5];
    int pot;
    boolean betting, showdown;
    
    public Server(int port)
    {
        this.port = port;
        
        cmdLine = new JTextField();
        cmdLine.setFont(font);
        cmdLine.setBackground(Color.black);
        cmdLine.setForeground(Color.white);
        cmdLine.addActionListener(this);
        add(cmdLine, BorderLayout.SOUTH);
        
        ImageIcon icon = new ImageIcon("Drill_Icon.png");
        setIconImage(icon.getImage());
        
        output = new JTextArea();
        DefaultCaret caret = (DefaultCaret) output.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        output.setFont(font);
        output.setBackground(Color.black);
        output.setForeground(Color.white);
        output.setEditable(false);
        add(new JScrollPane(output));
        setSize(600,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    public void startServer()
    {
        setVisible(true);
        try 
        {
            server = new ServerSocket(port);
            running = true;
        } 
        catch (IOException ex) 
        {
            System.out.println("Failed to create server on port: " + port);
        }
        try {
            server.setSoTimeout(500);
        } catch (SocketException ex) {
            System.err.println("Failed to set Timeout");
        }
        run();
    }
    
    private void run()
    {
        ServerClient newClient;
        Thread t;
        while(running)
        {
            while(!gameInProgress)  //Check for connections, etc...
            {
                if(!running)
                    break;
                try 
                {
                    newClient = new ServerClient(server.accept(), this);
                    clients.add(newClient);
                    t = new Thread(newClient);
                    t.start();
                    
                }
                catch (IOException ex) 
                {
                    //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            startGame();
            while(gameInProgress) //Game Logic & Stuff
            {
                if(!running)
                    break;
                preFlop();
                flop();
                turn();
                river();
                showdown();
            }
        }
    }
    
    private void preFlop()
    {
        Card.createDeck(1);
        Card.shuffleDeck();
        Arrays.fill(commCards, null);
        for(ServerClient c : clients)
        {
            c.dealHand();
            c.folded = false;
        }
        betting();
    }
    
    private void flop()
    {
        Card.takeCard();
        commCards[0] = Card.takeCard();
        commCards[1] = Card.takeCard();
        commCards[2] = Card.takeCard();
        message(Arrays.toString(commCards) +"\n");
        betting();
    }
    
    private void turn()
    {
        Card.takeCard();
        commCards[3] = Card.takeCard();
        message(Arrays.toString(commCards) +"\n");
        betting();
    }
    
    private void river()
    {
        Card.takeCard();
        commCards[4] = Card.takeCard();
        message(Arrays.toString(commCards) +"\n");
        betting();
    }
    
    private void showdown()
    {
        showdown = true;
        message("Community Cards: " + Arrays.toString(commCards) +"\n");
        message("Player hands:\n");
        for(ServerClient c : clients)
            if(!c.folded)
                message("\t" +c.name + ": " + Arrays.toString(c.hand) +"\n");
        while(showdown)
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
//                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    private void betting()
    {
        betting = true;
        message("Betting has began\nPlease type /woot when done betting\n");
        while(!allBetsIn())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
//                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for(ServerClient c : clients)
            c.woot = false;
        betting = false;
    }
    
    private boolean allBetsIn()
    {
        for(ServerClient c : clients)
            if(!c.woot && c.socket.isConnected())
                return false;
        return true;
    }
    
    private void startGame()
    {
       gameInProgress = true;
       message("Game Started\n");
       for(ServerClient c : clients)
            c.sendCash(500);
    }
    
    public void addToPot(int n)
    {
        pot+=n;
    }
    
    boolean checkName(String name)
    {
        boolean found = false;
        for(ServerClient client : clients)
            if(name.equals(client.name))
                found = true;
        if(name.contains("Server") || name.contains("server"))
            found = true;
        return found;
    }
    
    public void findWinner(String name)
    {
        ServerClient winner = null;
        boolean found = false;
        for(ServerClient client : clients)
            if(name.equals(client.name))
            {
                winner = client;
                found = true;
            }
        if(found)
        {
            winner.sendCash(pot);
            message(name + " is the winner\n");
            showdown = false;
        }
        else
        {
            output.append(name + " not found");
        }
    }
    
    public void drill(String name)
    {
        message(name + " has drilled " + clients.get(rand.nextInt(clients.size())).name + " for " + rand.nextInt(1000) + " damage\n");
        message("/drill \n");
    }
    
    public void message(String s)
    {
        output.append(s);
        for(ServerClient c : clients)
            try {
                c.out.writeObject(s);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        String command = e.getActionCommand();
        if(!command.equals(""))
        {
            if(command.charAt(0) == '/')
            {
                if(command.startsWith("/stop")) //stops server
                {
//                    stop();
                }
                else if(command.startsWith("/start")) //starts game
                    startGame();
                
                else if(command.startsWith("/kick")) //kicks player
                {
                    
                }
                else if(command.startsWith("/endGame")) //ends game returns to lobby
                {
                    
                }
                else if(command.startsWith("/printClients")) //prints connected clients
                    for(ServerClient c : clients)
                        output.append(c.toString()+"\n");
                else if(command.startsWith("/winner "))
                {
                    findWinner(command.substring(8));
                }
                
            }
            else
            {
                message("<Server>: " + command + "\n");
                
            }
            cmdLine.setText("");
        }
    }
}
