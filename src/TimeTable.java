import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TimeTable extends JFrame implements ActionListener {

	private JPanel screen = new JPanel(), tools = new JPanel();
	private JButton tool[];
	private JTextField field[];
	private CourseArray courses;
	private Color CRScolor[] = {Color.RED, Color.GREEN, Color.BLACK};
	private int min_clashes = Integer.MAX_VALUE;
	private int current_step = 0, best_step = 0;
	
	public TimeTable() {
		super("Dynamic Time Table");
		setSize(550, 800);
		setLayout(new FlowLayout());
		
		screen.setPreferredSize(new Dimension(400, 800));
		add(screen);
		
		setTools();
		add(tools);
		
		setVisible(true);
	}
	
	public void setTools() {
		String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
		field = new JTextField[capField.length];
		
		String capButton[] = {"Load", "Start", "Step", "Continue", "Print", "Exit"};
		tool = new JButton[capButton.length];
		
		tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));
		
		for (int i = 0; i < field.length; i++) {
			tools.add(new JLabel(capField[i]));
			field[i] = new JTextField(5);
			tools.add(field[i]);
		}
		
		for (int i = 0; i < tool.length; i++) {
			tool[i] = new JButton(capButton[i]);
			tool[i].addActionListener(this);
			tools.add(tool[i]);
		}
		
		field[0].setText("17");
		field[1].setText("381");
		field[2].setText("res/lse-f-91.stu");
		field[3].setText("2");
		field[4].setText("1");
	}
	
	public void draw() {
		Graphics g = screen.getGraphics();
		int width = Integer.parseInt(field[0].getText()) * 10;
		for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) {
			g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
			g.drawLine(0, courseIndex, width, courseIndex);
			g.setColor(CRScolor[CRScolor.length - 1]);
			g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
		}
	}
	
	private int getButtonIndex(JButton source) {
		int result = 0;
		while (source != tool[result]) result++;
		return result;
	}

	public void actionPerformed(ActionEvent click) {
		int num_iterations = 0;

		switch (getButtonIndex((JButton) click.getSource())) {
		case 0:
			int slots = Integer.parseInt(field[0].getText());
			courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
			courses.readClashes(field[2].getText());
			draw();
			break;
		case 1:
			this.min_clashes = Integer.MAX_VALUE;
			this.best_step = 0;
			this.current_step = 0;
			for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);
			num_iterations = Integer.parseInt(field[3].getText());
			break;
		case 2:
			num_iterations = 1;
			break;
		case 3:
			num_iterations = Integer.parseInt(field[3].getText());
			break;
		case 4:
			System.out.println("Exam\tSlot\tClashes");
			for (int i = 1; i < courses.length(); i++)
				System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
			break;
		case 5:
			System.exit(0);
		}

		if (num_iterations > 0) {
			for (int iteration = 1; iteration <= num_iterations; iteration++) {
				courses.iterate(Integer.parseInt(field[4].getText()));
				draw();
				int clashes = courses.clashesLeft();
				current_step++;
				if (clashes < this.min_clashes) {
					this.min_clashes = clashes;
					this.best_step = current_step;
				}
			}
			System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + this.min_clashes + "\tat step " + this.best_step);
			setVisible(true);
		}
	}

	public static void main(String[] args) {
		new TimeTable();
	}
}
