package gdata.contacts;

import gdata.Constantes;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.ServiceException;

public class BatchRequests {

	static final Logger LOGGER = Logger.getLogger(BatchRequests.class.getName());

	static ContactFeed contactFeedBatch = new ContactFeed();

	public void executeBatchRequest(final ContactFeed contactFeedBatch) throws ServiceException, IOException {
		StringBuilder info;

		// Recursividade para tratar lotes
		while (contactFeedBatch.getEntries().size() > 100) {
			// Criando lista secundária
			final List<ContactEntry> subList = new ArrayList<ContactEntry>(
					contactFeedBatch.getEntries().subList(0, 100));
			// Criando feed parcial
			final ContactFeed partialFeed = new ContactFeed();
			partialFeed.setEntries(subList);
			// Executando método recursivamente
			this.executeBatchRequest(partialFeed);
			// Removendo itens que ja foram atualizados
			contactFeedBatch.getEntries().removeAll(subList);
		}

		final ContactFeed responseFeed = Contacts.service.batch(new URL(Constantes.URL_BATCH_CONTATOS),
				contactFeedBatch);

		for (final ContactEntry entry : responseFeed.getEntries()) {

			final String nomeCompleto = Names.getInstance(entry).getFullName();

			if (BatchUtils.isSuccess(entry)) {
				info = new StringBuilder();
				if (BatchUtils.getBatchOperationType(entry) == BatchOperationType.DELETE) {
					info.append("Contato \"");
					info.append(nomeCompleto);
					info.append("\" excluido com sucesso!");
					Contacts.contatosExcluidos++;
				} else if (BatchUtils.getBatchOperationType(entry) == BatchOperationType.UPDATE) {
					info.append("Contato \"");
					info.append(nomeCompleto);
					info.append("\" atualizado com sucesso!");
					Contacts.contatosAlterados++;
				}
				LOGGER.info(info.toString());
			} else {
				final BatchStatus status = BatchUtils.getBatchStatus(entry);
				final StringBuilder error = new StringBuilder();
				error.append("Contato ");
				error.append(nomeCompleto);
				error.append("\t");
				error.append(status.getReason());
				LOGGER.severe(error.toString());
			}

		}
	}

	public static synchronized void deleteEntry(final ContactEntry entry) {
		BatchUtils.setBatchId(entry, Constantes.BATCH_DELETE);
		BatchUtils.setBatchOperationType(entry, BatchOperationType.DELETE);
		if (contactFeedBatch.getEntries().contains(entry)) {
			contactFeedBatch.getEntries().remove(entry);
		}
		contactFeedBatch.getEntries().add(entry);
	}

	public static void updateEntry(final ContactEntry entry) {
		BatchUtils.setBatchId(entry, Constantes.BATCH_UPDATE);
		BatchUtils.setBatchOperationType(entry, BatchOperationType.UPDATE);
		if (contactFeedBatch.getEntries().contains(entry)) {
			contactFeedBatch.getEntries().remove(entry);
		}
		if (Names.getInstance(entry).isEmpty()) {
			LOGGER.severe("Contato com nome vazio encontrado.");
		} else {
			contactFeedBatch.getEntries().add(entry);
		}
	}

}
