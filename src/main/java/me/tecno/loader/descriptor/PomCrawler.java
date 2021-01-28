package me.tecno.loader.descriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipFile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PomCrawler implements Clearable {
	public static final String TEMP_PREFIX = "deeploader";
	
	private final File jarFile;
	private final String pomLocation;
	
	@Getter(AccessLevel.PROTECTED)
	private List<File> fileCache = new ArrayList<>();
	
	public Optional<File> loadPom() {
		try(ZipFile zipFile = new ZipFile(getJarFile())) {
			return Optional.ofNullable(
					zipFile.getEntry(getPomLocation()))
				.map((ent) -> {
					try {
						return writeToTempFile(zipFile.getInputStream(ent));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public File writeToTempFile(InputStream is) throws IOException {
		File f = File.createTempFile(TEMP_PREFIX, getJarFile().getName() + ".pom");
		f.deleteOnExit();
		
	    byte[] buffer = new byte[is.available()];
	    is.read(buffer);
	    
	    try(OutputStream outStream = new FileOutputStream(f)) {
	    	outStream.write(buffer);
	    }
	    
	    return f;
	}

	@Override
	public void clear() {
		Iterator<File> it = getFileCache().iterator();
		while(it.hasNext()) {
			it.next().delete();
			it.remove();
		}
	}
}
