package me.mindlessly.antirat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipFile;
import me.mindlessly.antirat.utils.Console;
import me.mindlessly.antirat.utils.OSValidator;
import me.mindlessly.antirat.utils.Utils;

public class AntiRat {
	private static int count;
	private Scanner scanner;
	private Console console;

	public static void main(String args[]) {
		AntiRat m = new AntiRat();
		try {
			m.onEnable();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setCount(int count) {
		AntiRat.count = count;
	}

	private void onEnable() throws IOException {
		count = 0;
		console = new Console();
		System.setOut(console.getOut());
		System.setIn(console.getIn());
		File folder = null;

		if (OSValidator.isWindows()) {
			folder = new File("C:/Users/" + System.getProperty("user.name") + "/AppData/Roaming/.minecraft/mods");
		} else if (OSValidator.isMac()) {
			folder = new File("~/Library/Application Support/minecraft/mods");
		} else if (OSValidator.isUnix()) {
			folder = new File("/home/" + System.getProperty("user.name") + "/.minecraft/mods");
		} else {
			System.out.println("Your OS is not supported!");
			return;
		}
		File[] contents = folder.listFiles();
		File srcDir = new File(folder.getPath()+ "/AntiRat");
		for (File file : contents) {
			count = 0;
			if (Utils.getFileExtension(file).equals(".jar")) {
				System.out.println("Currently scanning " + file.getName());
				ZipFile zip = new ZipFile(file);
				Utils.extractFolder(zip.getName(), file.getParent() + "/AntiRat");
				zip.close();
				File[] classes = srcDir.listFiles();
				if (file != null) {
					if (count > 0) {
						System.out.println("A total of " + count + " red flags have been found.");
						System.out.println("Would you like to delete " + file.getName() + "? (Y/N)");
						scanner = new Scanner(System.in);
						String decision = scanner.nextLine();
						decision = String.valueOf(decision.charAt(decision.length() - 1));
						if (decision.equalsIgnoreCase("Y")) {
							file.delete();
							System.out.println("File deleted.");
						} else {
							System.out.println("File not deleted.");
						}
					} else {
						System.out.println("File is likely safe.");
					}
					recursiveFileRemove(classes);

				}
			}
		}
		srcDir.delete();
		System.exit(0);
		if (scanner != null) {
			scanner.close();
		}
	}

	public static int getCount() {
		return count;
	}

	private void recursiveFileRemove(File[] classes) {
		for (File f : classes) {
			if (!f.isDirectory()) {
				f.delete();
			} else {
				recursiveFileRemove(f.listFiles());
				f.delete();
			}
		}
	}

}