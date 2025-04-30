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

const axios = require('axios').default;

const router = new express.Router();

const {isAuthenticated} = require('../../../middlewares/authentication');
const {handleError} = require("../../../utils/errors");
const fs = require("fs");

router.post('/:userId/wallets', isAuthenticated, function(req, res) {
  axios.post(`${config.get('SCaaSUrl')}/api/v3/users/${(req.params.userId === 'me')?req.user.userId:req.params.userId}/wallets`, {
    name: req.body.name,
    password: req.body.password,
    blockchain: req.body.blockchain
  },{
    headers: {
      'USER-ID': req.user.userId,
      'USER-TOKEN': req.user.token
    }
  }).then(function(result){
    res.send(result.data)
  }).catch(function(error){
    return handleError(error, res);
  });
});

module.exports = router;
