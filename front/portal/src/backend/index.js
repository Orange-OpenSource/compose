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

process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;

const config = require('config');
const express = require('express');

const app = express();

const configure = require('./configure');
const port = config.get('server.port');

configure(app);

let server = app.listen(port, () => {
    console.log('Sever started with config', config);
});

module.exports = {
  app: app,
  server: server
}
