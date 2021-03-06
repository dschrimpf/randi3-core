package org.randi3.schema

import java.sql.{SQLException, Connection}
import liquibase.Liquibase
import org.randi3.dao.DaoComponent
import liquibase.database.{DatabaseFactory, Database}
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.database.jvm.JdbcConnection
import scala.slick.session

object LiquibaseUtil {

  def updateDatabase(database: session.Database) {
    updateDatabase(database, "db/db.changelog-master.xml")
  }

  def updateDatabase(database: session.Database, changelog: String) {
    updateDatabase(database, changelog, this.getClass.getClassLoader)
  }


  def updateDatabase(database: session.Database, changelog: String, classloader: ClassLoader) {
    val liquibase = getLiquibaseObject(database, changelog, classloader)

    liquibase.update(null)
  }

  def getLiquibaseObject(database: session.Database): Liquibase = {
   getLiquibaseObject(database, "db/db.changelog-master.xml", this.getClass.getClassLoader)
  }

  def getLiquibaseObject(database: session.Database, classloader: ClassLoader): Liquibase = {
    getLiquibaseObject(database, "db/db.changelog-master.xml", classloader)
  }

  def getLiquibaseObject(database: session.Database, changelog: String, classloader: ClassLoader): Liquibase = {
    val c: Connection = database.createSession().conn
    val databaseLiquibase: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c))

     new Liquibase(changelog, new ClassLoaderResourceAccessor(classloader), databaseLiquibase)
  }
}
