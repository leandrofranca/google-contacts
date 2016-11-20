package gdata.contacts;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.PostalAddress;

public class PostalAddresses extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(PostalAddresses.class.getName());

	public PostalAddresses(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasPostalAddresses()) {
			for (final PostalAddress postalAddress : this.getEntry().getPostalAddresses()) {

				info = new StringBuilder();
				info.append("PostalAddress do Contato ");
				info.append(this.getNames().getFullName());
				info.append("\t");
				info.append("\"");
				info.append(postalAddress.getValue());
				info.append("\"");
				LOGGER.info(info.toString());

			}
		}
	}

}
