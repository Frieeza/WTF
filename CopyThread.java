import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyThread extends Thread {

	static File sourceFolder = new File("C:\\Users\\Mati\\Desktop\\P1\\Test"); // <- Sciezka folderu z ktorego beda kopiowane pliki
	static File destFolder = new File("C:\\Users\\Mati\\Desktop\\P1\\TestCopy"); // <- Sciezka folderu do ktorego beda kopiowane pliki
	static String[] fileTable = sourceFolder.list();
	private FileCounter counter;
	Counter counter2= new Counter();

	public CopyThread(FileCounter fileCounter) {
		this.counter = fileCounter;
		start();
	}

	public void run() { // funkcja odpowiedzialna za dzialanie watku
		int fileCounter = 0;
		fileCounter = counter.fileCounter();
		try {
			copyFunction(sourceFolder, destFolder, fileCounter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void checkFolders() {
		if (!sourceFolder.exists()) {
			System.out.print(
					"Incorrect source path ");
			System.exit(0);
		}
		if (!destFolder.exists()) {
			destFolder.mkdir();
			System.out.print("Destination folder created");
		}
	}

	public void copyFunction(File sourceFolder, File destFolder, int fileCounter) throws IOException { 
		File src = new File(sourceFolder.getPath(), fileTable[fileCounter - 1]);
		File dest = new File(destFolder.getPath(), fileTable[fileCounter - 1]);
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);
		byte[] buffer = new byte[1024]; // <- Ustawienie wielkosci bufora
		long size = src.length();
		long sizeCounter = 0;
		int lngth = 0;
		while ((lngth = in.read(buffer)) > 0) { // petla odpowiedzialna za kopiowanie przez bufor, wraz z licznikiem % skopiowania pliku
			sizeCounter += lngth;
			System.out.print((100 * sizeCounter / size) + "%\n");
			Gui.gui((int) (100 * sizeCounter / size), counter2.threadCounter());
			out.write(buffer, 0, lngth);
		}
		in.close();
		out.close();
		System.out.println("Plik skopiowany z " + src + " do " + dest);
	}
}
