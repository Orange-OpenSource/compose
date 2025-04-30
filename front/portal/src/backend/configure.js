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

const session = require('express-session');
const cookieParser = require('cookie-parser');

const history = require('connect-history-api-fallback');
const bodyParser = require('body-parser');

const path = require('path');

// Authentication and Routes
const passport = require('./utils/passport');

const apiRouter = require('./routes/api');
const authenticationRouter = require('./routes/authentication');

// Configuration
module.exports = (app) => {
    app.use(cookieParser());

    app.use(session({
        secret: config.get('session.secret'),
        cookie: { _expires: (60 * 60 * 1000) },
        saveUninitialized: true,
        resave: true
    }));

    app.use(passport.initialize());
    app.use(passport.session());

    app.use(bodyParser.json());
    app.use(bodyParser.urlencoded({
      extended: true,
      limit: "2mb"
    }));

    // When built
    app.use(express.static(path.join(__dirname, "../../dist")));

    app.use('/authentication', authenticationRouter);
    app.use('/api', apiRouter);

    app.use(history());

    // 404 Case : serve the index file
    app.use(function(req, res) {
        return res.sendFile(path.join(__dirname, "../../dist/index.html"));
    });
};
