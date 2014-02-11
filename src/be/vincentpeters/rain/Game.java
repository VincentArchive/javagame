package be.vincentpeters.rain;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;
import be.vincentpeters.rain.graphics.Screen;

public class Game extends Canvas implements Runnable
{
	// dunno this, something convention, is optional
	private static final long serialVersionUID = 1L;
	public static int width = 300;
	//calculate height to be 16/9 aspect ratio
	public static int height = width / 16 * 9;
	public static int scale = 3;

	private Thread thread;
	private boolean running = false;
	// include our screen class
	private Screen screen;

	// class that creates an window
	private JFrame frame;
	// Buffered Image is an image with an accessible buffer
	private BufferedImage image = new BufferedImage( width, height,
			BufferedImage.TYPE_INT_RGB );
	// dunno how, but this converts the the buffered image to an array of 
	// Integers for each pixels on the screen, so make it modifiable
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
			.getData();

	/**
	 * Game constructor
	 */
	public Game()
	{
		Dimension size = new Dimension( width * scale, height * scale );
		setPreferredSize( size );
		//Instantiate a new instance of our screen class
		screen = new Screen( width, height );
		//create e new window
		frame = new JFrame();
	}

	/**
	 * Start the game
	 */
	public synchronized void start()
	{
		running = true;
		// creating a new thread from itself (this) will cause to start the run 
		// method because it has the runnable parameter
		thread = new Thread( this, "Display" );
		thread.start();
	}

	/**
	 * Stop the game
	 */
	public synchronized void stop()
	{
		running = false;
		//try to stop thread, by joining both to single thread
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The game loop 
	 * is going to automatically run this because this class has parameter runnable
	 */
	public void run()
	{
		// get the system time in nanoseconds
		long lastTime = System.nanoTime();
		// final means it wont be changed
		// Nanoseconds conversion divided by the max frames per second you want
		// 1000000000 equals 1 second in nanoseconds
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		while (running) {
			long now = System.nanoTime();
			// get the difference between two nanoseconds
			delta += (now-lastTime) / ns;
			//when delta exceeds 1, do a logic tick
			// this will only be done 60 times per second
			while(delta >= 1)
			{
				tick(); // game logic loop - fixed in time
				delta --;
			}
			
			render(); //render loop - unlimited
		}
	}

	public void tick()
	{
		// game logic
	}

	public void render()
	{
		//get current buffer strategy to an variable
		//this is already in place because we extend canvas
		BufferStrategy bs = getBufferStrategy();
		//check if buffer strategy variable is already created, of not create it.
		// this is so the variable isen't created every time screen is rendered
		if (bs == null) {
			//set it to triple buffering
			// one screen visible to the user
			// and two back buffers to generate the upcoming frames too
			createBufferStrategy( 3 );
			return;
		}
		
		//first clear the array of pixels
		screen.clear();
		// then re-populate the array of pixels
		screen.render();
		// duplicate the array of pixels to the buffered image objects pixel array
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = screen.pixels[i];
		}

		// create graphics context for the buffer
		Graphics g = bs.getDrawGraphics();
		//set rectangle color to black
		//g.setColor( Color.BLACK );
		//create a rectangle full size of canvas
		//g.fillRect( 0, 0, getWidth(), getHeight() );

		g.drawImage( image, 0, 0, getWidth(), getHeight(), null );

		// dispose Graphics, else will be created every frame per second and explode
		g.dispose();
		// buffer swapping - show next available buffer and clear the last from memory
		bs.show();
	}

	/**
	 * Program entry point.
	 * this is executed first on runtime.
	 * @param args
	 */
	public static void main( String[] args )
	{
		Game game = new Game();
		game.frame.setResizable( false );
		game.frame.setTitle( "Rain" );
		game.frame.add( game );
		//sets the size of the frame ( done in constructor with preferred size )
		game.frame.pack();
		//close the window on clicking X
		game.frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		//center window in center of screen
		game.frame.setLocationRelativeTo( null );
		//show the window
		game.frame.setVisible( true );

		game.start();
	}
}
