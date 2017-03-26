package Copy;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

public class FileCopier extends JFrame implements ActionListener,
		PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	private JProgressBar progressCurrent;
	private JTextArea txtDetails;
	private JButton btnCopy;
	private CopyTask task;

	public FileCopier() {
		buildGUI();
	}

	private void buildGUI() { // no dobra to latwe bo to ui tylko to rozumiem
		setTitle("File Copier Utility");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (task != null)
					task.cancel(true);
				dispose();
				System.exit(0);
			}
		});

		JLabel lblProgressCurrent = new JLabel("Current File: ");
		progressCurrent = new JProgressBar(0, 100);
		progressCurrent.setStringPainted(true);
		txtDetails = new JTextArea(5, 50);
		txtDetails.setEditable(false);
		DefaultCaret caret = (DefaultCaret) txtDetails.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane(txtDetails,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		btnCopy = new JButton("Copy");
		btnCopy.setFocusPainted(false);
		btnCopy.setEnabled(true);
		btnCopy.addActionListener(this);

		/*
		 * DocumentListener listener = new DocumentListener() {
		 * 
		 * @Override public void removeUpdate(DocumentEvent e) { boolean
		 * bEnabled = txtSource.getText().length() > 0 &&
		 * txtTarget.getText().length() > 0; btnCopy.setEnabled(bEnabled); }
		 * 
		 * @Override public void insertUpdate(DocumentEvent e) { boolean
		 * bEnabled = txtSource.getText().length() > 0 &&
		 * txtTarget.getText().length() > 0; btnCopy.setEnabled(bEnabled); }
		 * 
		 * @Override public void changedUpdate(DocumentEvent e) { } };
		 */

		/*
		 * txtSource.getDocument().addDocumentListener(listener);
		 * txtTarget.getDocument().addDocumentListener(listener);
		 */

		JPanel contentPane = (JPanel) getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel panProgressLabels = new JPanel(new BorderLayout(0, 5));
		JPanel panProgressBars = new JPanel(new BorderLayout(0, 5));

		panProgressLabels.add(lblProgressCurrent, BorderLayout.CENTER);
		panProgressBars.add(progressCurrent, BorderLayout.CENTER);

		JPanel panProgress = new JPanel(new BorderLayout(0, 5));
		panProgress.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Progress"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		JPanel panDetails = new JPanel(new BorderLayout());
		panDetails.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Details"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		JPanel panControls = new JPanel(new BorderLayout());
		panControls.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		panProgress.add(panProgressLabels, BorderLayout.LINE_START);
		panProgress.add(panProgressBars, BorderLayout.CENTER);
		panDetails.add(scrollPane, BorderLayout.CENTER);
		panControls.add(btnCopy, BorderLayout.CENTER);

		JPanel panUpper = new JPanel(new BorderLayout());
		panUpper.add(panProgress, BorderLayout.SOUTH);

		contentPane.add(panUpper, BorderLayout.NORTH);
		contentPane.add(panDetails, BorderLayout.CENTER);
		contentPane.add(panControls, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) { // tu tez mniej wiecej wiem o
							// co chodzi
		if ("Copy".equals(btnCopy.getText())) {
			File source = new File("C:\\Javatesting/copy");
			File target = new File("C:\\Javatesting/copied");

			if (!source.exists()) {
				JOptionPane.showMessageDialog(this,
						"The source file/directory does not exist!", "ERROR",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (!target.exists() && source.isDirectory())
				target.mkdirs();
			else {
				int option = JOptionPane
						.showConfirmDialog(
								this,
								"The target file/directory already exists, do you want to overwrite it?",
								"Overwrite the target",
								JOptionPane.YES_NO_OPTION);
				if (option != JOptionPane.YES_OPTION)
					return;
			}

			task = this.new CopyTask(source, target);
			task.addPropertyChangeListener(this); // <-- co to robi?
			task.execute();

			btnCopy.setText("Cancel");
		} else if ("Cancel".equals(btnCopy.getText())) {
			task.cancel(true);
			btnCopy.setText("Copy");
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) { // czemu to musi byc?
								// jak nie ma tego
								// to wywala bl/ad
		// if ("progress".equals(evt.getPropertyName())) {
		// int progress = (Integer) evt.getNewValue();
		// progressAll.setValue(progress);
		// }
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new FileCopier().setVisible(true);
			}
		});
	}

	class CopyTask extends SwingWorker<Void, Integer> {
		private File source;
		private File target;
		private long totalBytes = 0L;
		private long copiedBytes = 0L;

		public CopyTask(File source, File target) {
			this.source = source;
			this.target = target;

			progressCurrent.setValue(0);
		}

		@Override
		public Void doInBackground() throws Exception {
			txtDetails.append("Retrieving some info ... ");
			retrieveTotalBytes(source);
			txtDetails.append("Done!\n");

			copyFiles(source, target);
			return null;
		}

		@Override
		public void process(List<Integer> chunks) {  //Nie wiem co tu sie dzieje tez
			for (int i : chunks) {
				progressCurrent.setValue(i);
			}
		}

		@Override
		public void done() {
			setProgress(100);
			btnCopy.setText("Copy");
		}

		private void retrieveTotalBytes(File sourceFile) {
			File[] files = sourceFile.listFiles();
			for (File file : files) {
				if (file.isDirectory())
					retrieveTotalBytes(file);
				else
					totalBytes += file.length();
			}
		}

		private void copyFiles(File sourceFile, File targetFile)
				throws IOException {
			if (sourceFile.isDirectory()) {
				if (!targetFile.exists())
					targetFile.mkdirs();

				String[] filePaths = sourceFile.list();

				for (String filePath : filePaths) {
					File srcFile = new File(sourceFile, filePath);
					File destFile = new File(targetFile, filePath);

					copyFiles(srcFile, destFile);
				}
			} else {
				txtDetails.append("Copying " + sourceFile.getAbsolutePath()
						+ " ... ");

				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream(sourceFile));
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(targetFile));

				long fileBytes = sourceFile.length();
				long soFar = 0L;

				int theByte;

				while ((theByte = bis.read()) != -1) {
					bos.write(theByte);

					setProgress((int) (copiedBytes++ * 100 / totalBytes));
					publish((int) (soFar++ * 100 / fileBytes));   		// i to tez nie wiem co robi
				}

				bis.close();
				bos.close();

				publish(100);

				txtDetails.append("Done!\n");
			}
		}
	}
}
