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

const passport = require('../utils/passport');

const router = new express.Router();

const {isAuthenticated} = require('../middlewares/authentication');

/**
 * Authenticate Admin
 */
router.post('/signin', async function(req, res, next) {
    passport.authenticate('signIn', {},function(err, user, next) {
        if (err) {
            return res.status(err.status).send(err);
        }

        if (!user) {
            return res.status(401).send({
                message: 'Unauthorized',
                description: 'You are not logged in',
            });
        }

        req.logIn(user, function(err) {
            if (err) {
                return next(err);
            }

            res.json({
                email: user.email,
                name: user.name,
                familyName: user.familyName,
                roles: user.roles
            });
        });
    })(req, res, next);
});

/**
 * Sign out admin
 */
router.get('/signout', function(req, res, next) {
    req.logout(function(err) {
        if (err) { return next(err); }
        res.redirect('/');
    });
});

/**
 * Check authentication
 */
router.get('/check', isAuthenticated, function(req, res) {
    res.json({
        email: req.user.email,
        name: req.user.name,
        familyName: req.user.familyName,
        roles: req.user.roles
    });
});

module.exports = router;
