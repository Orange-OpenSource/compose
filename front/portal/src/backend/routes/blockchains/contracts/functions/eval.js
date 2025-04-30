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

const {isAuthenticated} = require('../../../../middlewares/authentication');
const {handleError} = require("../../../../utils/errors");

router.post('/:blockchainName/contracts/:contractAddress/functions/:functionName/eval', isAuthenticated, function(req, res) {
  axios.post(`${config.get('SCaaSUrl')}/api/v3/blockchains/${req.params.blockchainName}/contracts/${req.params.contractAddress}/functions/${req.params.functionName}/eval`, {
    walletPassword: req.body.walletPassword,
    tokenValue: req.body.tokenValue,
    parameters: req.body.parameters
  },{
    headers: {
      'CLIENT-ID': req.get('application-id'),
      'API-KEY': req.get('api-key')
    }
  }).then(function(result){
    res.send(result.data)
  }).catch(function(error){
    return handleError(error, res);
  });
});

module.exports = router;
