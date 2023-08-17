package me.mindlessly.antirat.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import me.mindlessly.antirat.AntiRat;

public class Utils {

	public static String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf);
	}

	public static void checkForRat(ArrayList<String> toCheck) throws FileNotFoundException, IOException {
		int count = 0;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = loader.getResourceAsStream("config.yml");
		try (BufferedReader b = new BufferedReader(new InputStreamReader(inputStream))) {
			String bad;
			while ((bad = b.readLine()) != null) {
				for (String s : toCheck) {
					if (s.contains(bad)) {
						System.out.println("Red flag - " + bad);
						if (s.toLowerCase().contains("discord.com/api/webhook")) 
							System.out.println("Webhook - " + s);
						else if (s.toLowerCase().contains("pastebin.com")) 
							System.out.println("pastebin - " + s);
						else if (s.toLowerCase().contains("papi.co")) {
							System.out.println("IpGrabber - " + s);
						}
						count++;
					}
				}

			}
		}
		AntiRat.setCount(AntiRat.getCount() + count);
	}

	public static void extractFolder(String zipFile, String extractFolder) {
		try {
			int BUFFER = 2048;
			File file = new File(zipFile);

			ZipFile zip = new ZipFile(file);
			String newPath = extractFolder;

			new File(newPath).mkdir();
			Enumeration zipFileEntries = zip.entries();

			while (zipFileEntries.hasMoreElements()) {

				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(newPath, currentEntry);
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
					int currentByte;
					byte data[] = new byte[BUFFER];

					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}

			}
			zip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
