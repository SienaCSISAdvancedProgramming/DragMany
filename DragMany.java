
// going to be lazy about imports in these examples...
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * A program to demonstrate a dragging operation on any one of many
 * objects. Could be circles and squares, filled or outlined, and
 * come in various sizes, and colors.
 * 
 * @author Jim Teresco
 * @version Spring 2022
 */

public class DragMany extends MouseAdapter implements Runnable {

	// some named constants
	public static final int MIN_SIZE = 25;
	public static final int MAX_SIZE = 100;
	public static final int PANEL_SIZE = 600;

	// our list of shapes
	// note: normally we would declare as List, but with the "lazy"
	// imports above, we are importing two things that are "List"s
	private java.util.List<DraggableShape> shapes;

	// instead of a boolean to remember if we are dragging, we have
	// a variable that says where the mouse last was so we can move
	// the shape relative to that position, and this will be null
	// if the mouse is dragging but was not pressed on a shape
	private Point lastMouse;

	// the shape being dragged
	private DraggableShape dragging;

	// number of shapes we'll be creating
	private int count;

	private JPanel panel;

	/**
	 * A constructor so we can receive the number of shapes that
	 * was passed on the command line, and add them to our list
	 * 
	 * @param count number of shapes to create
	 */
	public DragMany(int count) {

		this.count = count;
	}

	/**
	 * The run method to set up the graphical user interface
	 */
	@Override
	public void run() {

		// set up the GUI "look and feel" which should match
		// the OS on which we are running
		JFrame.setDefaultLookAndFeelDecorated(true);

		// create a JFrame in which we will build our very
		// tiny GUI, and give the window a name
		JFrame frame = new JFrame("DragMany");
		frame.setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));

		// tell the JFrame that when someone closes the
		// window, the application should terminate
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// JPanel with a paintComponent method
		panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {

				// first, we should call the paintComponent method we are
				// overriding in JPanel
				super.paintComponent(g);

				// for each shape we have, paint it on the Graphics object
				for (DraggableShape s : shapes) {
					s.paint(g);
				}

			}
		};
		frame.add(panel);
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);

		// construct our list of shapes
		shapes = new ArrayList<DraggableShape>(count);

		Random r = new Random();

		for (int i = 0; i < count; i++) {
			int size = MIN_SIZE + r.nextInt(MAX_SIZE - MIN_SIZE);
			Point point = new Point(r.nextInt(PANEL_SIZE - size),
					r.nextInt(PANEL_SIZE - size));
			Color color = new Color(r.nextInt(255),
					r.nextInt(255),
					r.nextInt(255));
			shapes.add(new DraggableShape(r.nextInt(2),
					r.nextBoolean(),
					size, point, color));
		}

		// display the window we've created
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void mousePressed(MouseEvent e) {

		// make sure we don't start dragging if we didn't press on a shape
		lastMouse = null;

		Point p = e.getPoint();
		// if we pressed within an object in our list, set up for dragging
		// note the reverse loop so we first encounter the object drawn on
		// top in the case of any overlap
		for (int i = shapes.size() - 1; i >= 0; i--) {
			if (shapes.get(i).contains(p)) {
				dragging = shapes.get(i);
				lastMouse = p;
				// also move to the end of the list so this one's drawn on top
				shapes.remove(i);
				shapes.add(dragging);
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		// if we are dragging an object, update its position by the
		// amount the mouse has moved since the last press or drag
		// event
		if (lastMouse != null) {
			int dx = e.getPoint().x - lastMouse.x;
			int dy = e.getPoint().y - lastMouse.y;
			dragging.translate(dx, dy);
			panel.repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		// if we are dragging an object, update its position by the
		// amount the mouse has moved since the last press or drag
		// event
		if (lastMouse != null) {
			int dx = e.getPoint().x - lastMouse.x;
			int dy = e.getPoint().y - lastMouse.y;
			dragging.translate(dx, dy);
			lastMouse = e.getPoint();
			panel.repaint();
		}
	}

	/**
	 * Main method to launch our application.
	 * 
	 * @param args[0] the number of shapes to draw
	 */
	public static void main(String args[]) {

		// Nothing stops us from taking command-line parameters
		if (args.length != 1) {
			System.err.println("Usage: java DragMany count");
			System.exit(1);
		}

		int count = 0;
		try {
			count = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Could not parse " + args[0] + " as integer.");
			System.exit(1);
		}

		// and we can pass things to our constructor
		javax.swing.SwingUtilities.invokeLater(new DragMany(count));
	}
}

/**
 * A class to encapsulate information about a shape on the screen.
 */
class DraggableShape {

	// some named constants to define shapes
	public static final int CIRCLE = 0;
	public static final int SQUARE = 1;

	// shape info
	private int shape;
	private boolean isFilled;
	private int size;
	private Point upperLeft;
	private Color color;

	public DraggableShape(int shape, boolean isFilled, int size,
			Point upperLeft, Color color) {

		// some bounds checking on these would probably be nice
		this.shape = shape;
		this.isFilled = isFilled;
		this.size = size;
		this.color = color;

		// since Point is mutable, we want to avoid side-effects that
		// could result from changes to the Point object, so we
		// make a new one here that we know will not be modified
		// by anyone else
		this.upperLeft = new Point(upperLeft);
	}

	/**
	 * paint this object onto the given Graphics area
	 * 
	 * @param g the Graphics object where the shape should be drawn
	 */
	public void paint(Graphics g) {

		g.setColor(color);
		if (shape == CIRCLE) {
			if (isFilled) {
				g.fillOval(upperLeft.x, upperLeft.y, size, size);
			} else {
				g.drawOval(upperLeft.x, upperLeft.y, size, size);
			}
		} else {
			if (isFilled) {
				g.fillRect(upperLeft.x, upperLeft.y, size, size);
			} else {
				g.drawRect(upperLeft.x, upperLeft.y, size, size);
			}
		}
	}

	/**
	 * A relative move of this object.
	 * 
	 * @param dx amount to translate in x
	 * @param dy amount to translate in y
	 */
	public void translate(int dx, int dy) {

		upperLeft.translate(dx, dy);
	}

	/**
	 * Determine if the given point is within this shape.
	 * 
	 * @param p Point to check
	 */
	public boolean contains(Point p) {

		if (shape == CIRCLE) {
			Point circleCenter = new Point(upperLeft.x + size / 2, upperLeft.y + size / 2);
			return circleCenter.distance(p) <= size / 2;

		} else {
			return p.x >= upperLeft.x && p.x <= upperLeft.x + size &&
					p.y >= upperLeft.y && p.y <= upperLeft.y + size;
		}
	}
}
