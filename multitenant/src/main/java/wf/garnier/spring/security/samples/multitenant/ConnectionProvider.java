package wf.garnier.spring.security.samples.multitenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
class ConnectionProvider implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

	@Autowired
	DataSource dataSource;

	@Override
	public Connection getAnyConnection() throws SQLException {
		return getConnection("PUBLIC");
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		var connection = dataSource.getConnection();
		connection.setSchema(tenantIdentifier);
		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		connection.setSchema(tenantIdentifier);
		connection.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	// @Override
	// public Connection getConnection(String schema) throws SQLException {
	// Connection connection = dataSource.getConnection();
	// connection.setSchema(schema);
	// return connection;
	// }

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
	}

	@Override
	public boolean isUnwrappableAs(Class<?> unwrapType) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		return null;
	}

}