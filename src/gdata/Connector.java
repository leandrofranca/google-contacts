package gdata;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.client.photos.PicasawebService;

public class Connector {

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/** Global instance of the scopes required by this quickstart. */
	private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR, DriveScopes.DRIVE,
			"https://www.google.com/m8/feeds/", "https://picasaweb.google.com/data/");

	private static GoogleService service = null;

	private static Drive serviceDrive = null;

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (final Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	private static Credential authorize(final String username) throws Exception {
		DATA_STORE_FACTORY = new FileDataStoreFactory(new java.io.File(System.getProperty("user.home"),
				".credentials/" + Constantes.APPLICATION_NAME + "/" + username));

		System.setProperty("java.net.useSystemProxies", "false");

		final GoogleClientSecrets clientSecret = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(FileUtils.openInputStream(new File("./client_secret.json"))));
		final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecret, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(username);
	}

	private static ContactsService connectContacts(final String username) throws Exception {
		final Credential credential = authorize(username);

		if (!credential.refreshToken()) {
			return null;
		}

		final ContactsService service = new ContactsService(Constantes.APPLICATION_NAME);
		service.setOAuth2Credentials(credential);
		return service;
	}

	private static PicasawebService connectPicasa(final String username) throws Exception {
		final Credential credential = authorize(username);

		if (!credential.refreshToken()) {
			return null;
		}

		final PicasawebService service = new PicasawebService(Constantes.APPLICATION_NAME);
		service.setOAuth2Credentials(credential);
		return service;
	}

	private static Drive connectDrive(final String username) throws Exception {
		final Credential credential = authorize(username);

		if (!credential.refreshToken()) {
			return null;
		}

		final Drive serviceDrive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(Constantes.APPLICATION_NAME).build();
		return serviceDrive;
	}

	public static ContactsService getInstanceContacts(final String username) throws Exception {
		if (service == null) {
			try {
				service = connectContacts(username);

			} catch (GeneralSecurityException | IOException e) {
				e.printStackTrace();
			}
		}

		return (ContactsService) service;
	}

	public static PicasawebService getInstancePicasa(final String username) throws Exception {
		if (service == null) {
			try {
				service = connectPicasa(username);

			} catch (GeneralSecurityException | IOException e) {
				e.printStackTrace();
			}
		}

		return (PicasawebService) service;
	}

	public static Drive getInstanceDrive(final String username) throws Exception {
		if (serviceDrive == null) {
			try {
				serviceDrive = connectDrive(username);

			} catch (GeneralSecurityException | IOException e) {
				e.printStackTrace();
			}
		}

		return serviceDrive;
	}

	private Connector() {
	}

}
