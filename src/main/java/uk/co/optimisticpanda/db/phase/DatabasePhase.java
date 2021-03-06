package uk.co.optimisticpanda.db.phase;

import org.springframework.beans.factory.annotation.Autowired;

import uk.co.optimisticpanda.conf.Phase;
import uk.co.optimisticpanda.db.conf.DatabaseConnectionDefinition;
import uk.co.optimisticpanda.db.conf.JdbcConnectionProvider;
import uk.co.optimisticpanda.db.conf.JdbcConnectionProvider.JdbcConnection;
import uk.co.optimisticpanda.util.ReflectionUtil.IgnoreForSerializing;

import com.google.common.base.Objects;

/**
 * A parent class for database phases. Provides a {@link VersionProvider} for a named database connection.
 */
public abstract class DatabasePhase extends Phase {

	private String connection;
	private transient JdbcConnection jdbcConnection;
	
	@Autowired
	public void setJdbcConnection(JdbcConnectionProvider jdbcProvider) {
		this.jdbcConnection = jdbcProvider.getJdbcConnection(connection);
	}
	
	public void setConnection(String connectionName) {
		this.connection= connectionName;
	}

	public String getConnection() {
		return connection;
	}

	@IgnoreForSerializing
	public JdbcConnection getJdbcConnection(){
		return jdbcConnection;
	}
	
	@IgnoreForSerializing
	public DatabaseConnectionDefinition getConnectionDefinition(){
		return jdbcConnection.getDefinition();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), connection);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DatabasePhase other = (DatabasePhase) obj;
		return super.equals(obj) //
				&& Objects.equal(this.connection, other.connection);
		}
}
