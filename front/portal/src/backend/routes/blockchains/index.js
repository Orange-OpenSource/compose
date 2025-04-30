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

const router = new express.Router();

// Add routes
const listBlockchainsRouter = require('./list');
const getBlockchainRouter = require('./get');

const evalFunctionRouter = require('./contracts/functions/eval');
const readFunctionRouter = require('./contracts/functions/read');

const listEventsRouter = require('./contracts/events/list');
const listEventsByNameRouter = require('./contracts/events/listByName');

const getTransactionRouter = require('./transactions/get');

const getBlockRouter = require('./blocks/get');

router.use(listBlockchainsRouter);
router.use(getBlockchainRouter);

router.use(evalFunctionRouter);
router.use(readFunctionRouter);

router.use(listEventsRouter);
router.use(listEventsByNameRouter);

router.use(getTransactionRouter);

router.use(getBlockRouter);

module.exports = router;
