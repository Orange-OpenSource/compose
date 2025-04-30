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

import play.api.Logger

trait TraceUtils {
    val logger: Logger = Logger(getClass)

    def entering(methodName: String): Unit = logger.info(s"Entering $methodName")

    def debug(methodName: String, msg: String): Unit = logger.debug(s"$methodName | $msg")

    def info(methodName: String, msg: String): Unit = logger.info(s"$methodName | $msg")

    def warn(methodName: String, msg: String): Unit = logger.warn(s"$methodName | $msg")

    def error(methodName: String, msg: String): Unit = logger.error(s"$methodName | $msg")
}
