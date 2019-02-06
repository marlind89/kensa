package com.github.langebangen.kensa.storage;

import com.github.langebangen.kensa.config.DatabaseConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Martin.
 */
@Singleton
public class Storage
{
	private final String url;
	private final String username;
	private final String password;

	@Inject
	public Storage(DatabaseConfig dbConfig)
	{
		username = dbConfig.username();
		password = dbConfig.password();
		url = dbConfig.url();
	}

	public Connection getConnection()
		throws SQLException
	{
		return DriverManager.getConnection(url, username, password);
	}

}
