package gdata.contacts;

import gdata.Constantes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.extensions.PhoneNumber;

public class PhoneNumbers extends ChangeContact implements Callable<ContactEntry> {

	static final Logger LOGGER = Logger.getLogger(PhoneNumbers.class.getName());

	public PhoneNumbers(final ContactEntry entry) {
		super(entry);
	}

	@Override
	public ContactEntry call() throws Exception {
		this.corrigirContatoSemTelefone();
		return super.call();
	}

	private void corrigeLabelTelefoneCelular(final PhoneNumber phoneNumber, final String telefone, final String label,
			final String operadoraTipo) {
		if (this.isTelefoneCelular(telefone)) {
			if (operadoraTipo != null) {
				phoneNumber.setLabel(operadoraTipo);
				phoneNumber.setRel(null);
			} else if (!this.isLabel(label, Constantes.LABEL_CELULAR) && !label.startsWith("Rdio")) {
				phoneNumber.setLabel(Constantes.LABEL_CELULAR);
				phoneNumber.setRel(null);
			}
		}
	}

	private void corrigeLabelTelefoneFixo(final PhoneNumber phoneNumber, final String telefone, final String label,
			final String operadoraTipo) {
		if (this.isTelefoneFixo(telefone)) {
			if (operadoraTipo != null) {
				phoneNumber.setLabel(operadoraTipo);
				phoneNumber.setRel(null);
			} else if (!this.isLabel(label, Constantes.LABEL_FIXO)) {
				phoneNumber.setLabel(Constantes.LABEL_FIXO);
				phoneNumber.setRel(null);
			}
		}
	}

	private void corrigirContatoSemTelefone() {
		if (!this.getEntry().hasPhoneNumbers() && this.getEntry().hasGroupMembershipInfos() && this.getEntry()
				.getGroupMembershipInfos().remove(new GroupMembershipInfo(false, Groups.grupoMeusContatosId))) {
			StringBuilder info;
			info = new StringBuilder();
			info.append("Removendo Contato ");
			info.append(this.getNames().getFullName());
			info.append(" do grupo ");
			info.append("\"");
			info.append(Groups.todosGrupos.get(Groups.grupoMeusContatosId));
			info.append("\"");
			LOGGER.info(info.toString());

			this.setChanged(true);
		}
	}

