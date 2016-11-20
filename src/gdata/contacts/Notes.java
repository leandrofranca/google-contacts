package gdata.contacts;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.Content;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.contacts.ContactEntry;

public class Notes extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(Notes.class.getName());

	public Notes(final ContactEntry entry) {
		super(entry);
	}

	private void deleteNotes() {
		StringBuilder info;

		final Content content = this.getEntry().getContent();
		if (content != null && !((TextContent) content).getContent().getPlainText().isEmpty()) {
			info = new StringBuilder();
			info.append("Excluindo notas do Contato ");
			info.append(this.getNames().getFullName());
			info.append("\t");
			info.append("\"");
			info.append(((TextContent) content).getContent().getPlainText());
			info.append("\"");
			LOGGER.info(info.toString());

			this.getEntry().setContent(new PlainTextConstruct(null));
			this.setChanged(true);
		}
	}

	@Override
	protected void organize() {
		this.deleteNotes();
	}

}
