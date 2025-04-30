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

package smart_contracts.dao

import common.dao.AbstractDao
import play.modules.reactivemongo.ReactiveMongoApi
import smart_contracts.model.dao.WebhookListener

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WebhookListenersDao @Inject()(
                                     implicit ec: ExecutionContext,
                                     reactiveMongoApi: ReactiveMongoApi) extends AbstractDao[WebhookListener] {

  override val collectionName = "http-callback-hooks"
}
