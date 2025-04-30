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

package common.controllers

import common.utils.TraceUtils
import play.api.libs.json._

class JsonSanitizer[T](paths: Seq[JsPath])(implicit format: OFormat[T]) extends TraceUtils {

  def sanitize(idKey: String)(obj: T): JsValue = {
    entering(s"sanitize")
    val jsonObj = Json.toJson(obj)


    val pruneId = jsonObj \ "_id"
    val updatedObj = if (pruneId.isDefined) {
      val pruneId: Reads[JsObject] = __.json.update(
        (__ \ "_id").read[String].flatMap(id =>
          (__ \ idKey).json.put(JsString(id))
        )
      )
      jsonObj.transform(pruneId).get
    } else {
      jsonObj
    }

    (paths :+ (__ \ Symbol("_id")))
      .map(path => path.json.prune)
      .foldLeft(updatedObj) { (previousRes, f) => previousRes.transform(f).get }


  }

}

object JsonSanitizer {
  def apply[T](paths: JsPath*)(idKey: String)(implicit format: OFormat[T]): T => JsValue = new JsonSanitizer[T](paths)(format).sanitize(idKey)
}
