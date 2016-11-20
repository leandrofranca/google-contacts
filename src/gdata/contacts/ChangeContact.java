package gdata.contacts;

import gdata.util.Util;

import com.google.gdata.data.contacts.ContactEntry;

public abstract class ChangeContact {

	public ChangeContact(final ContactEntry entry) {
		this.entry = entry;
	}

	private ContactEntry entry = null;

	private boolean changed = false;

	private Util util = null;

	public Util getUtil() {
		if (this.util == null) {
			this.util = Util.getInstance();
		}
		return this.util;
	}

	private Names names = null;

	public Names getNames() {
		if (this.names == null) {
			this.names = new Names(this.getEntry());
		}
		return this.names;
	}

	public ContactEntry getEntry() {
		return this.entry;
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(final boolean changed) {
		this.changed = changed;
	}

	protected abstract void organize();

	public ContactEntry call() throws Exception {
		final long tempoInicial = System.currentTimeMillis();
		this.organize();
		final long tempoFinal = System.currentTimeMillis();
		Contacts.tempoTotal += tempoFinal - tempoInicial;

		return this.isChanged() ? this.getEntry() : null;
	}

}