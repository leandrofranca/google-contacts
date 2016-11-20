package gdata.contacts;

import gdata.Constantes;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.Name;

public class Names extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(Names.class.getName());

	public Names(final ContactEntry entry) {
		super(entry);
	}

	public static Names getInstance(final ContactEntry entry) {
		return new Names(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		// Nome
		if (this.getEntry().hasName() && !this.isEmpty()) {
			final Name name = this.getEntry().getName();
			final FullName fullName = name.getFullName();
			final String beforeSanitize = fullName.getValue();
			final String afterSanitize = this.getUtil().sanitize(beforeSanitize);

			if (beforeSanitize != null && afterSanitize != null && !beforeSanitize.equals(afterSanitize)) {
				if (name.hasNamePrefix()) {
					name.setNamePrefix(null);
				}
				if (name.hasGivenName()) {
					name.setGivenName(null);
				}
				if (name.hasAdditionalName()) {
					name.setAdditionalName(null);
				}
				if (name.hasFamilyName()) {
					name.setFamilyName(null);
				}
				if (name.hasNameSuffix()) {
					name.setNameSuffix(null);
				}

				fullName.setValue(afterSanitize);
				info = new StringBuilder();
				info.append("Sanitizando FullName do Contato ");
				info.append(this.getFullName());
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

	public String getFullName() {
		return this.isEmpty() ? null : this.getEntry().getName().getFullName().getValue();
	}

	public boolean isEmpty() {
		return this.getEntry().getName() == null || this.getEntry().getName().getFullName() == null
				|| this.getEntry().getName().getFullName().getValue() == null
				|| "".equals(this.getUtil().sanitize(this.getEntry().getName().getFullName().getValue()));
	}

	private void corrigirContatoSemNome() {
		StringBuilder info;

		if (!this.getEntry().hasName() || this.isEmpty()) {
			String afterName = "";
			if (this.getEntry().hasEmailAddresses()) {
				afterName = Constantes.PREFIXO_POS_CORRECAO
						+ this.getUtil().sanitize(this.getEntry().getEmailAddresses().get(0).getAddress());
			}
			if (this.getEntry().hasPhoneNumbers()) {
				afterName = Constantes.PREFIXO_POS_CORRECAO + this.getEntry().getPhoneNumbers().get(0).getPhoneNumber();
			}

			info = new StringBuilder();
			info.append("Contato SEM NOME encontrado. Alterando para \"");
			info.append(afterName);
			info.append("\"");
			LOGGER.info(info.toString());

			// Construindo novo nome
			final FullName fullName = new FullName(afterName, null);
			final Name name = new Name();
			name.setFullName(fullName);
			this.getEntry().setName(name);

			info = new StringBuilder();
			info.append("Adicionando Contato ");
			info.append(afterName);
			info.append(" ao grupo \"");
			info.append(Groups.todosGrupos.get(Groups.grupoDesconhecidosId));
			info.append("\"");
			LOGGER.info(info.toString());

			this.getEntry().addGroupMembershipInfo(new GroupMembershipInfo(false, Groups.grupoDesconhecidosId));
			this.setChanged(true);
		}
	}

	@Override
	public ContactEntry call() throws Exception {
		this.corrigirContatoSemNome();
		return super.call();
	}

}
