package com.amdatu.rti.bootstrap.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.amdatu.bootstrap.command.Command;
import org.amdatu.bootstrap.command.Description;
import org.amdatu.bootstrap.command.InstallResult;
import org.amdatu.bootstrap.command.InstallResult.Builder;
import org.amdatu.bootstrap.command.Parameters;
import org.amdatu.bootstrap.command.RunConfig;
import org.amdatu.bootstrap.command.Scope;
import org.amdatu.bootstrap.core.BootstrapPlugin;
import org.amdatu.bootstrap.services.Dependency;
import org.amdatu.bootstrap.services.DependencyBuilder;
import org.amdatu.bootstrap.services.Navigator;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Inject;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.osgi.framework.BundleContext;

@Component
public class RTIPlugin implements BootstrapPlugin {

	@Inject
	private volatile BundleContext m_bundleContext;

	@ServiceDependency
	private volatile Navigator m_navigator;

	@ServiceDependency
	private volatile DependencyBuilder m_dependencyBuilder;

	@Override
	public String getName() {
		return "rti";
	}

	interface RTIInstallArguments extends Parameters {

		@Description("include probes")
		boolean probes();

		@Description("include logging")
		boolean logging();
	}

	@Command(scope = Scope.PROJECT)
	@Description("Add Amdatu RTI build dependencies")
	public InstallResult install(RTIInstallArguments params) throws Exception {
		List<Dependency> dependencies = new ArrayList<>();
		installDefaults(dependencies);
		if (params.logging()) {
			installLoggingAPI(dependencies);
		}
		if (params.probes()) {
			installProbeAPI(dependencies);

		}
		return m_dependencyBuilder.addDependencies(dependencies);
	}

	private void installDefaults(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("osgi.core;version='[4.0.1,5)'", "osgi.cmpn;version='[4.3.0,5)'",
				"org.apache.felix.dependencymanager;version='[4.1.0,5)'",
				"org.apache.felix.http.servlet-api;version='[1.1.2,2)'",
				"org.amdatu.web.rest.jaxrs;version='[1.0.8,1.1)'", "org.amdatu.web.rest.doc;version='[1.2.2,1.3)'"));
	}

	private void installLoggingAPI(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("com.amdatu.rti.logging.slf4j;version=latest"));
	}

	private void installProbeAPI(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("com.amdatu.rti.probe.api;version=latest"));
	}

	interface RTIRunArguments extends Parameters {
		@Description("Location of run configuration")
		@RunConfig
		File runFile();

		@Description("include probes")
		boolean probes();

		@Description("include logging")
		boolean logging();

		@Description("include frontend logging")
		boolean frontendLogging();
	}

	@Command(scope = Scope.PROJECT)
	@Description("Add Amdatu RTI run dependencies")
	public InstallResult run(RTIRunArguments args) throws Exception {
		List<Dependency> dependencies = new ArrayList<>();
		runDefaults(dependencies);
		if (args.probes()) {
			runProbes(dependencies);
		}
		if (args.logging()) {
			runLogging(dependencies);
		}
		if (args.frontendLogging()) {
			runFrontendLogging(dependencies);
		}
		copyConfiguration();
		Builder builder = InstallResult.builder();
		builder.addResult(m_dependencyBuilder.addRunDependency(dependencies, args.runFile().toPath()));
		return builder.build();
	}

	private void runDefaults(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("com.amdatu.rti.infrastructure.resolver;version=latest",
				"org.amdatu.configurator.api;version='[1.0.1,1.1)'",
				"org.amdatu.configurator.properties;version='[1.0.1,1.1)'",
				"org.amdatu.scheduling.api;version='[1.0.1,1.1)'", "org.amdatu.scheduling.quartz;version='[1.0.2,2)'",
				"org.amdatu.security.tokenprovider.api;version='[1.0.2,1.1)'",
				"org.amdatu.security.tokenprovider.impl;version='[1.0.2,1.1)'",
				"org.amdatu.web.rest.jaxrs;version='[1.0.8,1.1)'", "org.amdatu.web.rest.doc;version='[1.2.2,1.3)'",
				"org.amdatu.web.rest.wink;version='[2.0.3,2.1)'", "org.apache.commons.lang3;version='[3.1.0,4)'",
				"org.apache.commons.collections;version='[3.2.1,4)'",
				"org.apache.felix.configadmin;version='[1.8.6,1.9)'",
				"org.apache.felix.dependencymanager;version='[4.1.0,5)'",
				"org.apache.felix.dependencymanager.runtime;version='[4.0.1,5)'",
				"org.apache.felix.eventadmin;version='[1.3.2,2)'",
				"org.apache.felix.http.servlet-api;version='[1.1.2,2)'",
				"org.apache.felix.http.api;version='[2.3.2,3)'", "org.apache.felix.http.jetty;version='[2.3.0,3)'",
				"org.apache.felix.http.whiteboard;version='[2.3.2,3)'",
				"org.apache.felix.metatype;version='[1.0.12,2)'", "org.apache.felix.log;version='[1.0.1,2)'",
				"com.fasterxml.jackson.core.jackson-annotations;version='[2.5.4,3)'",
				"com.fasterxml.jackson.core.jackson-core;version='[2.5.4,3)'",
				"com.fasterxml.jackson.core.jackson-databind;version='[2.5.4,3)'",
				"com.fasterxml.jackson.jaxrs.jackson-jaxrs-base;version='[2.3.0,3)'",
				"com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider;version='[2.3.0,3)'"));
	}

	private void runProbes(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("com.amdatu.rti.probe.api;version=latest",
				"com.amdatu.rti.probe.controller;version=latest", "com.amdatu.rti.probe.backend.kafka;version=latest"));
	}

	private void runLogging(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("com.amdatu.rti.logging.kafka;version=latest",
				"com.amdatu.rti.logging.slf4j;version=latest"));
	}

	private void runFrontendLogging(List<Dependency> dependencies) {
		dependencies.addAll(Dependency.fromStrings("com.amdatu.rti.logging.ws;version=latest",
				"org.atmosphere.runtime;version='[2.2.4,2.3)'"));
	}

	private void copyConfiguration() throws IOException {
		Path projectDir = m_navigator.getCurrentDir();
		Path confDir = projectDir.resolve("conf");
		Enumeration<?> entries = m_bundleContext.getBundle().findEntries("/conf", "*", true);
		while (entries.hasMoreElements()) {
			URL entry = (URL) entries.nextElement();
			if (entry.toExternalForm().endsWith("/")) {
				continue;
			}
			Path targetPath = confDir.resolve(Paths.get(entry.getFile().replace("/conf/", "")));
			try (InputStream in = entry.openStream()) {
				if (!targetPath.getParent().toFile().exists()) {
					targetPath.getParent().toFile().mkdirs();
				}
				Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
}
