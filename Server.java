import javax.swing.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
public class Server extends JFrame implements Constant
{
	private static final int SERVER_WIDTH = 500;
	private static final int SERVER_HEIGHT = 250;
	private static final int AREA_ROWS = 10;
	private static final int AREA_COLUMNS = 30;
	private static final int MAX_PLAYER = 2;
	
	private JTextArea area;
	private JScrollPane scroll;
	private Socket socket;
	private ServerSocket serverSocket;
	private int[] cards = {1,1,2,2,3,3};
	private int clientNum;
	private int[] pairs;
	//private int[] pairsShadow;
	//private int thisNum;
	//private int[] clients;
	
	public static void main(String[] args)
	{
		new Server();
	}
	public Server()
	{
		createGUI();
		createCard();
		clientNum = 0;
		pairs = new int[MAX_PLAYER];
		
		//int numPlayers = 2;
		int gameNumber = 1;
		try
		{
			serverSocket = new ServerSocket(PORT);
			area.append("Server start at"+LocalDateTime.now()+"\n");
			
			while(true)
			{
				// make a single game with two players
				Socket socketA = serverSocket.accept();
				Socket socketB = serverSocket.accept();
				
				clientNum++;
				InetAddress address = socketA.getInetAddress();
				area.append("Client "+clientNum+" with IP address "+address.getHostAddress()+" is connected"+"\n");
				area.append("Wait for the second person connecting..."+"\n");
				address  = socketB.getInetAddress();
				clientNum++;
				area.append("Client "+clientNum+" with IP address "+address.getHostAddress()+" is connected"+"\n");
				GameService gameService = new GameService(socketA, socketB);
				
				new Thread(gameService).start();
				gameNumber++;
			}
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
		}
	}
	public void createGUI()
	{
		area = new JTextArea(AREA_ROWS, AREA_COLUMNS);
		area.setText("Hello" + "\n");
		area.setEditable(false);
		scroll = new JScrollPane(area);
		JPanel panel = new JPanel();
		
	    panel.add(scroll);
		add(panel);
		
		setTitle("Server");
		setSize(SERVER_WIDTH,SERVER_HEIGHT);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public void createCard()
	{
		for(int i = CARDS-1; i > 0; i--)
		{
			Random r = new Random();
			int random = r.nextInt(i);
			int temp = cards[random];
			cards[random] = cards[i];
			cards[i]=temp;
		}
	}
	public int winnerCom()
	{
			if(pairs[0]+pairs[1] == CARDS/2)
			{
				if(pairs[0] > pairs[1])
					return 0;
				else
					return 1;
			}
			return -1;
	}
	class GameService implements Runnable
	{
		private Socket socketA;
		private Socket socketB;
		private DataInputStream[] fromClient;
		private DataOutputStream[] toClient;
		public GameService(Socket socket1, Socket socket2)
		{
			socketA = socket1;
			socketB = socket2;
		}
		public void run()
		{
			try
			{
				fromClient = new DataInputStream[MAX_PLAYER];
				toClient = new DataOutputStream[MAX_PLAYER];
				fromClient[0] = new DataInputStream(socketA.getInputStream());
				fromClient[1] = new DataInputStream(socketB.getInputStream());
				toClient[0] = new DataOutputStream(socketA.getOutputStream());
				toClient[1] = new DataOutputStream(socketB.getOutputStream());
				toClient[0].writeInt(PLAYER);
				toClient[0].writeInt(0);
				toClient[0].flush();
				toClient[1].writeInt(PLAYER);
				toClient[1].writeInt(1);
				toClient[1].flush();
				int m = 0;
				int[] twoCards = {-1,-1};
				toClient[m].writeInt(PLAYING);
				toClient[m].writeInt(m);
				toClient[m].flush();
				int count = 0;
				while(true)
				{
					int msg = fromClient[m].readInt();
					//int thisNum;
					
					switch(msg)
					{
					case ASK: int c = fromClient[m].readInt();
					          count++;
					          for(int i = 0; i < MAX_PLAYER; i++)
					          {
					        	  toClient[i].writeInt(ANSWER);
					        	  toClient[i].writeInt(cards[c]);
					        	  toClient[i].writeInt(c);
					        	  toClient[i].flush();
					          }
					          area.append("Player "+m+" is trying Card "+c+" with the value "+cards[c]+"\n");
					          if(twoCards[0] != -1)
					          {
					        	  twoCards[1] = c;
					              area.append("Player "+m+" : twoCards[1] = "+c+"\n");
					          }
					          if(twoCards[0] == -1)
					          {
					        	  twoCards[0] = c;
						          area.append("Player "+m+" : twoCards[0] = "+c+"\n");
					          }
					          if(count == 2)
					          {
					        	  int a = twoCards[0];
						          int b = twoCards[1];
						          if(cards[a] == cards[b])
						          {
						        	  for(int i = 0; i < MAX_PLAYER; i++)
						        	  {
							        	  toClient[i].writeInt(PAIR);
							        	  toClient[i].writeInt(a);
							        	  toClient[i].writeInt(b);
							        	  toClient[i].writeInt(m);
							        	  toClient[i].flush();
						        	  }
						        	  pairs[m]++;
						        	  twoCards[0]=-1;
						        	  twoCards[1]=-1;
						        	  area.append("Plyaer "+m+" : Card "+a+" and Card "+b+" make a pair."+"\n");
						          } 
						          else
						          {
						        	  for(int i = 0; i < MAX_PLAYER;i++)
						        	  {
							        	  toClient[i].writeInt(REVERSE);
							        	  toClient[i].writeInt(a);
							        	  toClient[i].writeInt(b);
							        	  toClient[i].flush();
						        	  }
						        	  twoCards[0]=-1;
						        	  twoCards[1]=-1;
						        	  area.append("Card "+a+" and Card "+b+" do not make a pair."+"\n");
						          }
					          }
						break;
					case QUIT: for(int i = 0; i < MAX_PLAYER;i++)
					           {
						          toClient[i].writeInt(DONE);
						          toClient[i].writeInt(m);
						          toClient[i].flush();
					           }
					           area.append("Player "+m+" is quit"+"\n");
						break;
					
					}
					if(winnerCom() != -1)
					{
						for(int i = 0; i < MAX_PLAYER; i++)
						{
							toClient[i].writeInt(WINNER);
							toClient[i].writeInt(m);
							toClient[i].flush();
						}
						area.append("The winner is Player "+winnerCom()+"\n");
					}
					if(count == 2)
					{
						int n = m;
						if(n == 0)
							m = 1;
						if(n == 1)
							m = 0;
						count = 0;
						toClient[m].writeInt(PLAYING);
						toClient[m].writeInt(m);
						toClient[m].flush();
					}
					
				}
			}
			catch(IOException exception)
			{
				exception.printStackTrace();
			}
			finally
			{
				try
				{
					serverSocket.close();
				}
				catch(IOException exception)
				{
					System.out.println("IOException2");
				}
			}
		}
	}
}
