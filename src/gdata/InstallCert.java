/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Originally from:
 * http://blogs.sun.com/andreas/resource/InstallCert.java
 * Use:
 * java InstallCert hostname
 * Example:
 *% java InstallCert ecc.fedora.redhat.com
 */

package gdata;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Class used to add the server's certificate to the KeyStore with your trusted certificates.
 */
public class InstallCert {

	public static void main(String[] args) throws Exception {
		String host;
		int port;
		char[] passphrase;
		if ((args.length == 1) || (args.length == 2)) {
			final String[] c = args[0].split(":");
			host = c[0];
			port = (c.length == 1) ? 443 : Integer.parseInt(c[1]);
			final String p = (args.length == 1) ? "changeit" : args[1];
			passphrase = p.toCharArray();
		} else {
			System.out.println("Usage: java InstallCert [:port] [passphrase]");
			return;
		}

		File file = new File("jssecacerts");
		if (file.isFile() == false) {
			final char SEP = File.separatorChar;
			final File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
			file = new File(dir, "jssecacerts");
			if (file.isFile() == false) {
				file = new File(dir, "cacerts");
			}
		}
		System.out.println("Loading KeyStore " + file + "...");
		final InputStream in = new FileInputStream(file);
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(in, passphrase);
		in.close();

		final SSLContext context = SSLContext.getInstance("TLS");
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		final X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		final SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		final SSLSocketFactory factory = context.getSocketFactory();

		System.out.println("Opening connection to " + host + ":" + port + "...");
		final SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);
		try {
			System.out.println("Starting SSL handshake...");
			socket.startHandshake();
			socket.close();
			System.out.println();
			System.out.println("No errors, certificate is already trusted");
		} catch (final SSLException e) {
			System.out.println();
			e.printStackTrace(System.out);
		}

		final X509Certificate[] chain = tm.chain;
		if (chain == null) {
			System.out.println("Could not obtain server certificate chain");
			return;
		}

		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println();
		System.out.println("Server sent " + chain.length + " certificate(s):");
		System.out.println();
		final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		final MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < chain.length; i++) {
			final X509Certificate cert = chain[i];
			System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
			System.out.println("   Issuer  " + cert.getIssuerDN());
			sha1.update(cert.getEncoded());
			System.out.println("   sha1    " + toHexString(sha1.digest()));
			md5.update(cert.getEncoded());
			System.out.println("   md5     " + toHexString(md5.digest()));
			System.out.println();
		}

		System.out.println("Enter certificate to add to trusted keystore or 'q' to quit: [1]");
		final String line = reader.readLine().trim();
		int k;
		try {
			k = (line.length() == 0) ? 0 : Integer.parseInt(line) - 1;
		} catch (final NumberFormatException e) {
			System.out.println("KeyStore not changed");
			return;
		}

		final X509Certificate cert = chain[k];
		final String alias = host + "-" + (k + 1);
		ks.setCertificateEntry(alias, cert);

		final OutputStream out = new FileOutputStream("jssecacerts");
		ks.store(out, passphrase);
		out.close();

		System.out.println();
		System.out.println(cert);
		System.out.println();
		System.out.println("Added certificate to keystore 'jssecacerts' using alias '" + alias + "'");
	}

	private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

	private static String toHexString(byte[] bytes) {
		final StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes) {
			b &= 0xff;
			sb.append(HEXDIGITS[b >> 4]);
			sb.append(HEXDIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}

	private static class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {

			/**
			 * This change has been done due to the following resolution advised for Java 1.7+ http://infposs.blogspot.kr/2013/06/installcert-and-java-7.html
			 **/

			return new X509Certificate[0];
			// throw new UnsupportedOperationException();
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			this.tm.checkServerTrusted(chain, authType);
		}
	}
}