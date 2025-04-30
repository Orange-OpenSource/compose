/*
 *
 *  * Software Name : Compose
 *  * SPDX-FileCopyrightText: Copyright (c) Orange SA
 *  * SPDX-License-Identifier:  MIT
 *  *
 *  * This software is distributed under the MIT License,
 *  * see the "LICENSE.txt" file for more details or https://spdx.org/licenses/MIT.html
 *  *
 *  * <Authors: optional: authors list / see CONTRIBUTORS>
 *
 */

package common.dao

import common.exceptions.DAOException
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentHandler}
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadConcern}
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson._
import reactivemongo.play.json.compat.lax._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


abstract class AbstractDao[T] @Inject()(
                                             implicit ec: ExecutionContext,
                                             reactiveMongoApi: ReactiveMongoApi,
                                             format: BSONDocumentHandler[T]) {

    val collectionName: String

    // Collection
    def collection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection](collectionName))

    // Creation
    def create(element: T): Future[T] = {
        collection
            .flatMap((bsonCollection: BSONCollection) => {
                bsonCollection
                    .insert(ordered = false)
                    .one(element)
            })
            .flatMap(returnOrFail[T](_, element))
    }

    // Count
    def count(): Future[Long] = collection.flatMap((bsonCollection: BSONCollection) => {
        bsonCollection.count(None, None, 0, None, ReadConcern.Available)
    })

    // Find
    def find(criteria: BSONDocument, limit: Int = Int.MaxValue): Future[Seq[T]] =
        collection.flatMap(
            _.find(criteria, Option.empty[BSONDocument])
                .cursor[T]()
                .collect[Seq](limit, Cursor.FailOnError())
        )

    def findOne(criteria: BSONDocument): Future[T] = find(criteria, 1).map(seq => seq.head)
    def findOneBy(field: String)(value: String): Future[T] =
        findOne(BSONDocument(field -> value))

    def findBy(field: String)(value: String, limit: Int = Int.MaxValue): Future[Seq[T]] =
        find(BSONDocument(field -> value), limit)

    def findById(id: String): Future[T] =
        findBy("_id")(id, 1)
            .map(_.head)
            .recoverWith { case err: Exception => Future.failed[T](DAOException(err.getMessage)) }


    def findAll(limit: Int = Int.MaxValue): Future[Seq[T]] = find(BSONDocument(), limit)

    // Update
    def update(criteria: BSONDocument)(newElement: T, createOnNotFound: Boolean = false): Future[T] =
        collection
            .flatMap(_.update.one(criteria, newElement, createOnNotFound, multi = false))
            .flatMap(returnOrFail[T](_, newElement))


    def updateBy(field: String)(value: String)(newElement: T, createOnNotFound: Boolean = false): Future[T] =
        update(BSONDocument(field -> value))(newElement, createOnNotFound)

    def updateById(id: String, newElement: T, createOnNotFound: Boolean = false): Future[T] =
        updateBy("_id")(id)(newElement, createOnNotFound)

    // Delete
    def delete(criteria: BSONDocument): Future[Unit] = {
        collection
            .flatMap(_.delete.one(criteria))
            .map(returnOrFail[Unit](_, ()))
    }

    def deleteBy(field: String)(value: String): Future[Unit] = delete(BSONDocument(field -> value))

    def deleteById(id: String): Future[Unit] = deleteBy("_id")(id)

    private def returnOrFail[U](writeResult: WriteResult, u: U): Future[U] =
        writeResult.writeErrors.size match {
            case 0 => Future.successful[U](u)
            case _ => Future.failed[U](DAOException(writeResult.toString))
        }

}
