package me.tecno.loader.descriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DescriptorCrawler {
	public static final String JAR_EXTENSION = "jar";
	public static final String DESCRIPTOR_TAG = "deeploader";
	
	private final File pluginsFolder;
	
	public List<File> getJarFiles() {
		return Arrays.asList(getPluginsFolder().listFiles((d, name) -> 
					Files.getFileExtension(name).equals(JAR_EXTENSION)));
	}
	
	protected Optional<InputStream> transposePluginDescriptor(ZipFile zipFile) 
			throws ZipException, IOException {
		Optional<ZipEntry> entry = 
				Optional.ofNullable(zipFile.getEntry("plugin.yml"));
		
		return entry.map((ent) -> {
			try {
				return zipFile.getInputStream(ent);
			} catch(IOException exc) {
				throw new RuntimeException(exc);
			}
		});
	}
	
	protected Optional<String> locationFromDescriptorFile(InputStream in) 
			throws FileNotFoundException, IOException, InvalidConfigurationException {
		YamlConfiguration y = new YamlConfiguration();
		
		try(BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
			y.load(r);
		}
			
		return Optional.ofNullable(y.getString(DESCRIPTOR_TAG));
	}
	
	public Map<File, String> loadLocationDescriptors() {
		try {
			Map<File, String> result = new HashMap<>();
			
			for(File jarFile : getJarFiles()) {
				try(ZipFile zipFile = new ZipFile(jarFile)) {
					Optional<InputStream> dFile = transposePluginDescriptor(zipFile);
					if(!dFile.isPresent()) continue;
					
					InputStream descriptorFile = dFile.get();
					
					Optional<String> dTag = locationFromDescriptorFile(descriptorFile);
					if(!dTag.isPresent()) continue;
					
					result.put(jarFile, dTag.get());
				}
			}
			
			return result;
		} catch(IOException | InvalidConfigurationException exc) {
			throw new RuntimeException(exc);
		}
	}
}
