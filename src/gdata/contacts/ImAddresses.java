package gdata.contacts;

import gdata.Constantes;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Im;

public class ImAddresses extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(ImAddresses.class.getName());

	public ImAddresses(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasImAddresses()) {
			for (final Im im : this.getEntry().getImAddresses()) {

				if (im.hasAddress()) {
					final String beforeSanitize = im.getAddress();
					final String afterSanitize = beforeSanitize.replaceAll(Constantes.REGEX_ESPACO, "").toLowerCase();
					if (!beforeSanitize.equals(afterSanitize)) {
						info = new StringBuilder();
						info.append("Sanitizando IM do Contato ");
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

						im.setAddress(afterSanitize);
						this.setChanged(true);
					}
				}

				if (im.hasLabel()) {
					// TODO Pensar o que fazer nessa situação
					im.getLabel();
				}

				if (im.hasRel()) {
					final String beforeSanitize = im.getRel();
					if (!beforeSanitize.equals(Im.Rel.HOME)) {
						im.setRel(Im.Rel.HOME);
						info = new StringBuilder();
						info.append("Trocando Rel do Contato ");
						info.append(this.getNames().getFullName());
						info.append(" de ");
						info.append("\"");
						info.append(this.getImRel(beforeSanitize));
						info.append("\"");
						info.append(" para ");
						info.append("\"");
						info.append(this.getImRel(im.getRel()));
						info.append("\"");
						LOGGER.info(info.toString());

						this.setChanged(true);
					}
				}

				if (im.hasProtocol()) {
					// TODO Pensar o que fazer nessa situação
					im.getProtocol();
				}

				if (im.hasPrimary() && im.getPrimary() && this.getEntry().getImAddresses().size() > 1) {
					im.setPrimary(false);
					info = new StringBuilder();
					info.append("Removendo IM primário do Contato ");
					info.append(this.getNames().getFullName());
					info.append("\t");
					info.append("\"");
					info.append(im.getAddress());
					info.append("\"");
					LOGGER.info(info.toString());

					this.setChanged(true);
				} else if (im.hasPrimary() && !im.getPrimary() && this.getEntry().getImAddresses().size() == 1) {
					im.setPrimary(true);
					info = new StringBuilder();
					info.append("Colocando o único IM existente como primário do Contato ");
					info.append(this.getNames().getFullName());
					info.append("\t");
					info.append("\"");
					info.append(im.getAddress());
					info.append("\"");
					LOGGER.info(info.toString());

					this.setChanged(true);
				}

			}
		}
	}

	private String getImRel(final String rel) {
		if (rel.equalsIgnoreCase(Im.Rel.HOME)) {
			return "HOME";
		} else if (rel.equalsIgnoreCase(Im.Rel.OTHER)) {
			return "OTHER";
		} else if (rel.equalsIgnoreCase(Im.Rel.WORK)) {
			return "WORK";
		} else {
			return "DESCONHECIDO";
		}
	}

	private String getImProtocol(final String protocol) {
		if (protocol.equalsIgnoreCase(Im.Protocol.AIM)) {
			return "AIM";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.GOOGLE_TALK)) {
			return "GOOGLE_TALK";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.ICQ)) {
			return "ICQ";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.JABBER)) {
			return "JABBER";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.MSN)) {
			return "MSN";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.NETMEETING)) {
			return "NETMEETING";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.QQ)) {
			return "QQ";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.SKYPE)) {
			return "SKYPE";
		} else if (protocol.equalsIgnoreCase(Im.Protocol.YAHOO)) {
			return "YAHOO";
		} else {
			return "DESCONHECIDO";
		}
	}

}
