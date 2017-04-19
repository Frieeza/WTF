public class Counter {
	private int fileCounter;
	private int threadCounter;

	public int fileCounter() {
		fileCounter += 1;
		return fileCounter;
	}

	public int threadCounter() {
		threadCounter += 1;
		if (threadCounter == 5) { // <- ustawic na maxymalnosc wartosc watkow +1
			threadCounter = 1;
		}
		return threadCounter;
	}
}
