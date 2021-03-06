package org.oobium.framework.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.oobium.app.persist.PersistServicesTests;
import org.oobium.build.esp.EjsCompilerTests;
import org.oobium.build.esp.EspCompilerTests;
import org.oobium.build.views.dom.html_elements.InputTests;
import org.oobium.build.workspace.BundleTests;
import org.oobium.build.workspace.ModuleTests;
import org.oobium.console.ParametersTests;
import org.oobium.mailer.MailerTests;
import org.oobium.persist.ModelTests;
import org.oobium.persist.SessionCacheTests;
import org.oobium.persist.ValidatorTests;
import org.oobium.persist.db.FindAllTests;
import org.oobium.persist.db.FindTests;
import org.oobium.persist.db.PaginatorTests;
import org.oobium.persist.db.internal.DbPersistorUpdateTests;
import org.oobium.persist.db.internal.QueryBuilderTests;
import org.oobium.utils.CharStreamUtilsTests;
import org.oobium.utils.FileUtilsTests;
import org.oobium.utils.SqlUtilsTests;
import org.oobium.utils.StringUtilsTests;
import org.oobium.utils.coercion.TypeCoercerTests;
import org.oobium.utils.json.JsonUtilsTests;

@RunWith(Suite.class)
@SuiteClasses({
	// org.oobium.build.tests
	EjsCompilerTests.class,
	EspCompilerTests.class,
	InputTests.class,
	BundleTests.class,
	ModuleTests.class,
	// org.oobium.console.tests
	ParametersTests.class,
	// org.oobium.mailer.tests
	MailerTests.class,
	// org.oobium.persist.db.tests
	SessionCacheTests.class,
	FindAllTests.class,
	FindTests.class,
	DbPersistorUpdateTests.class,
	QueryBuilderTests.class,
	PaginatorTests.class,
	// org.oobium.persist.tests
	ModelTests.class,
	PersistServicesTests.class,
	ValidatorTests.class,
	// org.oobium.utils.tests
	TypeCoercerTests.class,
	JsonUtilsTests.class,
	CharStreamUtilsTests.class,
	FileUtilsTests.class,
	SqlUtilsTests.class,
	StringUtilsTests.class
})
public class OobiumTestSuite {

}
