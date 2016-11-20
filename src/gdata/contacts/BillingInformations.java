package gdata.contacts;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;

public class BillingInformations extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(BillingInformations.class.getName());

	public BillingInformations(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasBillingInformation()) {
			info = new StringBuilder();
			info.append("BillingInformation do Contato ");
			info.append(this.getNames().getFullName());
			info.append("\t");
			info.append("\"");
			info.append(this.getEntry().getBillingInformation().getValue());
			info.append("\"");
			LOGGER.info(info.toString());
		}
	}

}
