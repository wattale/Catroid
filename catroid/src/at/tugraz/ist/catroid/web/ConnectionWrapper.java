/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.web;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.os.Handler;
import android.util.Log;
import at.tugraz.ist.catroid.common.Consts;

public class ConnectionWrapper {

	private HttpURLConnection urlConnection;
	private final static String TAG = ConnectionWrapper.class.getSimpleName();

	private String getString(InputStream is) {
		if (is == null) {
			return "";
		}
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr, Consts.BUFFER_8K);

			String line;
			String response = "";
			while ((line = br.readLine()) != null) {
				response += line;
			}
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public void sendFTP(String filePath, Handler handler, String projectName, String fileName) {

		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(Consts.SERVER, 21);
			ftpClient.login(Consts.USERNAME, Consts.PASSWORD);

			if (ftpClient.getReplyString().contains("230")) {
				ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
				BufferedInputStream buffIn = null;
				buffIn = new BufferedInputStream(new FileInputStream(filePath));
				ftpClient.enterLocalPassiveMode();
				ProgressInputStream progressInput = new ProgressInputStream(buffIn, handler);

				ftpClient.storeFile(fileName, progressInput);
				buffIn.close();
				ftpClient.logout();
				ftpClient.disconnect();
			}

		} catch (IOException e) {
			Log.d("FTP", "Error: FTP Upload failed!");
			e.printStackTrace();
		}
	}

	public boolean checkChangeTime(String projectName) {

		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(Consts.SERVER, 21);
			ftpClient.login(Consts.USERNAME, Consts.PASSWORD);
			//ftpClient.changeWorkingDirectory(Consts.PATH);
			// TODO a test with the right catroid server 

			if (ftpClient.getReplyString().contains("230")) {
				FTPFile[] ftpFiles = ftpClient.listFiles(".");
				String[] fileList = null;
				fileList = new String[ftpFiles.length];

				for (int i = 0; i < ftpFiles.length; i++) {
					fileList[i] = ftpFiles[i].getName();
				}

				String name[] = ftpClient.listNames(".");
				String directory = ftpClient.printWorkingDirectory();

				ftpClient.logout();
				ftpClient.disconnect();
				return true;
			}

		} catch (IOException e) {
			Log.d("FTP", "Error: FTP Conenction failed!");
			e.printStackTrace();
		}
		return false;
	}

	//	public String doHttpPost(String urlString, HashMap<String, String> postValues) throws IOException,
	//			WebconnectionException {
	//
	//		buildPost(urlString, postValues);
	//
	//		// response code != 2xx -> error
	//		if (urlConnection.getResponseCode() / 100 != 2) {
	//			throw new WebconnectionException(urlConnection.getResponseCode());
	//		}
	//		InputStream resultStream = urlConnection.getInputStream();
	//		return getString(resultStream);
	//	}

	public void doHttpPostFileDownload(String urlString, HashMap<String, String> postValues, String filePath)
			throws IOException {
		//MultiPartFormOutputStream out = buildPost(urlString, postValues);
		//out.close();

		// read response from server
		DataInputStream input = new DataInputStream(urlConnection.getInputStream());

		File file = new File(filePath);
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> DOWNLOAD
		byte[] buffer = new byte[Consts.BUFFER_8K];
		int length = 0;
		while ((length = input.read(buffer)) != -1) {
			fos.write(buffer, 0, length);
		}
		input.close();
		fos.flush();
		fos.close();
	}

	public String doHttpPost(String urlString, HashMap<String, String> postValues) throws IOException {
		MultiPartFormOutputStream out = buildPost(urlString, postValues);
		out.close();

		InputStream resultStream = null;
		//try {

		Log.e("bla", "http code: " + urlConnection.getResponseCode());
		resultStream = urlConnection.getInputStream();
		//		} catch (FileNotFoundException e) {
		//			Log.e("bla", "error string: " + getString(urlConnection.getErrorStream()));
		//		}
		return getString(resultStream);
	}

	private MultiPartFormOutputStream buildPost(String urlString, HashMap<String, String> postValues)
			throws IOException {
		if (postValues == null) {
			postValues = new HashMap<String, String>();
		}

		URL url = new URL(urlString);

		String boundary = MultiPartFormOutputStream.createBoundary();
		urlConnection = (HttpURLConnection) MultiPartFormOutputStream.createConnection(url);

		urlConnection.setRequestProperty("Accept", "*/*");
		urlConnection.setRequestProperty("Content-Type", MultiPartFormOutputStream.getContentType(boundary));

		urlConnection.setRequestProperty("Connection", "Keep-Alive");
		urlConnection.setRequestProperty("Cache-Control", "no-cache");

		MultiPartFormOutputStream out = new MultiPartFormOutputStream(urlConnection.getOutputStream(), boundary);

		Set<Entry<String, String>> entries = postValues.entrySet();
		for (Entry<String, String> entry : entries) {
			Log.d(TAG, "key: " + entry.getKey() + ", value: " + entry.getValue());
			out.writeField(entry.getKey(), entry.getValue());
		}

		return out;
	}

	//private MultiPartFormOutputStream buildPost(String urlString, HashMap<String, String> postValues)
	//	private void buildPost(String urlString, HashMap<String, String> postValues) throws IOException {
	//		if (postValues == null) {
	//			postValues = new HashMap<String, String>();
	//		}
	//
	//		URL url = new URL(urlString);
	//
	//		String boundary = MultiPartFormOutputStream.createBoundary();
	//		urlConnection = (HttpURLConnection) MultiPartFormOutputStream.createConnection(url);
	//
	//		urlConnection.setRequestProperty("Accept", "*/*");
	//		urlConnection.setRequestProperty("Content-Type", MultiPartFormOutputStream.getContentType(boundary));
	//
	//		urlConnection.setRequestProperty("Connection", "Keep-Alive");
	//		urlConnection.setRequestProperty("Cache-Control", "no-cache");
	//
	//		MultiPartFormOutputStream out = new MultiPartFormOutputStream(urlConnection.getOutputStream(), boundary);
	//		String postData = "";
	//		Set<Entry<String, String>> entries = postValues.entrySet();
	//		for (Entry<String, String> entry : entries) {
	//			Log.d(TAG, "key: " + entry.getKey() + ", value: " + entry.getValue());
	//			if (postData != "") {
	//				postData += "&";
	//			}
	//			postData += entry.getKey() + "=" + entry.getValue();
	//			out.writeField(entry.getKey(), entry.getValue());
	//			out.writeField(entry.getKey(), entry.getValue());
	//		}
	//
	//		//URL url = new URL(urlString);
	//		urlConnection = (HttpURLConnection) MultiPartFormOutputStream.createConnection(url);
	//		urlConnection.setRequestProperty(Consts.URL_ACCEPT_KEY, Consts.URL_ACCEPT_VALUE);
	//		urlConnection.setRequestProperty(Consts.URL_CONTENT_TYPE_KEY, Consts.URL_CONTENT_TYPE_VALUE);
	//		urlConnection.setRequestProperty(Consts.URL_CONNECTION_KEY, Consts.URL_CONNECTION_VALUE);
	//		urlConnection.setRequestProperty(Consts.URL_CACHE_KEY, Consts.URL_CACHE_VALUE);
	//		//urlConnection.setRequestProperty(Consts.URL_LENGTH_KEY, Integer.toString(postData.length()));
	//
	//		//OutputStream out = urlConnection.getOutputStream();
	//		//out.write(postData.getBytes());
	//		out.flush();
	//		out.close();
	//	}
}
