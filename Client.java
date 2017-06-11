import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
/**
 * Create a client frame and a server frame to play a 2X2 CardGame
 * @author songyihang
 *
 */
public class Client extends JFrame implements Constant, Runnable
{
	
	private static final int FRAME_WIDTH = 800;
	private static final int FRAME_HEIGHT = 600;
	
	private JFrame frameClient;
	private JPanel panel;
	private JPanel subPanel1;
	private JPanel subPanel2;
	private JPanel subPanel3;
	private PlayerCard button1;
	private PlayerCard button2;
	private PlayerCard button3;
	private PlayerCard button4;
	private PlayerCard button5;
	private PlayerCard button6;
	private JButton quit;
	private JLabel comment;
	private JLabel info;
	private JLabel pair;
	private PlayerCard[] cards;
	private int playerNum;
	private int pairNum;
	private JLabel msg;
	private ActionListener listener;
	private int count;
	private JButton start;
	
	private static final int M = CARDS/2;
	
	private Socket socket;
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	private boolean[] click;
	
	public static void main(String[] args)
	{
		System.out.println("Welcome! This is a 2X2 Card Game! Please wait for server gives you a number...");
		String host = "localhost";
		if(args.length == 1)
			host = args[0];
		new Client(host);
	}
	/**
	 * Create a constructor
	 */
	public Client(String host)
	{
		//pairNum = 0;
		createGUI();
		if(!connectedToServer(host))
			freeze();
		else
		{
			Thread t = new Thread(this);
			t.start();
		}
		
	}
	/**
	 * Create a client frame
	 */
	public void createGUI()
	{
		frameClient = new JFrame();
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		subPanel1 = new JPanel();
		subPanel1.setLayout(new GridLayout(2,1));
		subPanel2 = new JPanel();
		subPanel3 = new JPanel();
		JPanel mini1 = new JPanel();
		JPanel mini2 = new JPanel();
		
		button1 = new PlayerCard("0");
		button2 = new PlayerCard("1");
		button3 = new PlayerCard("2");
		button4 = new PlayerCard("3");
		button5 = new PlayerCard("4");
		button6 = new PlayerCard("5");
		listener = new ClickListener();
		button1.addActionListener(listener);
		button2.addActionListener(listener);
		button3.addActionListener(listener);
		button4.addActionListener(listener);
		button5.addActionListener(listener);
		button6.addActionListener(listener);
		
		//buttons are unclickable unless the server allows player to click
		button1.setEnabled(false);
		button2.setEnabled(false);
		button3.setEnabled(false);
		button4.setEnabled(false);
		button5.setEnabled(false);
		button6.setEnabled(false);
		
		cards = new PlayerCard[CARDS];
		cards[0] = button1;
		cards[1] = button2;
		cards[2] = button3;
		cards[3] = button4;
		cards[4] = button5;
		cards[5] = button6;
		
		//show the back of Cards to player
		button1.coverCard();
		button2.coverCard();
		button3.coverCard();
		button4.coverCard();
		button5.coverCard();
		button6.coverCard();
		
		for(int i = 0; i < M;i++)
		{
			mini1.add(cards[i]);
		}
		for(int i = M;i < cards.length;i++)
		{
			mini2.add(cards[i]);
		}
		
		subPanel1.add(mini1);
		subPanel1.add(mini2);
		panel.add(subPanel1,BorderLayout.CENTER);
		
		quit = new JButton("EXIT");
		start = new JButton("Start Game");
		quit.setName("EXIT");
		start.setName("START");
		quit.addActionListener(listener);
		//start = new JButton("START A NEW GAME");
		//start.setName("NEW");
		start.addActionListener(listener);
		comment = new JLabel();
		subPanel3.setLayout(new GridLayout(1,3));
		subPanel3.add(start);
		subPanel3.add(comment);
		subPanel3.add(quit);
		panel.add(subPanel3, BorderLayout.SOUTH);
		
		info = new JLabel("This is a 3X3 Card Game");
		pair = new JLabel("Pairs: "+ pairNum);
		msg = new JLabel();
		subPanel2.setLayout(new GridLayout(1,3));
		subPanel2.add(info);
		subPanel2.add(msg);
		subPanel2.add(pair);
		panel.add(subPanel2, BorderLayout.NORTH);
		
		frameClient.add(panel);
		
		frameClient.setTitle("Client");
		frameClient.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frameClient.setVisible(true);
		frameClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	/**
	 * ActionListener that catches which button player clicks
	 * @author songyihang
	 *
	 */
	class ClickListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				JButton c = (JButton)e.getSource();
				if(c.getName().equals("EXIT"))
				{
					toServer.writeInt(QUIT);
					toServer.flush();
				}
				if(!c.getName().equals("EXIT") && !c.getName().equals("START"))
				{
					toServer.writeInt(ASK);
					toServer.writeInt(Integer.parseInt(c.getName()));
					toServer.flush();
				}
			}
			catch(IOException exception)
			{
				exception.printStackTrace();
			}
		}
	}
	public void run()
	{
		boolean done = false;
		try
		{
			while(!done)
			{
				int get = fromServer.readInt();
				switch(get)
				{
				case ANSWER: int cValue = fromServer.readInt();
				             int c = fromServer.readInt();
				             cards[c].setText(cValue+"");
				             cards[c].setEnabled(false);
				             count++;
					break;
				/**case WAIT: Thread.sleep(10);
				           toServer.writeInt(WHOPLAY);
				    break;
				    */
				case PAIR: int a = fromServer.readInt();
				           int b = fromServer.readInt();
				           int p = fromServer.readInt();
				           click[a] = true;
				           click[b] = true;
				           cards[a].setEnabled(false);
				           cards[b].setEnabled(false);
				           if(p == playerNum)
				           {
					           pairNum++;
					           pair.setText("Pairs: "+pairNum);
					           msg.setText("You get a pair!");
				           }
				           else
				           {
				        	   msg.setText("The other player gets a pair!");
				           }
				           closeCom();
				           Thread.sleep(1000);
				           toServer.flush();
					break;
				case REVERSE: int c1 = fromServer.readInt();
				              int c2 = fromServer.readInt();
				              Thread.sleep(1000);
				              cards[c1].coverCard();
				              cards[c2].coverCard();
				              closeCom();
				              toServer.flush();
					break;
				case WINNER: int num = fromServer.readInt();
						         msg.setText("Player "+num+" wins the game!");
					             if(num == playerNum)
					            	 info.setText("You win!!!!");
					             done = true;
				             
					break;
				case PLAYING:count = 0;
				             int m = fromServer.readInt();
				             if(m == playerNum)
				            	 able();
					break;
				case PLAYER:pairNum = 0;
				            click = new boolean[CARDS];
				            playerNum = fromServer.readInt();
				            info.setText("Player "+playerNum);
					break;
				case DONE:int player = fromServer.readInt();
				          if(player == playerNum)
				          {
				        	  closeCom();
				        	  done = true;
				          }else
				          {
				        	  info.setText("You win!!!!");
				        	  closeCom();
				        	  done = true;
				          }
					     
					break;
				}
				repaint();
				
			}
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
		}
		catch(InterruptedException exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			try
			{
				closeCom();
				socket.close();
			}
			catch(IOException exception)
			{
				exception.printStackTrace();
			}
		}
	}
	public boolean connectedToServer(String host)
	{
		boolean connected = true;
		try
		{
			socket = new Socket(host, PORT);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch(IOException exception)
		{
			connected = false;
			System.out.println("IO");
		}
			return connected;
		
		
	}
	public void freeze()
	{
		System.out.println("Cannot connect!");
	}
	public void ansCom(int c1Value, int c2Value, int c1, int c2)
	{
		cards[c1].setText(c1Value+"");
		cards[c2].setText(c2Value+"");
		cards[c1].setEnabled(false);
		cards[c2].setEnabled(false);
	}
	public void closeCom()
	{
  	   for(int i = 0; i < cards.length;i++)
  	   {
  		   cards[i].setEnabled(false);
  	   }
	}
	public void able()
	{
		for(int i = 0; i < cards.length; i++)
		{
			if(click[i] == false)
				cards[i].setEnabled(true);
		}
	}
}
