package gdata.contacts;

import gdata.Constantes;
import gdata.util.Util;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.google.gdata.client.Query;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactGroupEntry;
import com.google.gdata.data.contacts.ContactGroupFeed;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.util.ServiceException;

public class Groups extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(Groups.class.getName());

	public Groups(final ContactEntry entry) {
		super(entry);
	}

	static Map<String, String> todosGrupos = new HashMap<String, String>();

	static String grupoDesconhecidosId;
	static String grupoMeusContatosId;

	public static void carregarTodos() throws IOException, ServiceException {
		final Query query = new Query(new URL(Constantes.URL_GRUPOS));
		query.setMaxResults(Short.MAX_VALUE);
		query.setStringCustomParameter("showdeleted", "false");

		final ContactGroupFeed contactGroupFeed = Contacts.service.query(query, ContactGroupFeed.class);

		todosGrupos = new HashMap<String, String>();
		for (final ContactGroupEntry contactGroupEntry : contactGroupFeed.getEntries()) {
			todosGrupos.put(contactGroupEntry.getId(), contactGroupEntry.getTitle().getPlainText());
		}

		if (todosGrupos.containsValue(Constantes.GRUPO_MY_CONTACTS)) {
			grupoMeusContatosId = Util.getInstance().obterChavePorValorMap(todosGrupos, Constantes.GRUPO_MY_CONTACTS);
		}
		if (todosGrupos.containsValue(Constantes.GRUPO_DESCONHECIDOS)) {
			grupoDesconhecidosId = Util.getInstance().obterChavePorValorMap(todosGrupos, Constantes.GRUPO_DESCONHECIDOS);
		} else {
			final StringBuilder error = new StringBuilder();
			error.append("Grupo \"");
			error.append(Constantes.GRUPO_DESCONHECIDOS);
			error.append("\"");
			error.append(" não existe.");
			System.exit(1);
		}

		final StringBuilder info = new StringBuilder();
		info.append("Grupos capturados\t");
		info.append(todosGrupos.values().toString());
		LOGGER.info(info.toString());
	}

	private void corrigirContatoSemGrupo() {
		if (this.getEntry().getGroupMembershipInfos().isEmpty()) {
			final StringBuilder info = new StringBuilder();
			info.append("Adicionando Contato ");
			info.append(this.getNames().getFullName());
			info.append(" ao grupo ");
			info.append("\"");
			info.append(todosGrupos.get(grupoDesconhecidosId));
			info.append("\"");
			info.append(" por não possuir grupos.");
			LOGGER.info(info.toString());

			this.getEntry().addGroupMembershipInfo(new GroupMembershipInfo(false, grupoDesconhecidosId));
			this.setChanged(true);
		}
	}

	@Override
	protected void organize() {
		this.corrigirContatoSemGrupo();
	}

}
