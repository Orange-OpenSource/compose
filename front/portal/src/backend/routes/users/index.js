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
const postRouter = require('./post');
const putRouter = require('./put');
const listRouter = require('./list');
const getRouter = require('./get');

const updateCredentialsRouter = require('./credentials/put');

const listApplicationsRouter = require('./applications/list');
const postApplicationRouter = require('./applications/post');
const deleteApplicationRouter = require('./applications/delete');

const getWalletRouter = require('./wallets/get');
const listWalletsRouter = require('./wallets/list');
const postWalletRouter = require('./wallets/post');
const importWalletRouter = require('./wallets/import');
const deleteWalletRouter = require('./wallets/delete');
const getWalletBalanceRouter = require('./wallets/getBalance');

const getContractRouter = require('./contracts/get');
const postContractRouter = require('./contracts/post');
const listContractsRouter = require('./contracts/list');
const deleteContractsRouter = require('./contracts/delete');

const getDeploymentRouter = require('./deployments/get');
const listDeploymentsRouter = require('./deployments/list');
const listOngoingDeploymentsRouter = require('./deployments/ongoing');
const postDeploymentsRouter = require('./deployments/post');

const getEventListenerRouter = require('./contracts/events/listeners/get');
const postEventListenerRouter = require('./contracts/events/listeners/post');
const listEventListenersRouter = require('./contracts/events/listeners/list');
const deleteEventListenersRouter = require('./contracts/events/listeners/delete');

router.use(postRouter);
router.use(putRouter);
router.use(listRouter);
router.use(getRouter);

router.use(updateCredentialsRouter);

router.use(listApplicationsRouter);
router.use(postApplicationRouter);
router.use(deleteApplicationRouter);

router.use(getWalletRouter);
router.use(listWalletsRouter);
router.use(postWalletRouter);
router.use(importWalletRouter);
router.use(deleteWalletRouter);
router.use(getWalletBalanceRouter);

router.use(getContractRouter);
router.use(postContractRouter);
router.use(listContractsRouter);
router.use(deleteContractsRouter);

router.use(listOngoingDeploymentsRouter);
router.use(getDeploymentRouter);
router.use(listDeploymentsRouter);
router.use(postDeploymentsRouter);

router.use(getEventListenerRouter);
router.use(postEventListenerRouter);
router.use(listEventListenersRouter);
router.use(deleteEventListenersRouter);

module.exports = router;
