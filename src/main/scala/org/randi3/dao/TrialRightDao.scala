package org.randi3.dao

import org.randi3.model._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.Parameters
import scalaz._

import org.randi3.utility.UtilityDBComponent

trait TrialRightDaoComponent {

  this: DaoComponent with
    TrialDaoComponent with
    UtilityDBComponent =>

  val trialRightDao: TrialRightDao

  class TrialRightDao {

    import driver.Implicit._
    import schema._
    import utilityDB._

    def queryRightFromUserAndTrialAndRole(userId: Int, trialId: Int, roleName: String) = for {
      right <- Rights if right.userId === userId && right.trialId === trialId && right.role === roleName
    } yield right

    val queryAllRightsFromUser = for {
      id <- Parameters[Int]
      right <- Rights if right.userId === id
    } yield right.userId ~ right.trialId ~ right.role

    def createAll(userId: Int, trialRights: Set[TrialRight]): Validation[String, Boolean] = {
      onDB {
        threadLocalSession withTransaction {
          Rights insertAll(trialRights.map(right => (userId, right.trial.id, right.role.toString)).toSeq: _*)
        }

        Success(true)
      }
    }

    def updateRights(userId: Int, trialRights: Set[TrialRight]): Validation[String, Boolean] = {
      deleteRights(userId).either match {
        case Left(x) =>  Failure(x)
        case Right(_) => createAll(userId, trialRights)
      }

    }

    def addRight(userId: Int, right: TrialRight): Validation[String, TrialRight] = {
      onDB {
        threadLocalSession withTransaction {
          Rights insert(userId, right.trial.id, right.role.toString)
        }
        Success(right)
      }
    }

    def removeRight(userId: Int, right: TrialRight): Validation[String, TrialRight] = {
      onDB {
        threadLocalSession withTransaction {
          queryRightFromUserAndTrialAndRole(userId, right.trial.id, right.role.toString).mutate {
            r => r.delete()
          }
        }
        Success(right)
      }
    }

    private def deleteRights(userId: Int): Validation[String, Boolean] = {
      onDB {
        threadLocalSession withTransaction {
          queryAllRightsFromUser(userId).mutate {
            r =>
              r.delete()
          }
        }
        Success(true)
      }
    }

    def getAll(userId: Int): Validation[String, Set[TrialRight]] = {
      onDB {
        val allTrials = trialDao.getAll.either match {
          case Left(x) => return Failure(x)
          case Right(x) => x
        }

        Success(queryAllRightsFromUser(userId).list.map(entry =>
          TrialRight(Role.withName(entry._3),
            allTrials.find(trial => trial.id == entry._2).getOrElse(return Failure("trial not found"))).either match {
            case Left(x) => return Failure("Database entry corrupt " + x.toString)
            case Right(right) => right
          }).toSet)
      }
    }

  }

}