	private String executePost(final String targetURL, final String urlParameters) {
		HttpsURLConnection connection = null;
		String retorno = null;

		try {

			final URL url = new URL(targetURL);
			this.aplicarCertificado();
			// final Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			// InetSocketAddress("192.168.90.4", 9090));
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36");
			connection.setRequestProperty("Referer", Constantes.URL_CONSULTA_OPERADORA);
			if (urlParameters != null) {
				connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			}
			connection.setRequestProperty("Content-Language", "pt-BR");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			if (urlParameters != null) {
				DataOutputStream wr;
				wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
			}

			// Get Response
			final InputStream is = connection.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			final StringBuilder response = new StringBuilder();
			String line = null;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			retorno = response.toString();
			rd.close();

			// } catch (final MalformedURLException e) {
			// } catch (final ProtocolException e) {
			// } catch (final IOException e) {
		} catch (final Exception e) {
			final StringBuilder error = new StringBuilder();
			error.append("Ocorreu algum problema ao acessar ");
			error.append("\t");
			error.append(targetURL);
			error.append("\t");
			error.append(urlParameters);
			LOGGER.severe(error.toString());
			LOGGER.severe(e.getMessage());
			retorno = null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return retorno;
	}

	private void aplicarCertificado() throws NoSuchAlgorithmException, KeyManagementException {
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());

		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		final HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	private String formatarTelefone(final String telefone) {
		if (telefone.length() == 8) {
			return telefone.substring(0, 4) + "-" + telefone.substring(4);
		} else if (telefone.length() == 9) {
			return telefone.substring(0, 1) + " " + telefone.substring(1, 5) + "-" + telefone.substring(5);
		} else {
			return telefone;
		}
	}

	private String formatarTelefone(final String zero, final String ddd, final String telefone) {
		return MessageFormat.format(Constantes.PADRAO_TELEFONE, zero, ddd, this.formatarTelefone(telefone));
	}

	private String getPhoneNumberRel(final String rel) {
		if (rel.equalsIgnoreCase(PhoneNumber.Rel.ASSISTANT)) {
			return "ASSISTANT";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.CALLBACK)) {
			return "CALLBACK";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.CAR)) {
			return "CAR";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.COMPANY_MAIN)) {
			return "COMPANY_MAIN";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.FAX)) {
			return "FAX";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.GENERAL)) {
			return "GENERAL";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.HOME)) {
			return "HOME";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.HOME_FAX)) {
			return "HOME_FAX";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.INTERNAL_EXTENSION)) {
			return "INTERNAL_EXTENSION";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.ISDN)) {
			return "ISDN";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.MAIN)) {
			return "MAIN";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.MOBILE)) {
			return "MOBILE";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.OTHER)) {
			return "OTHER";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.OTHER_FAX)) {
			return "OTHER_FAX";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.PAGER)) {
			return "PAGER";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.RADIO)) {
			return "RADIO";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.SATELLITE)) {
			return "SATELLITE";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.TELEX)) {
			return "TELEX";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.TTY_TDD)) {
			return "TTY_TDD";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.WORK)) {
			return "WORK";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.WORK_FAX)) {
			return "WORK_FAX";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.WORK_MOBILE)) {
			return "WORK_MOBILE";
		} else if (rel.equalsIgnoreCase(PhoneNumber.Rel.WORK_PAGER)) {
			return "WORK_PAGER";
		} else {
			return "DESCONHECIDO";
		}
	}

	private boolean isDdd9Digitos(final String ddd) {
		return Arrays.asList(Constantes.ddd9Digitos).contains(Integer.valueOf(ddd));
	}

	private boolean isDddNot9Digitos(final String ddd) {
		return Arrays.asList(Constantes.dddNot9Digitos).contains(Integer.valueOf(ddd));
	}

	private boolean isLabel(final String labelPhone, final String label) {
		if (labelPhone == null || label == null) {
			return false;
		}
		return labelPhone.startsWith(label);
	}

	private boolean isTelefoneCelular(final String telefone) {
		if (telefone == null) {
			return false;
		}
		return telefone.startsWith("9") || telefone.startsWith("8") || telefone.startsWith("7")
				|| telefone.startsWith("6") || telefone.startsWith("5");
	}

	private boolean isTelefoneFixo(final String telefone) {
		if (telefone == null) {
			return false;
		}
		return telefone.startsWith("2") || telefone.startsWith("3") && !telefone.startsWith("300")
				|| telefone.startsWith("4") && !telefone.startsWith("400");
	}

	private boolean isTelefoneGratuito(final String telefone) {
		if (telefone == null) {
			return false;
		}
		return telefone.startsWith("0800") && telefone.length() == 11;
	}

	private boolean isTelefoneServico(final String telefone) {
		if (telefone == null) {
			return false;
		}
		return telefone.startsWith("400") || telefone.startsWith("300");
	}

	private boolean isTim(final PhoneNumber phoneNumber) {
		return phoneNumber.getLabel().toUpperCase().indexOf(Constantes.TIM) > -1;
	}

	private boolean isVivo(final PhoneNumber phoneNumber) {
		return phoneNumber.getLabel().toUpperCase().indexOf(Constantes.VIVO) > -1;
	}

	@SuppressWarnings("unused")
	private String obterOperadora(final String telefone) {
		if (Constantes.VERIFICAR_OPERADORA == false) {
			return null;
		}

		final long tempoInicial = System.currentTimeMillis();
		String retorno = null;

		final StringBuilder params = new StringBuilder();
		params.append("numero=");
		try {
			params.append(URLEncoder.encode(telefone, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			final StringBuilder error = new StringBuilder();
			error.append("Erro de encoding na passagem de parâmetro para consulta de operadora.");
			LOGGER.severe(error.toString());
			return null;
		}

		final String resultado = this.executePost(Constantes.URL_CONSULTA_OPERADORA, params.toString());

		// Evitando NullPointerException
		if (resultado == null) {
			return null;
		}

		String operadora = null, tipo = null;

		// Trata retorno da consulta
		final Matcher matcher = Pattern.compile(Constantes.REGEX_PATTERN_CONSULTA_OPERADORA).matcher(resultado);

		// Separando conteúdo
		if (matcher.find()) {
			operadora = this.getUtil().sanitize(matcher.group(1).trim());
			tipo = this.getUtil().sanitize(matcher.group(2).trim());
		}

		// Devolvendo resultado do Label
		if (operadora != null && tipo != null) {
			retorno = MessageFormat.format("{0} ({1})", tipo, operadora);
		}

		final long tempoFinal = System.currentTimeMillis();
		final StringBuilder info = new StringBuilder();
		info.append(tempoFinal - tempoInicial);
		info.append(" ms.");
		// LOGGER.info(info.toString());
		return retorno;
	}

	@Override
	protected void organize() {
		StringBuilder info;

		if (this.getEntry().hasPhoneNumbers()) {
			// Inicializando variáveis
			final Set<String> telefonesUnicos = new HashSet<String>();
			final List<PhoneNumber> listToDelete = new ArrayList<PhoneNumber>();

			lacoPhoneNumbers: for (final PhoneNumber phoneNumber : this.getEntry().getPhoneNumbers()) {

				// Partes do telefone
				String ddi = null;
				String zero = "0";
				@SuppressWarnings("unused")
				String operadora = null;
				String ddd = null;
				String telefone = null;

				// Capturando informações
				final boolean isPrimary = phoneNumber.getPrimary();
				final String beforeSanitize = phoneNumber.getPhoneNumber();
				final String afterSanitize = beforeSanitize.replaceAll(Constantes.REGEX_CARACTERES_INVALIDOS_TELEFONE,
						"");
				final String label = phoneNumber.getLabel() == null ? "" : phoneNumber.getLabel();
				final String rel = phoneNumber.getRel() == null ? "" : phoneNumber.getRel();

				// Verifica duplicidade
				if (afterSanitize == null || afterSanitize.isEmpty() || !telefonesUnicos.add(beforeSanitize)) {
					listToDelete.add(phoneNumber);
					continue lacoPhoneNumbers;
				}

				Matcher matcher = null;

				// Correcao de LABEL
				if (afterSanitize.startsWith(Constantes.ZERO_OITOCENTOS)) {

					if (this.isTelefoneGratuito(afterSanitize) && !this.isLabel(label, Constantes.LABEL_GRATUITO)) {
						phoneNumber.setLabel(Constantes.LABEL_GRATUITO);
						phoneNumber.setRel(null);
					}
					if (this.isTelefoneGratuito(afterSanitize) && !beforeSanitize.equals(afterSanitize)) {
						phoneNumber.setPhoneNumber(afterSanitize);
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_ZERO_OPERADORA_DDD_TELEFONE)
						.matcher(afterSanitize)).find()) {

					// Com operadora e DDD
					// 0 + Operadora + DDD + Telefone
					// 0 41 71 88888888 / 0 41 11 988888888

					zero = matcher.group(2);
					operadora = matcher.group(3);
					ddd = matcher.group(4);
					telefone = matcher.group(5);

					// Tratamento de Rel e Label
					if (this.isTelefoneServico(telefone) && !this.isLabel(label, Constantes.LABEL_SERVICO)) {
						phoneNumber.setLabel(Constantes.LABEL_SERVICO);
						phoneNumber.setRel(null);
					} else {
						final String operadoraTipo = this.obterOperadora(ddd.concat(telefone));
						this.corrigeLabelTelefoneCelular(phoneNumber, telefone, label, operadoraTipo);
						this.corrigeLabelTelefoneFixo(phoneNumber, telefone, label, operadoraTipo);
					}

					if (this.isTelefoneServico(telefone)) {
						// Telefone de serviço não tem DDD. Ex.: 4002-0022
						// (Bradesco)
						phoneNumber.setPhoneNumber(this.formatarTelefone(telefone));
					} else if (this.isDdd9Digitos(ddd) && this.isTelefoneCelular(telefone) && telefone.length() == 8) {
						// Acrescenta nono dígito
						telefone = "9" + telefone;
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (this.isDddNot9Digitos(ddd) && this.isTelefoneCelular(telefone)
							&& telefone.length() == 9) {
						// Remove nono dígito
						telefone = telefone.substring(1);
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (!beforeSanitize.equals(this.formatarTelefone(zero, ddd, telefone))) {
						// Não altera o que ja está correto
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_ZERO_DDD_TELEFONE).matcher(afterSanitize))
						.find()) {

					// Com 0, DDD e Sem operadora
					// 0 + DDD + Telefone
					// 0 11 988888888 / 0 71 88888888

					zero = matcher.group(2);
					ddd = matcher.group(3);
					telefone = matcher.group(4);

					// Tratamento de Rel e Label
					if (this.isTelefoneServico(telefone) && !this.isLabel(label, Constantes.LABEL_SERVICO)) {
						phoneNumber.setLabel(Constantes.LABEL_SERVICO);
						phoneNumber.setRel(null);
					} else {
						final String operadoraTipo = this.obterOperadora(ddd.concat(telefone));
						this.corrigeLabelTelefoneCelular(phoneNumber, telefone, label, operadoraTipo);
						this.corrigeLabelTelefoneFixo(phoneNumber, telefone, label, operadoraTipo);
					}

					if (this.isTelefoneServico(telefone)) {
						// Telefone de serviço não tem DDD. Ex.: 4002-0022
						// (Bradesco)
						phoneNumber.setPhoneNumber(this.formatarTelefone(telefone));
					} else if (this.isDdd9Digitos(ddd) && this.isTelefoneCelular(telefone) && telefone.length() == 8) {
						// Acrescenta nono dígito
						telefone = "9" + telefone;
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (this.isDddNot9Digitos(ddd) && this.isTelefoneCelular(telefone)
							&& telefone.length() == 9) {
						// Remove nono dígito
						telefone = telefone.substring(1);
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (!beforeSanitize.equals(this.formatarTelefone(zero, ddd, telefone))) {
						// Não altera o que ja está correto
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_DDI55_DDD_TELEFONE).matcher(afterSanitize))
						.find()) {

					// Com DDI, DDD e sem operadora
					// DDI + DDD + Telefone
					// +55 71 88888888 / 55 71 88888888 / 55 71 988888888

					ddi = matcher.group(2);
					ddd = matcher.group(3);
					telefone = matcher.group(4);

					// Tratamento de Rel e Label
					if (this.isTelefoneServico(telefone) && !this.isLabel(label, Constantes.LABEL_SERVICO)) {
						phoneNumber.setLabel(Constantes.LABEL_SERVICO);
						phoneNumber.setRel(null);
					} else {
						final String operadoraTipo = this.obterOperadora(ddd.concat(telefone));
						this.corrigeLabelTelefoneCelular(phoneNumber, telefone, label, operadoraTipo);
						this.corrigeLabelTelefoneFixo(phoneNumber, telefone, label, operadoraTipo);
					}

					if (this.isTelefoneServico(telefone)) {
						// Telefone de serviço não tem DDD. Ex.: 4002-0022
						// (Bradesco)
						phoneNumber.setPhoneNumber(this.formatarTelefone(telefone));
					} else if (this.isDdd9Digitos(ddd) && this.isTelefoneCelular(telefone) && telefone.length() == 8) {
						// Acrescenta nono dígito
						telefone = "9" + telefone;
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (this.isDddNot9Digitos(ddd) && this.isTelefoneCelular(telefone)
							&& telefone.length() == 9) {
						// Remove nono dígito
						telefone = telefone.substring(1);
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (!beforeSanitize.equals(this.formatarTelefone(zero, ddd, telefone))) {
						// Não altera o que ja está correto
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_DDI_DDD_TELEFONE).matcher(afterSanitize))
						.find()) {

					// Com DDI, DDD e sem operadora
					// DDI + DDD + Telefone
					// +171 88888888 / 171 88888888 / 171 988888888

					ddi = matcher.group(2);
					telefone = matcher.group(3);

					// Tratamento de Rel e Label
					if (!this.isLabel(label, Constantes.LABEL_INTERNACIONAL)) {
						phoneNumber.setLabel(Constantes.LABEL_INTERNACIONAL);
						phoneNumber.setRel(null);
					}

					if (!beforeSanitize.equals("+" + ddi + " " + this.formatarTelefone(telefone))) {
						phoneNumber.setPhoneNumber("+" + ddi + " " + this.formatarTelefone(telefone));
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_DDD_TELEFONE).matcher(afterSanitize)).find()) {

					// Só com DDD e telefone
					// DDD (sem o "0") + Telefone
					// 71 88888888 / 71 988888888

					ddd = matcher.group(2);
					telefone = matcher.group(3);

					// Tratamento de Rel e Label
					if (this.isTelefoneServico(telefone) && !this.isLabel(label, Constantes.LABEL_SERVICO)) {
						phoneNumber.setLabel(Constantes.LABEL_SERVICO);
						phoneNumber.setRel(null);
					} else {
						final String operadoraTipo = this.obterOperadora(ddd.concat(telefone));
						this.corrigeLabelTelefoneCelular(phoneNumber, telefone, label, operadoraTipo);
						this.corrigeLabelTelefoneFixo(phoneNumber, telefone, label, operadoraTipo);
					}

					if (this.isTelefoneServico(telefone)) {
						// Telefone de serviço não tem DDD. Ex.: 4002-0022
						// (Bradesco)
						phoneNumber.setPhoneNumber(this.formatarTelefone(telefone));
					} else if (this.isDdd9Digitos(ddd) && this.isTelefoneCelular(telefone) && telefone.length() == 8) {
						// Acrescenta nono dígito
						telefone = "9" + telefone;
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (!beforeSanitize.equals(this.formatarTelefone(zero, ddd, telefone))) {
						// Não altera o que ja está correto
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_TELEFONE).matcher(afterSanitize)).find()) {

					// Telefone
					// 88888888 / 988888888

					ddd = "85";
					telefone = matcher.group(1);

					// Tratamento de Rel e Label
					if (this.isTelefoneServico(telefone) && !this.isLabel(label, Constantes.LABEL_SERVICO)) {
						phoneNumber.setLabel(Constantes.LABEL_SERVICO);
						phoneNumber.setRel(null);
					} else {
						final String operadoraTipo = this.obterOperadora(ddd.concat(telefone));
						this.corrigeLabelTelefoneCelular(phoneNumber, telefone, label, operadoraTipo);
						this.corrigeLabelTelefoneFixo(phoneNumber, telefone, label, operadoraTipo);
					}

					if (this.isTelefoneServico(telefone)) {
						// Telefone de serviço não tem DDD. Ex.: 4002-0022
						// (Bradesco)
						phoneNumber.setPhoneNumber(this.formatarTelefone(telefone));
					} else if (this.isDdd9Digitos(ddd) && this.isTelefoneCelular(telefone) && telefone.length() == 8) {
						// Acrescenta nono dígito
						telefone = "9" + telefone;
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					} else if (!beforeSanitize.equals(this.formatarTelefone(zero, ddd, telefone))) {
						// Não altera o que ja está correto
						phoneNumber.setPhoneNumber(this.formatarTelefone(zero, ddd, telefone));
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_RAMAL).matcher(afterSanitize)).find()) {

					// Telefone
					// 9363608

					telefone = matcher.group(1);

					// Tratamento de Rel e Label
					if (!this.isLabel(label, Constantes.LABEL_RAMAL)) {
						phoneNumber.setLabel(Constantes.LABEL_RAMAL);
						phoneNumber.setRel(null);
					}

					if (!beforeSanitize.equals(telefone)) {
						phoneNumber.setPhoneNumber(telefone);
					}

				} else if ((matcher = Pattern.compile(Constantes.REGEX_SERVICO).matcher(afterSanitize)).find()) {

					// Serviço
					// *144

					// Tratamento de Rel e Label
					if (!this.isLabel(label, Constantes.LABEL_SERVICO)) {
						phoneNumber.setLabel(Constantes.LABEL_SERVICO);
						phoneNumber.setRel(null);
					}

				} else {

					info = new StringBuilder();
					info.append("Erro de formato desconhecido no telefone do Contato ");
					info.append(this.getNames().getFullName());
					info.append("\t");
					info.append("\"");
					info.append(beforeSanitize);
					info.append("\"");
					LOGGER.severe(info.toString());

				}

				// Mudanças realizadas
				if (beforeSanitize != null && !beforeSanitize.equals(phoneNumber.getPhoneNumber())) {
					info = new StringBuilder();
					info.append("Alterando Número do Contato ");
					info.append(this.getNames().getFullName());
					info.append(" de ");
					info.append("\"");
					info.append(beforeSanitize);
					info.append("\"");
					info.append(" para ");
					info.append("\"");
					info.append(phoneNumber.getPhoneNumber());
					info.append("\"");
					LOGGER.info(info.toString());

					this.setChanged(true);
				}
				if (label != null && !this.isLabel(label, phoneNumber.getLabel())) {
					info = new StringBuilder();
					info.append("Alterando Label do Contato ");
					info.append(this.getNames().getFullName());
					info.append(" de ");
					info.append("\"");
					info.append(label);
					info.append("\"");
					info.append(" para ");
					info.append("\"");
					info.append(phoneNumber.getLabel());
					info.append("\"");
					LOGGER.info(info.toString());

					this.setChanged(true);
				}
				if (rel != null && phoneNumber.getRel() != null && !rel.equals(phoneNumber.getRel())) {
					info = new StringBuilder();
					info.append("Alterando Rel do Contato ");
					info.append(this.getNames().getFullName());
					info.append(" de ");
					info.append("\"");
					info.append(this.getPhoneNumberRel(rel));
					info.append("\"");
					info.append(" para ");
					info.append("\"");
					info.append(this.getPhoneNumberRel(phoneNumber.getRel()));
					info.append("\"");
					LOGGER.info(info.toString());

					this.setChanged(true);
				}

				if (phoneNumber.getLabel() == null) {
					info = new StringBuilder();
					info.append("Copiando Rel para Label do Contato ");
					info.append(this.getNames().getFullName());
					info.append("\t");
					info.append("\"");
					info.append(this.getPhoneNumberRel(phoneNumber.getRel()));
					info.append("\"");
					info.append("\t");
					info.append("\"");
					info.append(phoneNumber.getPhoneNumber());
					info.append("\"");
					LOGGER.info(info.toString());

					phoneNumber.setLabel(this.getPhoneNumberRel(phoneNumber.getRel()));
					phoneNumber.setRel(null);
					this.setChanged(true);
				}

				// Verificando números primários
				if (isPrimary && !this.isVivo(phoneNumber) && this.getEntry().getPhoneNumbers().size() > 1) {
					phoneNumber.setPrimary(false);
					info = new StringBuilder();
					info.append("Removendo telefones que não são da VIVO como primário do Contato ");
					info.append(this.getNames().getFullName());
					LOGGER.info(info.toString());

					this.setChanged(true);
				} else if (!isPrimary && this.isVivo(phoneNumber) && this.getEntry().getPhoneNumbers().size() == 1) {
					phoneNumber.setPrimary(true);
					info = new StringBuilder();
					info.append("Colocando telefone da VIVO como primário do Contato ");
					info.append(this.getNames().getFullName());
					LOGGER.info(info.toString());

					this.setChanged(true);
				} else if (!isPrimary && this.isTim(phoneNumber) && this.getEntry().getPhoneNumbers().size() == 1) {
					phoneNumber.setPrimary(true);
					info = new StringBuilder();
					info.append("Colocando telefone da TIM como primário do Contato ");
					info.append(this.getNames().getFullName());
					LOGGER.info(info.toString());

					this.setChanged(true);
				} else if (!isPrimary && this.getEntry().getPhoneNumbers().size() == 1) {
					phoneNumber.setPrimary(true);
					info = new StringBuilder();
					info.append("Colocando telefone único como primário do Contato ");
					info.append(this.getNames().getFullName());
					LOGGER.info(info.toString());

					this.setChanged(true);
				}

			} // Fim For

			// Excluir números marcados como duplicados
			for (final PhoneNumber phoneNumberRepetido : listToDelete) {
				this.getEntry().getPhoneNumbers().remove(phoneNumberRepetido);
				info = new StringBuilder();
				info.append("Excluindo telefone duplicado do Contato ");
				info.append(this.getNames().getFullName());
				info.append("\t");
				info.append("\"");
				info.append(phoneNumberRepetido.getPhoneNumber());
				info.append("\"");
				LOGGER.info(info.toString());

				this.setChanged(true);
			}
		}
	}

}
