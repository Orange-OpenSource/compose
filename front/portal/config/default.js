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

const dotenv = require('dotenv');

if (process.env.NODE_ENV !== "production") {
  let mode =
    process.env.NODE_ENV !== undefined
      ? process.env.NODE_ENV
      : "development.local";

  dotenv.config({ path: `./.env.${mode}` });
}

module.exports = {
  name: 'SCaaS Frontend',
  server: {
    ip: '0.0.0.0',
    port: Number(process.env.SERVER_PORT)
  },
  SCaaSUrl: process.env.SCAAS_URL,
  session: {
    secret: process.env.JWT_SECRET
  }
};
