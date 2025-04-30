package dao

import common.dao.AbstractDao
import common.exceptions.DAOException
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.mongo.transitions.{Mongod, RunningMongodProcess}
import de.flapdoodle.reverse.TransitionWalker
import org.mockito.Mockito.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.test.PlaySpecification
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


abstract class AbstractDaoTest[T] extends PlaySpecification  {
  sequential
  val injector: Injector = new GuiceApplicationBuilder().injector()
  val element: T
  val updatedElement: T
  val secondElement: T
  val id: String = "_id"
  val collectionName: String
  val dao: AbstractDao[T]

  implicit val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit val reactiveMongoApi: ReactiveMongoApi =  mock(classOf[ReactiveMongoApi])

  var instance: TransitionWalker.ReachedState[RunningMongodProcess] = Mongod
    .builder()
    .build()
    .start(Version.V5_0_13)

  val mongoDriver: AsyncDriver = AsyncDriver()
  val parsedURIFuture: Future[ParsedURI] = MongoConnection.fromString("mongodb://" + instance.current().getServerAddress.toString)
  val connection: Future[MongoConnection] = parsedURIFuture.flatMap(u => mongoDriver.connect(u))
  val db: Future[DB] = connection.flatMap(_.database("mongo-scaas"))
  when(reactiveMongoApi.database).thenReturn(db)
  val collection: Future[BSONCollection] = db.map(_.collection(collectionName))
  collection.flatMap(_.drop(failIfNotFound = false))

  def after: Any = {
    collection.flatMap(_.drop(failIfNotFound = false))
  }

  def afterAll(): Unit = {
    val collection: Future[BSONCollection] = db.map(_.collection(collectionName))
    collection.flatMap(_.drop(failIfNotFound = false))

    if (System.getProperty("os.name")
      .toLowerCase()
      .contains("windows")) {
      instance = null
    } else {
      instance.current().stop()
    }
  }

  "AbstractDBDAOTest" should {

    "have a correct collection name" in {
      dao.collectionName.equals(collectionName)
    }

    "count" in {
      val initialCount = Await.result(dao.count(), Duration.Inf)
      initialCount mustEqual 0L
      Await.ready(dao.create(element), Duration.Inf)
      val secondCount = Await.result(dao.count(), Duration.Inf)
      secondCount mustEqual 1L
    }

    "findById" in {
      Await.ready(dao.create(element), Duration.Inf)
      val i = Await.result(dao.findById(id), Duration.Inf)

      i mustEqual element
      collection.flatMap(_.drop(failIfNotFound = false))
    }

    "create" in {
      Await.ready(dao.findById(id).recover { case DAOException(err) => err mustEqual "exceptions.DAOException: head of empty list" }, Duration.Inf)
      val elt = Await.result(dao.create(element), Duration.Inf)
      elt mustEqual element
      val i = Await.result(dao.findById(id), Duration.Inf)
      collection.flatMap(_.drop(failIfNotFound = false))
      i mustEqual element
    }

    "updateById" in {
      val elt = Await.result(dao.create(element), Duration.Inf)
      elt mustEqual element
      val i = Await.result(dao.findById(id), Duration.Inf)
      i mustEqual element
      val j = Await.result(dao.updateById(id, updatedElement), Duration.Inf)
      j mustEqual updatedElement
      val k = Await.result(dao.findById(id), Duration.Inf)
      collection.flatMap(_.drop(failIfNotFound = false))
      k mustEqual updatedElement
    }

    "findAll" in {
      Await.ready(dao.findById(id).recover { case DAOException(err) => err mustEqual "exceptions.DAOException: head of empty list" }, Duration.Inf)
      val elt1 = Await.result(dao.create(element), Duration.Inf)
      val elt2 = Await.result(dao.create(secondElement), Duration.Inf)
      val l = Await.result(dao.findAll(), Duration.Inf)
      collection.flatMap(_.drop(failIfNotFound = false))
      l mustEqual List(elt1, elt2)
    }

    "deleteById" in {
      Await.ready(dao.findById(id).recover { case DAOException(err) => err mustEqual "exceptions.DAOException: head of empty list" }, Duration.Inf)
      val elt = Await.result(dao.create(element), Duration.Inf)
      elt mustEqual element
      val i = Await.result(dao.findById(id), Duration.Inf)
      i mustEqual element
      Await.result(dao.deleteById(id), Duration.Inf)
      val initialCount = Await.result(dao.count(), Duration.Inf)
      collection.flatMap(_.drop(failIfNotFound = false))
      initialCount mustEqual 0L
    }
  }

}
