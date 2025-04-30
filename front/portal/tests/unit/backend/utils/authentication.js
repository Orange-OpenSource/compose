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

const {users} = require("./users");
const axios = require("axios");

function loginUser(api) {
  return function(done) {
    const mockSCAASResponse = {
      data: users.user
    };

    axios.put.mockResolvedValueOnce(mockSCAASResponse);
    axios.get.mockResolvedValueOnce(mockSCAASResponse);

    api
      .post('/authentication/signin')
      .send({
        username: "test2@orange.com",
        password: "test2"
      })
      .end(onResponse);

    function onResponse(err, res) {
      if (err) return done(err);
      return done();
    }
  };
}

module.exports = { loginUser }
