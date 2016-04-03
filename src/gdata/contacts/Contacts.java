package gdata.contacts;

/**
 * @author Leandro França
 */

import gdata.Connector;
import gdata.Constantes;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;

public class Contacts {

	static final Logger LOGGER = Logger.getLogger(Contacts.class.getName());

	static int contatosAlterados, contatosExcluidos;

	static ContactsService service;

	static long tempoTotal = 0;

	private boolean deleteEmptyContact(final ContactEntry entry) {
		StringBuilder info;

		if (!entry.hasPhoneNumbers() && !entry.hasEmailAddresses()) {
			info = new StringBuilder();
			info.append("Excluindo Contato vazio.");
			LOGGER.info(info.toString());

			BatchRequests.deleteEntry(entry);
			return true;
		}
		return false;
	}

	private void execute() throws Exception {
		StringBuilder info;

		final Query query = new Query(new URL(Constantes.URL_CONTATOS));
		query.setMaxResults(Short.MAX_VALUE);
		// query.setUpdatedMin(new DateTime(new GregorianCalendar(2013, 6, 25, 0, 0, 0).getTime()));
		// query.setUpdatedMax(new DateTime(new GregorianCalendar(2013, 6, 26, 0, 0, 0).getTime()));
		query.setStringCustomParameter("orderby", "lastmodified");
		query.setStringCustomParameter("showdeleted", "false");
		query.setStringCustomParameter("sortorder", "ascending");

		final ContactFeed contactFeed = service.query(query, ContactFeed.class);

		info = new StringBuilder();
		info.append(contactFeed.getEntries().size());
		info.append(" contatos encontrados.");
		LOGGER.info(info.toString());

		final ExecutorService executorService = Executors.newFixedThreadPool(50);
		final Set<Future<ContactEntry>> listaRetornoThread = new HashSet<Future<ContactEntry>>();

		final long totalContatos = contactFeed.getEntries().size();

		for (final ContactEntry entry : contactFeed.getEntries()) {

			// Limpa contatos sem informações significativas
			if (this.deleteEmptyContact(entry)) {
				continue;
			}

			// Tratando usuário sem grupo
			final Groups groups = new Groups(entry);
			listaRetornoThread.add(executorService.submit(groups));

			// Atributos principais
			final Names names = new Names(entry);
			listaRetornoThread.add(executorService.submit(names));

			final PhoneNumbers phoneNumbers = new PhoneNumbers(entry);
			listaRetornoThread.add(executorService.submit(phoneNumbers));

			final EmailAddresses emailAddresses = new EmailAddresses(entry);
			listaRetornoThread.add(executorService.submit(emailAddresses));

			// Outros atributos
			final Nicknames nicknames = new Nicknames(entry);
			listaRetornoThread.add(executorService.submit(nicknames));

			final Organizations organizations = new Organizations(entry);
			listaRetornoThread.add(executorService.submit(organizations));

			final ImAddresses imAddresses = new ImAddresses(entry);
			listaRetornoThread.add(executorService.submit(imAddresses));

			// /////////////////////////////// //
			// Listando informações adicionais //
			// /////////////////////////////// //

			// final BillingInformations billingInformations = new BillingInformations(entry);
			// listaRetornoThread.add(executorService.submit(billingInformations));

			// final PostalAddresses postalAddresses = new PostalAddresses(entry);
			// listaRetornoThread.add(executorService.submit(postalAddresses));

			// final StructuredPostalAddresses structuredPostalAddresses = new StructuredPostalAddresses(entry);
			// listaRetornoThread.add(executorService.submit(structuredPostalAddresses));

			// Limpa notas do contato
			// final Notes notes = new Notes(entry);
			// listaRetornoThread.add(executorService.submit(notes));

		}

		boolean isProcessing;
		while (true) {
			isProcessing = false;

			for (final Future<ContactEntry> retornoThread : listaRetornoThread) {
				if (retornoThread.isDone() && retornoThread.get() != null) {
					BatchRequests.updateEntry(retornoThread.get());
				} else if (!retornoThread.isDone()) {
					isProcessing = true;
				}
			}

			if (isProcessing == false) {
				break;
			}
			Thread.sleep(1000);
		}

		info = new StringBuilder();
		info.append(tempoTotal / totalContatos);
		info.append(" ms foi o tempo médio.");
		LOGGER.info(info.toString());

		// Finalizando threads
		executorService.shutdown();

		if (!Constantes.MOCK) {
			final BatchRequests batchRequests = new BatchRequests();
			batchRequests.executeBatchRequest(BatchRequests.contactFeedBatch);
		}
	}

	public static void main(final String[] args) {

		StringBuilder info;
		final long tempoInicial = System.currentTimeMillis();

		try {

			final String username;

			if (args.length > 0) {
				username = args[0].toLowerCase();
			} else {
				username = JOptionPane.showInputDialog("Usuário");
			}

			service = Connector.getInstanceContacts(username);

			// Cria lista com todos os grupos
			Groups.carregarTodos();

			// Percorre todos os contatos
			final Contacts contacts = new Contacts();
			contacts.execute();

			info = new StringBuilder();
			info.append("Contatos alterados: ");
			info.append(contatosAlterados);
			LOGGER.info(info.toString());

			info = new StringBuilder();
			info.append("Contatos excluídos: ");
			info.append(contatosExcluidos);
			LOGGER.info(info.toString());

			final long tempoFinal = System.currentTimeMillis();
			info = new StringBuilder();
			info.append("Tempo total: ");
			info.append(tempoFinal - tempoInicial);
			info.append(" ms.");
			LOGGER.info(info.toString());

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
