
public class Starter {

	public static void main(String[] args) {
		FileCounter fileCounter = new FileCounter();
		int threadCounter = 0;
		Gui.makeGui();
		CopyThread[] copyThread = new CopyThread[4]; // <- okreslenie maxymalnej ilosc watki
		
		CopyThread.checkFolders();
		
		for(int i = 0; i < CopyThread.fileTable.length; i++) { // petla uruchamiajaca watki				
			if(threadCounter > 3 ) { // <- Zmienic na wartosc maxymalnej ilosci watkow -1
				for(int threadNumber = 0; threadNumber < 4; threadNumber++){ // <-^ petla czekajaca na zakonczenie dzialajch watkow
					try {
						copyThread[threadNumber].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				threadCounter = 0;
			}
			copyThread[threadCounter] = new CopyThread(fileCounter); // uruchomienie watku
			threadCounter++;
		}
	}
}
