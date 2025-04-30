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

const passport = require('passport');
const {default: axios} = require("axios");
const config = require("config");

const LocalStrategy = require('passport-local').Strategy;

// Sign in strategy for admin users
passport.use('signIn', new LocalStrategy({
        usernameField: 'username',
        passwordField: 'password',
        passReqToCallback: true,
    }, async function (req, username, password, done) {
        try {
            let loginUrl = `${config.get('SCaaSUrl')}/api/v3/login`;

            let auth = await axios.put(loginUrl, {
              'email': username,
              'password': password
            });

            let userId = auth.data['userId'],
                userToken= auth.data['userToken'];

            let userUrl = `${config.get('SCaaSUrl')}/api/v3/users/${userId}`;
            let user = await axios.get(userUrl, {
              headers: {
                'USER-ID': userId,
                'USER-TOKEN': userToken
              }
            });

            return done(null, {
              userId: userId,
              token: userToken,
              companyId: user.data['companyId'],
              email: user.data['email'],
              name:  user.data['name'],
              familyName:  user.data['familyName'],
              roles: user.data['roles'],
            }, {
              message: 'Logged in Successfully'
            });
        } catch (error) {
          if ( error.response && (error.response.status === 404 || error.response.status === 401) ){
            return done(null, false);
          } else {
            return done({
              status: error.response?error.response.status:500,
              message: "Server error",
              description: error.message
            });
          }
        }
    })
);

passport.serializeUser(function (user, done) {
    done(null, user);
});

passport.deserializeUser(function (user, done) {
    done(null, user);
});

module.exports = passport;
