package gdata.contacts;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.Organization;

public class Organizations extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(Organizations.class.getName());

	public Organizations(final ContactEntry entry) {
		super(entry);
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasOrganizations()) {
			for (final Organization organization : this.getEntry().getOrganizations()) {

				// Nome da Empresa
				if (organization.hasOrgName()) {
					final OrgName orgName = organization.getOrgName();
					String beforeSanitize = orgName.getValue();
					if (beforeSanitize == null) {
						beforeSanitize = "";
					}
					final String afterSanitize = this.getUtil().sanitize(beforeSanitize);
					if (!beforeSanitize.equals(afterSanitize)) {
						orgName.setValue(afterSanitize);
						info = new StringBuilder();
						info.append("Sanitizando OrgName do Contato ");
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

				// Titulo
				if (organization.hasOrgTitle()) {
					final String beforeSanitize = organization.getOrgTitle().getValue();
					final String afterSanitize = this.getUtil().sanitize(beforeSanitize);
					if (!beforeSanitize.equals(afterSanitize)) {
						organization.getOrgTitle().setValue(afterSanitize);
						info = new StringBuilder();
						info.append("Sanitizando OrgTitle do Contato ");
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

				// Grupo
				if (organization.hasRel()) {
					final String beforeSanitize = organization.getRel();
					if (!beforeSanitize.equals(Organization.Rel.WORK)) {
						organization.setRel(Organization.Rel.WORK);
						info = new StringBuilder();
						info.append("Trocando Rel do Contato ");
						info.append(this.getNames().getFullName());
						info.append(" de ");
						info.append("\"");
						info.append(this.getOrganizationRel(beforeSanitize));
						info.append("\"");
						info.append(" para ");
						info.append("\"");
						info.append(this.getOrganizationRel(organization.getRel()));
						info.append("\"");
						LOGGER.info(info.toString());

						this.setChanged(true);
					}
				}

				// Label
				if (organization.hasLabel()) {
					info = new StringBuilder();
					info.append("Label do Contato ");
					info.append(this.getNames().getFullName());
					info.append("\t");
					info.append("\"");
					info.append(organization.getLabel());
					info.append("\"");
					LOGGER.info(info.toString());
				}

				if (organization.hasPrimary() && organization.getPrimary()
						&& this.getEntry().getOrganizations().size() > 1) {
					organization.setPrimary(false);
					info = new StringBuilder();
					info.append("Removendo Organization primário do Contato ");
					info.append(this.getNames().getFullName());
					LOGGER.info(info.toString());

					this.setChanged(true);
				} else if (organization.hasPrimary() && !organization.getPrimary()
						&& this.getEntry().getOrganizations().size() == 1) {
					organization.setPrimary(true);
					info = new StringBuilder();
					info.append("Colocando único Organization como primário do Contato ");
					info.append(this.getNames().getFullName());
					LOGGER.info(info.toString());

					this.setChanged(true);
				}

			}
		}
	}

	private String getOrganizationRel(final String rel) {
		if (rel.equalsIgnoreCase(Organization.Rel.OTHER)) {
			return "OTHER";
		} else if (rel.equalsIgnoreCase(Organization.Rel.WORK)) {
			return "WORK";
		} else {
			return "DESCONHECIDO";
		}
	}

}
