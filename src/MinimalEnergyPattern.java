public class MinimalEnergyPattern implements Comparable<MinimalEnergyPattern> {

    public int[] neurons;
    public int num_ones = 0;

    public MinimalEnergyPattern(int[] neurons) {
        this.neurons = new int[neurons.length];
        for (int i = 0; i < neurons.length; ++i) {
            this.neurons[i] = neurons[i];
            num_ones += (1 + neurons[i]) / 2;
        }
    }

    public int compareTo(MinimalEnergyPattern minimalEnergyPattern) {
        return this.num_ones - minimalEnergyPattern.num_ones;
    }
}
