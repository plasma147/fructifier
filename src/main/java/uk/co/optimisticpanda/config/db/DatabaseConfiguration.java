package uk.co.optimisticpanda.config.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.co.optimisticpanda.conf.RunningOrder;
import uk.co.optimisticpanda.config.db.apply.ScriptApplier;
import uk.co.optimisticpanda.runner.RegisteredExtensions.ConnectionRegistration;
import uk.co.optimisticpanda.runner.RegisteredExtensions.PhaseRegistration;
import uk.co.optimisticpanda.runner.RegisteredExtensions.RegisterExtension;
import uk.co.optimisticpanda.versioning.VersionProvider.VersionProviderFactory;
import uk.co.optimisticpanda.versioning.VersionProvider.VersionProviders;
import uk.co.optimisticpanda.versioning.db.JdbcProvider;

/**
 * The spring configuration for database functionality
 */
@Configuration
public class DatabaseConfiguration {

	@Autowired
	private RunningOrder runningOrder;

	/**
	 * Register this extension
	 * @formatter:off
	 */
	@Bean
	public RegisterExtension registerDatabaseExtension() {
		return new RegisterExtension()
				.connectionTypes(
						new ConnectionRegistration("database", DatabaseConnection.class)
						)
				.typeAdaptors(
						new SeperatorLocationTypeAdaptor()
						)
				.phaseTypes(
						new PhaseRegistration("database.incremental.phase", IncrementalDatabasePhase.class),
						new PhaseRegistration("database.single.script.phase", SingleScriptDatabasePhase.class)
						);
	}
	/* @formatter:off*/

	/**
	 * A bean that gets autowired into the {@link DatabasePhase}
	 */
	@Bean
	public VersionProviders databaseVersionProviders() {
		return new VersionProviders(databaseVersionProviderFactory());
	}
	
	@Bean
	public VersionProviderFactory databaseVersionProviderFactory() {
		return new DatabaseVersionProvider.DatabaseVersionProviderFactory();
	}
	@Bean
	public JdbcProvider jdbcProviders() {
		return new JdbcProvider(runningOrder.getConnections());
	}

	@Bean
	public ScriptApplier scriptApplier() {
		return new ScriptApplier();
	}

}
