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

package wallets.dao

import common.dao.AbstractDao
import common.utils.TraceUtils
import play.modules.reactivemongo.ReactiveMongoApi
import wallets.controllers.WalletManager
import wallets.model.dao.Wallet

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WalletDao @Inject()(
                           implicit ec: ExecutionContext,
                           reactiveMongoApi: ReactiveMongoApi) extends AbstractDao[Wallet] with TraceUtils {

  override val collectionName = "wallets"

  def findOneByAddress(userId: String, blockchain: String, address: String): Future[Wallet] = {
    debug("findOneByAddress", userId)
    findBy("userId")(userId)
      .map(wallets => {
        wallets
          .filter(wallet => wallet.blockchain == blockchain && wallet.address == address)
          .head
      })
  }

}
