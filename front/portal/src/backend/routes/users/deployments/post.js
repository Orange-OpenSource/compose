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

const fs= require("fs");
const express = require('express');
const config = require('config');

const axios = require('axios').default;

const multer  = require('multer');
const FormData = require('form-data');
const upload = multer({
  dest: 'uploads/',
  limits: {
    fileSize: 8000000 // Compliant: 8MB
  }
});

const router = new express.Router();

const {isAuthenticated} = require('../../../middlewares/authentication');
const {handleError} = require("../../../utils/errors");

router.post('/:userId/deployments', isAuthenticated, upload.single('projectFile'), async function(req, res) {
  try {
    const file = await fs.readFileSync(req.file.path);

    const form = new FormData();

    form.append('myBody', req.body.myBody);
    form.append('projectFile', file, 'contract.zip');

    axios.post(`${config.get('SCaaSUrl')}/api/v3/users/${(req.params.userId === 'me') ? req.user.userId : req.params.userId}/deployments`, form, {
      headers: {
        'USER-ID': req.user.userId,
        'USER-TOKEN': req.user.token
      }
    }).then(function (result) {
      // Delete temp file
      fs.unlinkSync(req.file.path);
      res.send(result.data);
    }).catch(function (error) {
      fs.unlinkSync(req.file.path);
      return handleError(error, res);
    });
  } catch (error){
    return handleError(error, res);
  }
});

module.exports = router;
