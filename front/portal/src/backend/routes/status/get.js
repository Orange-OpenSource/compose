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

const express = require('express');
const config = require('config');
const {handleError} = require("../../utils/errors");

const axios = require('axios').default;

const router = new express.Router();

router.get('/', function(req, res) {
  axios.get(`${config.get('SCaaSUrl')}/api/v3/status`).then(function(result){
    res.send(result.data)
  }).catch(function(error){
    return handleError(error, res);
  });
});

module.exports = router;
