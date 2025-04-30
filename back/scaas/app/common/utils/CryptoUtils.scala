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

package common.utils

import play.api.Configuration

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}
import scala.collection.immutable.ArraySeq
import scala.util.Random

class CryptoUtils(configuration: Configuration) extends TraceUtils {

  private def mk: String = configuration.get[String]("blockchain.wallet.master-key")

  def retrieveWalletKey(walletSalt: String, clientSalt: String): String = {
    entering("retrieveWalletKey")
    def inner(password: String, salt: String): String = {
      val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      val spec = new PBEKeySpec(password.toCharArray, salt.getBytes, 65536, 256)
      Hex().toHexString(ArraySeq.unsafeWrapArray(new SecretKeySpec(factory.generateSecret(spec).getEncoded, "AES").getEncoded)).mkString
    }

    inner(inner(mk, walletSalt), clientSalt)
  }

  def generateSafeToken(size: Int): String = {
    entering("generateSafeToken")

    val bytes = new Array[Byte](size)
    Random.nextBytes(bytes)
    Hex().toHexString(ArraySeq.unsafeWrapArray(bytes)).mkString
  }

}

object CryptoUtils {
  def apply(configuration: Configuration): CryptoUtils = new CryptoUtils(configuration)
}
