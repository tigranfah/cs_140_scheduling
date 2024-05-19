import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import javax.swing.*;
import java.time.LocalDate;

public class TimeTable extends JFrame implements ActionListener {

	private JPanel screen = new JPanel(), tools = new JPanel();
	private JButton tool[];
	private JTextField field[];
	private CourseArray courses;
	private Color CRScolor[] = {Color.RED, Color.GREEN, Color.BLACK};
	private Autoassociator associator;
	private int min_clashes = Integer.MAX_VALUE;
	private int current_step = 0, best_step = 0;
	private int num_iterations = 0, num_shifts = 0, num_slots = 0;
	private ArrayList<MinimalEnergyPattern> training_patterns = new ArrayList<>();
	
	public TimeTable() {
		super("Dynamic Time Table");
		setSize(650, 800);
		setLayout(new FlowLayout());

		screen.setPreferredSize(new Dimension(400, 800));
		add(screen);

		setTools();
		add(tools);

		setVisible(true);
//		test_hopfield_network();
	}
	
	public void setTools() {
		String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
		field = new JTextField[capField.length];
		
		String capButton[] = {"Load", "Reset", "Step", "Continue", "Collect train data", "Train", "Step (hopfield)", "Continue (hopfield)", "Save train data", "Print", "Exit"};
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
		
//		field[0].setText("17");
//		field[1].setText("381");
//		field[2].setText("res/lse-f-91.stu");
//		field[3].setText("200");
//		field[4].setText("19");

		field[0].setText("20");
		field[1].setText("261");
		field[2].setText("res/tre-s-92.stu");
		field[3].setText("200");
		field[4].setText("19");
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

	private void save_train_data() {
		training_patterns.sort(Collections.reverseOrder());
		int v = 0;
		String ds_name = "res/data/dataset";
		while (new File(ds_name + "-v" + v + ".txt").exists())
			v++;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(ds_name + "-v" + v + ".txt");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < training_patterns.size(); ++i) {
			System.out.println(training_patterns.get(i).num_ones);
			for (int j = 0; j < training_patterns.get(i).neurons.length; ++j)
				writer.print(training_patterns.get(i).neurons[j] + " ");
			writer.println();
		}
		writer.close();
	}

	private ArrayList<MinimalEnergyPattern> collect_train_data(int num_iter, int num_shifts) {
		reset();
		perform_iterations(num_iter, num_shifts);

		float mean_number_of_ones = 0;
		for (int slot = 1; slot <= num_slots; ++slot) {
			if (courses.is_clash_free(slot)) {
				int[] class_schedule = courses.getTimeSlot(slot);
				int num_of_ones = 0;
				for (int i = 0; i < class_schedule.length; ++i)
					num_of_ones += (class_schedule[i] + 1) / 2;
				mean_number_of_ones += num_of_ones;
			}
		}
		mean_number_of_ones /= num_slots;
		System.out.println("Mean number of exams in a slot " + mean_number_of_ones);

		ArrayList<MinimalEnergyPattern> energy_min_patterns = new ArrayList<>();
		for (int slot = 1; slot <= num_slots; ++slot) {
			if (courses.is_clash_free(slot)) {
				int[] class_schedule = courses.getTimeSlot(slot);
				int num_of_ones = 0;
				for (int i = 0; i < class_schedule.length; ++i)
					num_of_ones += (class_schedule[i] + 1) / 2;
				System.out.println("num of scheduled exams in slot " + slot + ": " + num_of_ones);
				if (num_of_ones >= mean_number_of_ones)
					energy_min_patterns.add(new MinimalEnergyPattern(class_schedule));
			}
		}
		return energy_min_patterns;
	}

	public void reset() {
		this.min_clashes = Integer.MAX_VALUE;
		this.best_step = 0;
		this.current_step = 0;
		for (int i = 1; i < courses.length(); i++)
			courses.setSlot(i, 0);
		draw();
		setVisible(true);
	}

