import javax.swing.*;
import java.awt.*;
import java.io.IOException;
/**
 * Create a button with specific size and name
 * @author songyihang
 *
 */
public class PlayerCard extends JButton
{
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 200;
	/**
	 * Create a PlayerCard with no argument
	 */
	public PlayerCard()
	{
		//set size of button
		setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		
	}
	/**
	 * Create a PlayerCard with a name
	 * @param s
	 */
	public PlayerCard(String s)
	{
		setName(s);//set name of the button
		setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
	}
	/**
	 * Change the text on the button
	 */
	public void coverCard()
	{
		ImageIcon image = new ImageIcon(this.getClass().getResource("back.jpg"));
		setIcon(image);
		
	}
	/**
	 * Get the text on the button
	 */
	public String toString()
	{
		return getText();
	}
}
