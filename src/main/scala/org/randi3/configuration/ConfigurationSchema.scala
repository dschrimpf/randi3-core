package org.randi3.configuration

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.extended._


object ConfigurationSchema {

  val databaseURL = "jdbc:h2:file:configuration;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=100000"


  val ConfigurationProperties = new Table[(String, String)]("ConfigurationProperties") {
    def propertyName = column[String]("propertyName",O PrimaryKey, O NotNull)

    def propertyValue = column[String]("propertyValue", O NotNull)

    def * = propertyName ~ propertyValue
  }


  def createDatabase: (Database, ExtendedProfile) = {
    createDatabase(databaseURL)
  }

  def getDatabase: (Database, ExtendedProfile) = {
    getDatabase(databaseURL)
  }

  def createDatabase(databaseJDBC: String): (Database, ExtendedProfile) = {
    val db: Database = Database.forURL(databaseJDBC)
    import org.scalaquery.ql.extended.H2Driver.Implicit._

    db  withSession {
      ConfigurationProperties.ddl.create
    }

    (db, org.scalaquery.ql.extended.H2Driver)
  }


  def getDatabase(databaseJDBC: String): (Database, ExtendedProfile) = {
    val db: Database = Database.forURL(databaseJDBC)
    (db, org.scalaquery.ql.extended.H2Driver)
  }

}
