package at.jku.se.eatemup.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

public class FacebookImageLoader {
	private static final String baseUrl = "http://graph.facebook.com/%userID%/picture";

	private static String doGet(String urlString) throws IOException {
		URL url = new URL(urlString);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
		return Base64.encodeBase64String(response);
	}

	public static String getImageForId(String facebookid) {
		try {
			return doGet(baseUrl.replace("%userID%", facebookid));
		} catch (Exception ex) {
			return "";
		}
	}
}
