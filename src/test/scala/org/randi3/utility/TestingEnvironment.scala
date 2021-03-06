package org.randi3.utility

import scala.slick.session.Database
import scala.slick.session.Database.threadLocalSession
import org.randi3.schema.DatabaseSchema._
import org.apache.commons.math3.random.MersenneTwister
import org.randi3.model._
import scala.collection.mutable.ListBuffer
import java.util.{ Properties, Locale, Date }
import org.randi3.randomization.RandomizationPluginManagerComponent
import org.randi3.dao._
import org.joda.time.LocalDate
import org.randi3.service.{ TrialSiteServiceComponent, TrialServiceComponent, UserServiceComponent }
import org.randi3.configuration.{ ConfigurationServiceComponent, ConfigurationValues, ConfigurationSchema }
import org.randi3.schema.DatabaseSchema
import org.randi3.schema.LiquibaseUtil
import scala.slick.driver.ExtendedProfile
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }

class TestingEnvironment extends RandomizationPluginManagerComponent with DaoComponent with AuditDaoComponent with CriterionDaoComponent with TreatmentArmDaoComponent with TrialSubjectDaoComponent with TrialSiteDaoComponent with TrialRightDaoComponent with TrialDaoComponent with UserDaoComponent with SecurityComponent with I18NComponent with RandomizationMethodDaoComponent with TrialSiteServiceComponent with UtilityDBComponent with UtilityMailComponent with MailSenderComponent with TrialServiceComponent with UserServiceComponent with ConfigurationServiceComponent {

  val properties = new Properties()

  properties.load(this.getClass.getClassLoader.getResourceAsStream("test.properties"))

  val databaseTuple: (Database, ExtendedProfile) = getDatabasePostgreSQL(properties.getProperty("testDatabaseName"), properties.getProperty("testDatabaseUser"), properties.getProperty("testDatabasePassword"))

  try {
    ConfigurationSchema.createDatabase
  } catch {
    case e: Exception =>
  }

  lazy val configurationService = new ConfigurationService

  configurationService.saveConfigurationEntry(ConfigurationValues.PLUGIN_PATH.toString, properties.getProperty("pluginPath"))

  lazy val database = databaseTuple._1

  lazy val driver = databaseTuple._2

  lazy val schema: DatabaseSchema = org.randi3.schema.DatabaseSchema.schema(driver)

  //LiquibaseUtil.getLiquibaseObject(database).dropAll()

  LiquibaseUtil.updateDatabase(database)

  lazy val auditDao = new AuditDao
  lazy val randomizationMethodDao = new RandomizationMethodDao
  lazy val trialDao = new TrialDao
  lazy val treatmentArmDao = new TreatmentArmDao
  lazy val trialSubjectDao = new TrialSubjectDao
  lazy val trialSiteDao = new TrialSiteDao
  lazy val trialRightDao = new TrialRightDao
  lazy val userDao = new UserDao
  lazy val criterionDao = new CriterionDao
  lazy val randomizationPluginManager = new RandomizationPluginManager
  lazy val i18n = I18N()

  lazy val securityUtility = new SecurityUtility {

    var user = User(Int.MinValue, 0, "validName", "validPassword", "valid@mail.de", "validFirst", "validLastName", "123456", TrialSite(Int.MinValue, 0, "validName", "validCountry", "validStreet", "validPostCode", "validCity", "validPassword", true).toOption.get, Set(), true, true, Locale.GERMAN, true, 0, None, None).toOption.get

    def currentUser: Option[User] = Some(user)
  }

  lazy val utilityDB = new UtilityDB
  lazy val utilityMail = new UtilityMail

  lazy val mailSender = new MailSender("localhost", "25", false, "username", "password", false, "mail@example.com") {
    override def sendMessage(to: String, cc: String, bcc: String, subject: String, content: String) {}
  }

  lazy val trialSiteService = new TrialSiteService
  lazy val trialService = new TrialService
  lazy val userService = new UserService

  val random = new MersenneTwister

  def trialName = "trial" + random.nextLong()

  def trialAbbreviation = "trial" + random.nextLong()

  def userName = "user" + random.nextLong()

  def treatmentArmName = "treatment" + random.nextLong()

  def trialSiteName = "trialSite" + random.nextLong()

  def country = "country"

  def street = "street"

  def postCode = "123456"

  def city = "city"

  def password = "validPassord"

  val randomizationPlugin = randomizationPluginManager.getPlugin("org.randi3.randomization.CompleteRandomization").get

  def createTrialSite = TrialSite(Int.MinValue, 0, trialSiteName, country, street, postCode, city, password, true).toOption.get

  def createTreatmentArm = TreatmentArm(Int.MinValue, 0, treatmentArmName, "description", new ListBuffer[TrialSubject](), 100).toOption.get

  def createTreatmentArms(count: Int) = {
    val result = new ListBuffer[TreatmentArm]()
    (1 to count).foreach {
      _ => result.append(createTreatmentArm)
    }
    result.toList
  }

  def randomMethod = randomizationPlugin.randomizationMethod(new MersenneTwister, null, Nil)

  def createTrial = Trial(Int.MinValue, 0, trialName, trialAbbreviation, "description", new LocalDate(), (new LocalDate()).plusYears(5), TrialStatus.ACTIVE, createTreatmentArms(2), Nil, List(createTrialSiteDB), Some(randomMethod.toOption.get), Map(), TrialSubjectIdentificationCreationType.TRIAL_ARM_COUNTER, false, false, false).toEither match {
    case Left(x) => throw new RuntimeException(x.toString())
    case Right(x) => x
  }

  def createUser = User(username = userName, password = "password", email = "abc@def.de", firstName = "firstName", lastName = "lastName", phoneNumber = "123456", site = createTrialSite, rights = Set()).toOption.get

  def createTrialDB = trialDao.get(trialDao.create(createTrial).toOption.get).toOption.get.get

  def createTreatmentArmDB = createTrialDB.treatmentArms(0)

  def createTrialSiteDB = trialSiteDao.get(trialSiteDao.create(createTrialSite).toOption.get).toOption.get.get

  def createUserDB = userDao.get(userDao.create(createUser.copy(site = createTrialSiteDB)).toOption.get).toOption.get.get

}

object TestingEnvironment extends TestingEnvironment {

}
