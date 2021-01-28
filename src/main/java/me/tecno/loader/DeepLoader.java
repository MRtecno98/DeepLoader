package me.tecno.loader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

import me.tecno.loader.descriptor.ClearablePool;
import me.tecno.loader.descriptor.DescriptorCrawler;
import me.tecno.loader.descriptor.PomCrawler;

public class DeepLoader extends JavaPlugin {
	
	@Override
	public void onLoad() {
		getDataFolder().mkdirs();
		
		getLogger().info("Analyzing plugins");
		
		Map<File, String> descs = new DescriptorCrawler(
				getDataFolder().getParentFile()).loadLocationDescriptors();
		
		getLogger().info(String.format(
				"Analyzed %d plugins which requested dependencies", 
				descs.size()));
		
		ClearablePool clearables = new ClearablePool();
		List<File> poms = new ArrayList<>();
		
		getLogger().info("Crawling for POM files");
		
		descs.forEach((file, location) -> {
			PomCrawler crawler = new PomCrawler(file, location);
			clearables.add(crawler);
			
			Optional<File> pom = crawler.loadPom();
			
			if(!pom.isPresent())
				getLogger().warning(String.format(
						"JAR %s requested dependencies for an unknown pom file \"%s\"", 
						file.getName(), location));
			else poms.add(pom.get());
		});
		
		Set<URL> urls = new HashSet<>();
		
		getLogger().info("Switching context ClassLoader");
		ClassLoader oldContext = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
		getLogger().info("Resolving dependencies");
		poms.forEach((pom) -> 
			urls.addAll(Arrays.asList(Maven.configureResolver()
					.withMavenCentralRepo(true)
					.fromClassloaderResource("settings.xml")
					.loadPomFromFile(pom)
					.importDependencies(ScopeType.COMPILE, ScopeType.RUNTIME)
					.resolve()
					.withTransitivity().as(URL.class))));
		
		Thread.currentThread().setContextClassLoader(oldContext);
		
		getLogger().info(String.format(
				"Succesfully loaded %d jar urls", urls.size()));
		
		urls.forEach(System.out::println);
		
		getLogger().info("Cleaning up");
		clearables.clearAll();
	}
	
	
}
