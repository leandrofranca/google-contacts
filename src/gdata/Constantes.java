package gdata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constantes {

	public static final String APPLICATION_NAME = "Contacts-API";

	public static final boolean MOCK = !true;

	public static final boolean VERIFICAR_OPERADORA = true;

	public static final String REGEX_PATTERN_CONSULTA_OPERADORA = "<h2><span class='azul' >A Operadora é </span> <span class='verde'>: ([^-]+)-([^<]+)</span></h2>";

	public static final String TIM = "TIM";
	public static final String VIVO = "VIVO";

	public static final String URL_CONTATOS = "https://www.google.com/m8/feeds/contacts/default/full";
	public static final String URL_CONSULTA_OPERADORA = "https://www.qual-operadora.net/#fb-root";
	public static final String URL_GRUPOS = "https://www.google.com/m8/feeds/groups/default/full";
	public static final String URL_BATCH_CONTATOS = "https://www.google.com/m8/feeds/contacts/default/full/batch";

	public static final String GRUPO_MY_CONTACTS = "System Group: My Contacts";
	public static final String GRUPO_DESCONHECIDOS = "Desconhecidos";

	public static final String PREFIXO_POS_CORRECAO = "Corrigir ";

	public static final String REGEX_CARACTERES_INVALIDOS_TELEFONE = "[^0-9*#\\+]";
	public static final String REGEX_DDD_TELEFONE = "^((\\d{2})(9?\\d{8}))$";
	public static final String REGEX_DDI_DDD_TELEFONE = "^(\\+?(\\d{3})(\\d{8,9}))$";
	public static final String REGEX_DDI55_DDD_TELEFONE = "^(\\+?(55)(\\d{2})(9?\\d{8}))$";
	public static final String REGEX_RAMAL = "^(\\d{7})$";
	public static final String REGEX_SERVICO = "^([*#+]?\\d{3,5}#?)$";
	public static final String REGEX_TELEFONE = "^(9?\\d{8})$";
	public static final String REGEX_ZERO_DDD_TELEFONE = "^((0)(\\d{2})(9?\\d{8}))$";
	public static final String ZERO_OITOCENTOS = "0800";
	public static final String REGEX_ZERO_OPERADORA_DDD_TELEFONE = "^((0)(\\d{2})(\\d{2})(9?\\d{8}))$";

	public static final String REGEX_EXTENSAO = "\\.(\\w+)$";
	public static final String REGEX_CARACTERES_SANITIZAVEIS = "[^\\p{L}\\p{Nd}\\s\\?\\,\\-\\.]";
	public static final String REGEX_DOIS_ESPACOS = "\\s\\s";
	public static final String REGEX_PONTO = "\\.";
	public static final String REGEX_ESPACO = "\\s";
	public static final String REGEX_SANITIZE_EMAIL = "[^a-z0-9\\_\\.\\-\\@\\+]";

	public static final String LABEL_CELULAR = "Celular";
	public static final String LABEL_FIXO = "Fixo";
	public static final String LABEL_GRATUITO = "Gratuito";
	public static final String LABEL_INTERNACIONAL = "Internacional";
	public static final String LABEL_SERVICO = "Serviço";
	public static final String LABEL_RAMAL = "Ramal";

	public static final List<String> EMAILS_PESSOAIS = Arrays
			.asList(new String[] { "@bol.com.br", "@gmail.com", "@googlegroups.com", "@googlemail.com",
					"@groups.facebook.com", "@hotmail.com", "@hotmail.de", "@ibest.com.br", "@ig.com.br", "@live.com",
					"@misseroni.com", "@msgamestudios.com", "@msn.com", "@oi.com.br", "@outlook.com", "@pop.com.br",
					"@si.unifacs.br", "@takenami.com.br", "@tanajura.com.br", "@terra.com.br", "@unifacs.br",
					"@unifacs.edu.br", "@uol.com.br", "@vergasta.com.br", "@walla.com", "@yahoo.com", "@yahoo.com.br",
					"@rasea.org", "@fespinheira.com", "@flaviocampos.com", "@bot.talk.google.com",
					"@diegocardoso.com.br", "@hotmail.com.br", "@leandrorangel.com.br", "@phydias.com.br" });

	public static final String BATCH_DELETE = "delete";

	public static final String BATCH_UPDATE = "update";

	public static final Integer[] ddd9Digitos = new Integer[] { 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 24, 27, 28,
			31, 32, 33, 34, 35, 37, 38, 41, 42, 43, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 61, 62, 63, 64, 65, 66, 67,
			68, 69, 71, 73, 74, 75, 77, 79, 81, 82, 83, 84, 85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 96, 97, 98, 99 };

	public static final Integer[] dddNot9Digitos = new Integer[] {};

	public static final String[] siglas = new String[] { "IPRAJ", "SKY", "UNIFACS", "TAO", "BNB", "ETL", "TI", "BV",
			"BB", "ITIL", "PMP" };

	public static Map<String, String> mimeTypes = new HashMap<String, String>();

	static {
		mimeTypes.put("3gpp", "video/3gpp");
		mimeTypes.put("avi", "video/avi");
		mimeTypes.put("bmp", "image/bmp");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpeg", "image/jpeg");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("m4v", "video/mp4");
		mimeTypes.put("mp4", "video/mp4");
		mimeTypes.put("png", "image/png");
	}

	public static final String PADRAO_TELEFONE = "({0}{1}) {2}";

}
