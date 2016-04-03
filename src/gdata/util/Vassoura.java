package gdata.util;

import gdata.picasa.Photo;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Vassoura {

	private static final Logger LOGGER = Logger.getLogger(Vassoura.class.getName());

	private static List<Photo> listPhotos = new ArrayList<Photo>();

	public void start(final String path, final boolean changeFiles) {
		try {
			final Path startPath = Paths.get(path);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {

					final String pasta = path.getParent().toString();
					final String arquivo = path.getFileName().toString();
					Date dateFromFile = null;

					final Matcher matcherPhoto = Pattern.compile("^\\w{3}_(\\d{8})_(\\d{6})").matcher(arquivo);
					final Matcher matcherScreenshot = Pattern.compile("^Screenshot_(\\d{4}-\\d{2}-\\d{2})-(\\d{2}-\\d{2}-\\d{2})").matcher(arquivo);
					final boolean isPhoto = matcherPhoto.find();
					final boolean isScreenshot = matcherScreenshot.find();

					if (!isPhoto && !isScreenshot) {
						final StringBuilder error = new StringBuilder();
						error.append("Padrão não reconhecido: ");
						error.append(pasta);
						error.append("\\");
						error.append(arquivo);
						LOGGER.severe(error.toString());

						return FileVisitResult.CONTINUE;
					} else {
						final Matcher matcher = isPhoto ? matcherPhoto : matcherScreenshot;
						final String anoMesDia = matcher.group(1).replace("-", "");
						final String horaMinutoSegundo = matcher.group(2).replace("-", "");
						final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
						try {
							dateFromFile = formatter.parse(anoMesDia + "_" + horaMinutoSegundo);
						} catch (final ParseException e) {
							final StringBuilder error = new StringBuilder();
							error.append(e.getMessage());
							LOGGER.severe(error.toString());

							return FileVisitResult.CONTINUE;
						}
					}

					try {

						if (dateFromFile != null) {
							final Photo p = new Photo();
							p.setCreatedDate(dateFromFile);
							p.setPath(pasta);
							p.setTitle(arquivo);
							listPhotos.add(p);

							if (changeFiles) {
								// Alterar datas do arquivo
								Vassoura.this.changeFileDate(path, dateFromFile);

								// Alterar metadata
								Vassoura.this.changeMetadata(path, dateFromFile);
							}
						}

					} catch (final ImageProcessingException e) {
						final StringBuilder error = new StringBuilder();
						error.append(e.getMessage());
						LOGGER.severe(error.toString());
					}
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (final IOException e) {
			final StringBuilder error = new StringBuilder();
			error.append(e.getMessage());
			LOGGER.severe(error.toString());
		}
	}

	public static void main(final String[] args) throws IOException {
		final Vassoura varrer = new Vassoura();
		varrer.start("D:\\Camera Uploads", false);
		varrer.deleteThumbs();
		varrer.getListPhotos();
	}

	public List<Photo> getListPhotos() {
		return listPhotos;
	}

	public void changeFileDate(final Path path, final Date dateFromFile) throws IOException {
		final BasicFileAttributeView attributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
		final FileTime time = FileTime.fromMillis(dateFromFile.getTime());
		attributes.setTimes(time, time, time);
	}

	public void changeMetadata(final Path path, final Date dateFromFile) throws ImageProcessingException, IOException {
		final Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
		final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

		if (directory != null) {
			final StringBuilder info = new StringBuilder();
			info.append("Alterando arquivo: ");
			info.append(path.toAbsolutePath().toString());
			LOGGER.info(info.toString());

			directory.setDate(ExifSubIFDDirectory.TAG_DATETIME, dateFromFile);
			directory.setDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED, dateFromFile);
			directory.setDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, dateFromFile);
		}
	}

	public void deleteThumbs() throws IOException {
		if (listPhotos.size() == 0) {
			return;
		} else {
			for (final Photo photo : listPhotos) {
				final String arquivo = photo.getTitle();
				if (arquivo.equalsIgnoreCase("Thumbs.db")) {
					this.deleteFile(Paths.get(photo.getPath() + "\\" + arquivo));
				}
			}
		}
	}

	public boolean deleteFile(final Path dir) throws IOException {
		if (Files.deleteIfExists(dir)) {
			final StringBuilder info = new StringBuilder();
			info.append("Excluindo arquivo: ");
			info.append(dir.toAbsolutePath().toString());
			LOGGER.info(info.toString());

			return true;
		}
		return false;
	}

}
