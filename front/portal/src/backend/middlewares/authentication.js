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

/**
 * Pass through if the user is authenticated
 * @param req
 * @param res
 * @param next
 */
exports.isAuthenticated = async function(req, res, next){
    let fireUnauthorized = function(res){
        res.status(401).json({
            message: 'Unauthorized',
            description: 'You are not logged in',
        });
    };

    if ( !req.isAuthenticated() ) {
        return fireUnauthorized(res);
    } else {
        // TODO : Refresh token ?
    }

    next();
};
