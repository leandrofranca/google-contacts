package gdata.contacts;

import gdata.Constantes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;

public class EmailAddresses extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(EmailAddresses.class.getName());

	public EmailAddresses(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasEmailAddresses()) {
			final Set<String> listEmailUnico = new HashSet<String>();
			final List<Email> listEmailGoogle = new ArrayList<Email>();
			final List<Email> listToDelete = new ArrayList<Email>();

			lacoEmailAddresses: for (final Email email : this.getEntry().getEmailAddresses()) {

				final String beforeSanitize = email.getAddress(); // Email

				if (beforeSanitize == null) {
					listToDelete.add(email);
					continue lacoEmailAddresses;
				}

				final String dominio = this.getDominio(beforeSanitize); // Dominio
				final String afterSanitize = beforeSanitize.toLowerCase().replaceAll(Constantes.REGEX_SANITIZE_EMAIL, ""); // Sanitizado
				final String rel = email.getRel(); // Rel
				final boolean isPrimary = email.getPrimary(); // Primary

				if (afterSanitize.isEmpty() || !listEmailUnico.add(afterSanitize)) {
					listToDelete.add(email);
					continue lacoEmailAddresses;
				}

				if (dominio.indexOf("@gmail") > -1) {
					listEmailGoogle.add(email);
				}

				final String label = email.getLabel(); // Label
				if (label != null && !"account".equals(label)) {

					info = new StringBuilder();
					info.append("Alterando Label do Contato ");
					info.append(this.getNames().getFullName());
					info.append(" de ");
					info.append("\"");
					info.append(label);
					info.append("\"");
					info.append(" para ");
					info.append("\"");
					info.append("nulo");
					info.append("\"");
					LOGGER.info(info.toString());
					email.setLabel(null);
					email.setRel(Email.Rel.HOME);
					this.setChanged(true);

				}

				if (!beforeSanitize.equals(afterSanitize)) {

					email.setAddress(afterSanitize);
					info = new StringBuilder();
					info.append("Alterando Email do Contato ");
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

				if (rel != null) {
					if (!Constantes.EMAILS_PESSOAIS.contains(dominio) && !Email.Rel.WORK.equals(rel)) {

						email.setRel(Email.Rel.WORK);
						info = new StringBuilder();
						info.append("Alterando Rel do Contato ");
						info.append(this.getNames().getFullName());
						info.append(" de ");
						info.append("\"");
						info.append(this.getEmailRel(rel));
						info.append("\"");
						info.append(" para ");
						info.append("\"");
						info.append(this.getEmailRel(email.getRel()));
						info.append("\"");
						LOGGER.info(info.toString());
						this.setChanged(true);

					} else if (Constantes.EMAILS_PESSOAIS.contains(dominio) && !Email.Rel.HOME.equals(rel)) {

						email.setRel(Email.Rel.HOME);
						info = new StringBuilder();
						info.append("Alterando Rel do Contato ");
						info.append(this.getNames().getFullName());
						info.append(" de ");
						info.append("\"");
						info.append(this.getEmailRel(rel));
						info.append("\"");
						info.append(" para ");
						info.append("\"");
						info.append(this.getEmailRel(email.getRel()));
						info.append("\"");
						LOGGER.info(info.toString());
						this.setChanged(true);

					}
				}

				if (!isPrimary && this.getEntry().getEmailAddresses().size() == 1) {

					email.setPrimary(true);
					info = new StringBuilder();
					info.append("Tornando único Email primário do Contato ");
					info.append(this.getNames().getFullName());
					info.append("\t");
					info.append("\"");
					info.append(email.getAddress());
					info.append("\"");
					LOGGER.info(info.toString());
					this.setChanged(true);

				}

			} // Fim For

			// Excluir emails gmail duplicados
			for (final Email emailGoogleX : listEmailGoogle) {
				for (final Email emailGoogleY : listEmailGoogle) {
					final String dominioX = this.getDominio(emailGoogleX.getAddress()); // Dominio
					final String dominioY = this.getDominio(emailGoogleY.getAddress()); // Dominio
					final String loginX = emailGoogleX.getAddress().replace(dominioX, ""); // Login
					final String loginY = emailGoogleY.getAddress().replace(dominioY, ""); // Login

					if (loginX.length() < loginY.length()
							&& loginX.replaceAll(Constantes.REGEX_PONTO, "").equals(loginY.replaceAll(Constantes.REGEX_PONTO, "")) && !loginX.equals(loginY)) {
						listToDelete.add(emailGoogleX);
					}
				}
			}

			// Excluir emails duplicados
			for (final Email emailToDelete : listToDelete) {
				this.getEntry().getEmailAddresses().remove(emailToDelete);
				info = new StringBuilder();
				info.append("Excluindo Email duplicado do Contato ");
				info.append(this.getNames().getFullName());
				info.append("\t");
				info.append("\"");
				info.append(emailToDelete.getAddress());
				info.append("\"");
				LOGGER.info(info.toString());
				this.setChanged(true);
			}
		}
	}

	private String getDominio(final String email) {
		if (email == null || email == "") {
			return null;
		}
		return email.substring(email.indexOf("@"));
	}

	private String getEmailRel(final String rel) {
		if (Email.Rel.HOME.equals(rel)) {
			return "HOME";
		} else if (Email.Rel.OTHER.equals(rel)) {
			return "OTHER";
		} else if (Email.Rel.WORK.equals(rel)) {
			return "WORK";
		} else {
			return "DESCONHECIDO";
		}
	}

}