	private void perform_iterations(int num_iter, int num_shifts) {
		if (num_iter > 0) {
			for (int iteration = 1; iteration <= num_iter; iteration++) {
				courses.iterate(num_shifts);
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

	private void perform_iteration_hopfield(int num_iter) {
		if (num_iter <= 0) return;
		Random rand = new Random();
		for (int iteration = 0; iteration < num_iter; ++iteration) {
			for (int slot = 0; slot < num_slots; ++slot) {
				int random_index = rand.nextInt(courses.length() - 1);
				int optimized_value = associator.unit_optimize(courses.getTimeSlot(slot), random_index);
				if (optimized_value == 1) {
					courses.elements[random_index + 1].mySlot = slot;
				} else if (optimized_value == -1) {
					courses.elements[random_index + 1].mySlot = (slot + 1) % num_slots;
				} else {
					System.out.println("WARNING: optimized value cannot be " + optimized_value);
				}
				draw();
				int clashes = courses.clashesLeft();
				current_step++;
				if (clashes < this.min_clashes) {
					this.min_clashes = clashes;
					this.best_step = current_step;
				}
			}
		}
		System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + this.min_clashes + "\tat step " + this.best_step);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent click) {
		num_iterations = Integer.parseInt(field[3].getText());
		num_slots = Integer.parseInt(field[0].getText());
		num_shifts = Integer.parseInt(field[4].getText());

		switch (getButtonIndex((JButton) click.getSource())) {
		case 0:
			System.out.println("<<Load>> button pressed");
			System.out.println("Loading the file " + field[2].getText() + ", " + field[1].getText() + " cources.");
			courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, num_slots);
			courses.readClashes(field[2].getText());
			draw();
			associator = new Autoassociator(Integer.parseInt(field[1].getText()));
			break;
		case 1:
			System.out.println("<<Reset>> button pressed");
			reset();
			break;
		case 2:
			System.out.println("<<Step>> button pressed, num_slots: " + num_slots + ", num_iter: " + num_iterations + ", num_shifts: " + num_shifts);
			perform_iterations(1, this.num_shifts);
			break;
		case 3:
			System.out.println("<<Continue>> button pressed, num_slots: " + num_slots + ", num_iter: " + num_iterations + ", num_shifts: " + num_shifts);
			perform_iterations(this.num_iterations, this.num_shifts);
			break;
		case 4:
			System.out.println("<<Collect train data>> button pressed");
			int best_iteration_found = this.best_step;
			reset();
			perform_iterations(best_iteration_found, this.num_shifts);
			ArrayList<MinimalEnergyPattern> new_samples = this.collect_train_data(best_iteration_found, this.num_shifts);
			training_patterns.addAll(new_samples);
			if (training_patterns.size() >= 0.139 * courses.length()) {
				System.out.println("NOTE: Number of patterns " + training_patterns.size() + " are more than 0.139 * " + courses.length() + " = " + 0.139 * courses.length());
			}
			System.out.println(new_samples.size() + " training samples collected");
			break;
		case 5:
			System.out.println("<<Train>> button pressed");
			associator.fit(training_patterns);
			break;
		case 6:
			System.out.println("<<Step (hopfield)>> button pressed, num_slots: " + num_slots + ", num_iter: " + num_iterations + ", num_shifts: " + num_shifts);
			perform_iteration_hopfield(1);
			break;
		case 7:
			System.out.println("<<Continue (hopfield)>> button pressed, num_slots: " + num_slots + ", num_iter: " + num_iterations + ", num_shifts: " + num_shifts);
			perform_iteration_hopfield(num_iterations);
			break;
		case 8:
			System.out.println("<<Print>> button pressed");
			save_train_data();
			break;
		case 9:
			System.out.println("<<Exit>> button pressed");
			System.out.println("Exam\tSlot\tClashes");
			for (int i = 1; i < courses.length(); i++)
				System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
			break;
		case 10:
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		LocalDate currentDate = LocalDate.now();
		System.out.println("Date: " + currentDate);
		String git_commit_hash = get_git_commit_hash();
		System.out.println("Author: Tigran Fahradyan");
		System.out.println("Git commit hash: " + git_commit_hash);
		System.out.println("Git commit is specified to indicate the code (logic) used for the results obtained in this log file, and to ensure reproducability\n");
		new TimeTable();
	}

	private static String get_git_commit_hash() {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("git", "rev-parse", "HEAD");
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}

			int exitCode = process.waitFor();
			if (exitCode == 0) {
				return output.toString();
			} else {
				System.err.println("Error while getting git hash. Exit code: " + exitCode);
				return "";
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void test_hopfield_network() {
		Autoassociator associator = new Autoassociator(36);
		ArrayList<MinimalEnergyPattern> patterns = new ArrayList<>();
		int[] pat = new int[] {1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		patterns.add(new MinimalEnergyPattern(pat));
		pat = new int[] {-1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		patterns.add(new MinimalEnergyPattern(pat));
		pat = new int[] {-1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		patterns.add(new MinimalEnergyPattern(pat));
		pat = new int[] {-1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		patterns.add(new MinimalEnergyPattern(pat));
		pat = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		patterns.add(new MinimalEnergyPattern(pat));

		associator.fit(patterns);

		pat = new int[] {1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		associator.unit_optimize(pat, 0);
	}
}
