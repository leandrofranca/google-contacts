package gdata.contacts;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.Nickname;

public class Nicknames extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(Nicknames.class.getName());

	public Nicknames(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		// Apelido
		if (this.getEntry().hasNickname()) {
			final Nickname nickname = this.getEntry().getNickname();
			String beforeSanitize = nickname.getValue();
			if (beforeSanitize == null) {
				beforeSanitize = "";
			}
			final String afterSanitize = beforeSanitize.toLowerCase();
			// String afterSanitize = Util.sanitize(beforeSanitize);
			if (!beforeSanitize.equals(afterSanitize)) {
				nickname.setValue(afterSanitize);
				info = new StringBuilder();
				info.append("Sanitizando Nickname do Contato ");
				info.append(this.getNames().getFullName());
				info.append(" de ");
				info.append("\"");
				info.append(beforeSanitize);
				info.append("\"");
				info.append(" para ");
				info.append("\"");
				info.append(afterSanitize);
				info.append("\"");
				LOGGER.info(info.toString());

				this.setChanged(true);
			}
		}
	}

}
