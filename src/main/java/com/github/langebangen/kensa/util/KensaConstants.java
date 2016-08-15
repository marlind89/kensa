package com.github.langebangen.kensa.util;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Constants used in Kensa project.
 *
 * @author Martin.
 */
public class KensaConstants
{
	public static String VERSION = getVersion();

	private static String getVersion()
	{
		// Try to get version number from pom.xml (available in Eclipse)
		try
		{
			String className = KensaConstants.class.getName();
			String classFileName = "/" + className.replace('.', '/') + ".class";
			URL classFileResource = KensaConstants.class.getResource(classFileName);
			if(classFileResource != null)
			{
				Path absolutePackagePath = Paths.get(classFileResource.toURI())
						.getParent();
				int packagePathSegments = className.length()
						- className.replace(".", "").length();

				// Remove package segments from path, plus two more levels
				// for "target/classes", which is the standard location for
				// classes in Eclipse.
				Path path = absolutePackagePath;
				for(int i = 0, segmentsToRemove = packagePathSegments + 2;
				    i < segmentsToRemove; i++)
				{
					path = path.getParent();
				}
				Path pom = path.resolve("pom.xml");
				try(InputStream is = Files.newInputStream(pom))
				{
					Document doc = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder().parse(is);
					doc.getDocumentElement().normalize();
					String version = (String) XPathFactory.newInstance()
							.newXPath().compile("/project/version")
							.evaluate(doc, XPathConstants.STRING);
					if(version != null)
					{
						version = version.trim();
						if(!version.isEmpty())
						{
							return version;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			// Ignore
		}

		// Try to get version number from maven properties in jar's META-INF
		try(InputStream is = KensaConstants.class
				.getResourceAsStream("/META-INF/maven/com.github.langebangen/kensa/pom.properties"))
		{
			if(is != null)
			{
				Properties p = new Properties();
				p.load(is);
				String version = p.getProperty("version", "").trim();
				if(!version.isEmpty())
				{
					return version;
				}
			}
		}
		catch(Exception e)
		{
			// Ignore
		}

		// Fallback to using Java API to get version from MANIFEST.MF
		String version = null;
		Package pkg = KensaConstants.class.getPackage();
		if(pkg != null)
		{
			version = pkg.getImplementationVersion();
			if(version == null)
			{
				version = pkg.getSpecificationVersion();
			}
		}
		version = version == null ? "unknown" : version.trim();

		return version;
	}
}
