package org.randi3.configuration

import org.junit.runner.RunWith
import org.randi3.utility.TestingEnvironment

import org.scalaquery.session.Database.threadLocalSession
import org.scalatest.matchers.MustMatchers
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}
import org.randi3.model.Trial
import org.apache.commons.math3.random.MersenneTwister

import ConfigurationSchema._
import org.scalatest.junit.JUnitRunner
import org.randi3.utility.TestingEnvironment._

@RunWith(classOf[JUnitRunner])
class ConfigurationServiceSpec extends FunSpec with MustMatchers with ShouldMatchers with BeforeAndAfter{


  import schema._

   before {
   //createDatabaseH2
  }

  describe("The ConfigurationService save method") {

    val databaseTuple = getDatabase
    val db = databaseTuple._1
    import databaseTuple._2.Implicit._

    val configurationService = new ConfigurationService

    it("should be able to create a configuration value if it doesn't exists") {
         configurationService.saveConfigurationEntry(ConfigurationValues.DB_USER.toString, "randi3").either match {
           case Left(failure) => fail(failure)
           case Right(b) => b must be(true)
         }

      configurationService.saveConfigurationEntry(ConfigurationValues.DB_PASSWORD.toString, "randi3").either match {
        case Left(failure) => fail(failure)
        case Right(b) => b must be(true)
      }

      configurationService.saveConfigurationEntry(ConfigurationValues.DB_TYPE.toString, "mysql").either match {
        case Left(failure) => fail(failure)
        case Right(b) => b must be(true)
      }

      configurationService.saveConfigurationEntry(ConfigurationValues.DB_NAME.toString, "randi3").either match {
        case Left(failure) => fail(failure)
        case Right(b) => b must be(true)
      }

      configurationService.saveConfigurationEntry(ConfigurationValues.DB_ADDRESS.toString, "localhost").either match {
        case Left(failure) => fail(failure)
        case Right(b) => b must be(true)
      }



      //TODO fix it
      configurationService.getConfigurationEntry(ConfigurationValues.DB_ADDRESS.toString).either match {
        case Left(failure) => fail(failure)
        case Right(value) => value must be("localhost")
      }


    }

  }

}
