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

module.exports.errors = {
  '401': {
    response: {
      status: 401,
      data: { code: 403, message: 'Unauthorized' }
    }
  },
  '403': {
    response: {
      status: 403,
      data: { code: 403, message: 'Forbidden' }
    }
  },
  '404': {
    response: {
      status: 404,
      data: { code: 404, message: 'Not Found' }
    }
  },
  '409': {
    response: {
      status: 409,
      data: { code: 409, message: 'Conflict email already exist"' }
    }
  },
  '500': {
    response: {
      status: 500,
      data: { code: 500, message: 'Server error' }
    }
  }
}
