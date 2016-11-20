package gdata.util;

import gdata.Constantes;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;

public class Util {

	public static Util getInstance() {
		return new Util();
	}

	private boolean isSigla(final String str) {
		for (final String sigla : Constantes.siglas) {
			if (str.equalsIgnoreCase(sigla)) {
				return true;
			}
		}
		return false;
	}

	public String sanitize(final String str) {
		String afterSanitize = "";
		final String beforeSanitize = str.replaceAll(Constantes.REGEX_PONTO, " ")
				.replaceAll(Constantes.REGEX_CARACTERES_SANITIZAVEIS, "")
				.replaceAll(Constantes.REGEX_DOIS_ESPACOS, " ");

		final String[] palavras = beforeSanitize.split(Constantes.REGEX_ESPACO);

		for (int i = 0; i < palavras.length; i++) {
			if (palavras[i].length() > 3 && !this.isSigla(palavras[i])) {
				afterSanitize = afterSanitize.concat(WordUtils.capitalizeFully(palavras[i]));
			} else if (!this.isSigla(palavras[i])) {
				if (palavras[i].equalsIgnoreCase("e") || palavras[i].equalsIgnoreCase("da")
						|| palavras[i].equalsIgnoreCase("de") || palavras[i].equalsIgnoreCase("do")
						|| palavras[i].equalsIgnoreCase("dos") || palavras[i].equalsIgnoreCase("das")) {
					afterSanitize = afterSanitize.concat(palavras[i].toLowerCase());
				} else {
					afterSanitize = afterSanitize.concat(palavras[i]);
				}
			} else {
				afterSanitize = afterSanitize.concat(palavras[i].toUpperCase());
			}
			afterSanitize = afterSanitize.concat(" ");
		}

		return afterSanitize.trim();
	}

	public static String getExtension(final String str) {
		final Matcher matcher = Pattern.compile(Constantes.REGEX_EXTENSAO).matcher(str);
		String retorno = null;
		if (matcher.find() && matcher.groupCount() > 1) {
			retorno = matcher.group(1);
		}
		return retorno;
	}

	public String obterChavePorValorMap(final Map<String, String> hashMap, final String value) {
		for (final Map.Entry<String, String> e : hashMap.entrySet()) {
			final String key = e.getKey();
			if (e.getValue().equalsIgnoreCase(value)) {
				return key;
			}
		}
		return null;
	}

}
