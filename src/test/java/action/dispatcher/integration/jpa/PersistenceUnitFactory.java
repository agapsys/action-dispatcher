/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package action.dispatcher.integration.jpa;

import com.agapsys.jpa.PersistenceUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PersistenceUnitFactory  {
	// CLASS SCOPE =============================================================
	public static final String PERSISTENCE_UNIT_NAME = "default";
	public static final String PROPERTIES_FILE       = ".jpa-utils.properties";
	
	public static final String DEFAULT_JDBC_DRIVER       = "org.h2.Driver";
	public static final String DEFAULT_JDBC_URL          = "jdbc:h2:mem:";
	public static final String DEFAULT_JDBC_USER         = "sa";
	public static final String DEFAULT_JDBC_PASSWORD     = "sa";
	
	public static final String DEFAULT_HIBERNATE_DIALECT    = "org.hibernate.dialect.H2Dialect";
	public static final String DEFAULT_HIBERNATE_FLUSH_MODE = "COMMIT";
	public static final String DEFAULT_HIBERNATE_DDL        = "create"; // For test-only!!!
	
	private static PersistenceUnit singleton = null;
	public static PersistenceUnit getInstance() {
		if (singleton == null || !singleton.isOpen()) {
			
			File homeFolder = new File(System.getProperty("user.home"));
			File propFile = new File(homeFolder, PROPERTIES_FILE);
			
			Properties props = new Properties();
			props.setProperty("javax.persistence.jdbc.driver",   DEFAULT_JDBC_DRIVER);
			props.setProperty("javax.persistence.jdbc.url",      DEFAULT_JDBC_URL);
			props.setProperty("javax.persistence.jdbc.user",     DEFAULT_JDBC_USER);
			props.setProperty("javax.persistence.jdbc.password", DEFAULT_JDBC_PASSWORD);
			
			props.setProperty("org.hibernate.dialect",   DEFAULT_HIBERNATE_DIALECT);
			props.setProperty("org.hibernate.flushMode", DEFAULT_HIBERNATE_FLUSH_MODE);
			props.setProperty("hibernate.hbm2ddl.auto",  DEFAULT_HIBERNATE_DDL);
			
			if (propFile.exists()) {
				try (FileInputStream fis = new FileInputStream(propFile)) {
					props.load(fis);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				try (FileOutputStream fos = new FileOutputStream(propFile)) {
					props.store(fos, "");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			singleton = new PersistenceUnit(PERSISTENCE_UNIT_NAME, props);
		}
		
		return singleton;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private PersistenceUnitFactory() {}
	// =========================================================================
}
