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

const companiesRouter = require('./companies');
const usersRouter = require('./users');
const blockchainsRouter = require('./blockchains');
const hooksRouter = require('./hooks');
const statusRouter = require('./status');


const router = new express.Router();

router.use('/companies', companiesRouter);
router.use('/users', usersRouter);
router.use('/blockchains', blockchainsRouter);
router.use('/hooks', hooksRouter);
router.use('/status', statusRouter);

module.exports = router;
