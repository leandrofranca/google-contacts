package gdata.contacts;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.StructuredPostalAddress;

public class StructuredPostalAddresses extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(StructuredPostalAddresses.class.getName());

	public StructuredPostalAddresses(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasStructuredPostalAddresses()) {
			for (final StructuredPostalAddress structuredPostalAddress : this.getEntry()
					.getStructuredPostalAddresses()) {

				final String postalAddress = structuredPostalAddress.getFormattedAddress().getValue();

				// String postalAddressAlterado =
				// postalAddress.replaceAll("[\r\n]", " - ");
				// postalAddressAlterado = postalAddress.replaceAll("\\s-\\s$",
				// "");
				// structuredPostalAddress.getFormattedAddress().setValue(postalAddressAlterado);
				// this.setChanged(true);

				info = new StringBuilder();
				info.append("StructuredPostalAddress do Contato ");
				info.append(this.getNames().getFullName());
				info.append("\t");
				info.append("\"");
				info.append(postalAddress);
				info.append("\"");
				LOGGER.info(info.toString());

			}
		}
	}

}
