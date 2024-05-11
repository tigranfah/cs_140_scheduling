import java.util.ArrayList;

public class Autoassociator {
	private int weights[][];
	private int trainingCapacity;
	
	public Autoassociator(int num_neurons) {
		weights = new int[num_neurons][num_neurons];
		for (int i = 0; i < weights.length; ++i)
			weights[i][i] = 0;
	}
	
	public int getTrainingCapacity() {
		// TO DO
		
		return 0;
	}
	
	public void fit(ArrayList<MinimalEnergyPattern> training_patterns) {
		System.out.println("Fitting a model with " + training_patterns.size() + " examples.");
		for (int i = 0; i < weights.length; ++i) {
			for (int j = 0; j < weights[i].length; ++j) {
				if (i == j) continue;
				for (MinimalEnergyPattern pattern : training_patterns) {
					weights[i][j] += pattern.neurons[i] * pattern.neurons[j];
				}
			}
		}
		System.out.println("Finished fitting.");
	}

	private int[] heaviside_function(int[] neurons) {
		int[] new_neurons = new int[neurons.length];
		for (int i = 0; i < neurons.length; ++i) {
			new_neurons[i] = heaviside_function(neurons[i]);
		}
		return new_neurons;
	}

	private int heaviside_function(int value) {
		if (value == 0)
			return 1;
		return value / Math.abs(value);
	}

	private int dot_prod(int[][] w, int[] neurons, int index) {
		int value = 0;
		for (int i = 0; i < neurons.length; ++i) {
//			for (int j = 0; j < neurons.length; ++j)
			value += w[index][i] * neurons[i];
		}
		return value;
	}

	public int unit_optimize(int neurons[], int index) {
		neurons[index] = heaviside_function(dot_prod(weights, neurons, index));
		return neurons[index];
	}
	
	public void unitUpdate(int neurons[], int index) {
		// TO DO
		// implements the update step of a single neuron specified by index
	}
	
	public void chainUpdate(int neurons[], int steps) {
		// TO DO
		// implements the specified number od update steps
	}
	
	public void fullUpdate(int neurons[]) {
		// TO DO
		// updates the input until the final state achieved
	}
}
