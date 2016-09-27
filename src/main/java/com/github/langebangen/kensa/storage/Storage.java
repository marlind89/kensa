package com.github.langebangen.kensa.storage;

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
	public Storage()
		throws IOException
	{
		Properties properties = new Properties();
		properties.load(new FileInputStream("database.conf"));
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		url = properties.getProperty("url");
	}

	public Connection getConnection()
		throws SQLException
	{
		return DriverManager.getConnection(url, username, password);
	}

}
