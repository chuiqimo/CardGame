
public interface Constant {
	int PORT = 1181;
	int CARDS = 6;//how many cards
	
	int ANSWER = 0;//Server -> Client: give the face of Card
	int PAIR = 1;//Server -> Client: tell the Client tow Cards make a pair
	//int WAIT = 2;//Server -> Client: let the thread sleep for a while
	int PLAYING = 3;//Server -> Client: decide which player is going to play
	int WINNER = 4;//Server -> Client: decide which player is the winner
	int REVERSE = 5;//Server -> Client: flap Cards if there is not a pair
	int PLAYER = 6;//Server -> Client: set the player's number
	int ASK = 7;//Client -> Server: ask for the face of Card
	int QUIT = 8;//Client -> Server: want to quit
	int DONE = 9;//Server -> Client: quit
	int START = 10;//Client -> Server: want to start the game and get a player number
	int CLOSE = 11;//Client -> Server: click two cards and cannot click anymore
	int WHOWIN = 12;//Client -> Server: ask who wins
	int WHOPLAY = 13;//Client -> Server: ask whose turn playing
	
	int NONE = -1;//Server -> Client unsuccessful
	String[] CMD = {"ANSWER","PAIR","WAIT","PLAYING","WINNER","REVERSE","PLAYER","ASK","QUIT","DONE"};
	
}
