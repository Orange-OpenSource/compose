/*
 *
 *  *
 *  *  * Software Name : Compose
 *  *  * SPDX-FileCopyrightText: Copyright (c) Orange SA
 *  *  * SPDX-License-Identifier:  MIT
 *  *  *
 *  *  * This software is distributed under the MIT License,
 *  *  * see the "LICENSE.txt" file for more details or https://spdx.org/licenses/MIT.html
 *  *  *
 *  *  * <Authors: optional: authors list / see CONTRIBUTORS>
 *  *
 *
 */

/**
 * Handle errors
 * @param error
 * @param res
 */
exports.handleError = function(error, res){
  if ( error.response ){
    res.status(error.response.status).send({
      message: error.response.statusText,
      description: error.toString()
    });
  } else {
    res.status(500).send({
      message: "Server error",
      description: error.toString()
    });
  }
};
