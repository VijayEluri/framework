package org.oobium.persist.migrate.db;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.StringUtils.simpleName;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.build.gen.DbGenerator;
import org.oobium.build.model.ModelDefinition;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.SimpleDynClass;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.migrate.AbstractMigration;
import org.oobium.persist.migrate.MigrationService;
import org.oobium.persist.migrate.db.derby.embedded.DerbyEmbeddedMigrationService;
import org.oobium.persist.migrate.db.mysql.MySqlMigrationService;
import org.oobium.persist.migrate.db.postgresql.PostgreSqlMigrationService;
import org.oobium.utils.SqlUtils;

public class MigrateTester {

	private static int count; // test counter
	
	protected List<String> statements;
	protected MigrationService migrationService;

	protected void setup(int dbType) throws Exception {
		DynClasses.reset();
		
		statements = new ArrayList<String>();
		
		Statement statement = mock(Statement.class);
		when(statement.executeUpdate(anyString())).thenAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				String statement = (String) invocation.getArguments()[0];
				statements.add(statement);
				System.out.println(statement);
				return 1;
			}
		});
		
		Connection connection = mock(Connection.class);
		when(connection.createStatement()).thenReturn(statement);
		
		DbPersistService persistor = mock(DbPersistService.class);
		when(persistor.getConnection()).thenReturn(connection);
		
		switch(dbType) {
		case SqlUtils.DERBY:		migrationService = new DerbyEmbeddedMigrationService(); break;
		case SqlUtils.MYSQL:		migrationService = new MySqlMigrationService(); break;
		case SqlUtils.POSTGRESQL:	migrationService = new PostgreSqlMigrationService(); break;
		}
		migrationService.setPersistService(persistor);
	}
	
	protected String migrateUp(DynModel...models) throws Exception {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getSource(), DynClasses.getSiblings(models[i]));
		}
		DbGenerator gen = new DbGenerator("test" + (count++), "CreateDatabase", defs);
		gen.generate();
		Class<?> clazz = SimpleDynClass.getClass(gen.getFullName(), gen.getSource());
		System.out.println(gen.getSource());
		AbstractMigration mig = (AbstractMigration) clazz.newInstance();
		mig.setService(migrationService);
		mig.up();
		return join(statements, '\n');
	}

}
