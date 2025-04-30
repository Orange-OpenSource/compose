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

package login.dao

import common.dao.AbstractDao
import login.model.Token
import play.modules.reactivemongo.ReactiveMongoApi

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TokensDao @Inject()(
                           implicit ec: ExecutionContext,
                           reactiveMongoApi: ReactiveMongoApi) extends AbstractDao[Token] {

  override val collectionName = "tokens"
}
