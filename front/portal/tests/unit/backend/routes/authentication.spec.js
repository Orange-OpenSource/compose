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

import axios from 'axios';

import {users} from "../utils/users";
import {errors} from "../utils/errors";

const {app, server} = require('../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Authentication Endpoints', function(){

  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if login fails', function(done) {
    axios.put.mockRejectedValueOnce(errors['401']);

    api.post('/authentication/signin')
      .send({
        username: "fake",
        password: "fake"
      })
      .expect('Content-Type', /json/)
      .expect(401)
      .end(function (err, res) {
        if (err) return done(err);
        done()
      });
  });

  test('Do not authorize the access if user not found', function(done) {
    axios.put.mockRejectedValueOnce(errors['404']);

    api.post('/authentication/signin')
      .send({
        username: "fake",
        password: "fake"
      })
      .expect('Content-Type', /json/)
      .expect(401)
      .end(function (err, res) {
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error in case of failure',  function(done){
    axios.put.mockRejectedValueOnce(errors['500']);

    api.post('/authentication/signin')
      .send({
        username: "fake",
        password: "fake"
      })
      .expect('Content-Type', /json/)
      .expect(500).end(function (err, res) {
      if (err) return done(err);
      done()
    });
  });

  test('Sign default user in', loginUser());

  test('Get users details', function(done){
    api.get('/authentication/check')
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);

        expect(res.body.email).toEqual(users.user.email);
        expect(res.body.name).toEqual(users.user.name);
        expect(res.body.familyName).toEqual(users.user.familyName);
        expect(res.body.roles).toEqual(users.user.roles);

        done()
      });
  });

  test('Redirect on sign out', function(done){
    api.get('/authentication/signout')
      .expect('Location', '/')
      .expect(302)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });
});

function loginUser() {
  return function(done) {
    const mockSCAASResponse = {
      data: users.user
    };

    axios.put.mockResolvedValueOnce(mockSCAASResponse);
    axios.get.mockResolvedValueOnce(mockSCAASResponse);

    api
      .post('/authentication/signin')
      .send({
        username: "test2@orange.com",
        password: "test2"
      })
      .expect(200)
      .end(onResponse);

    function onResponse(err, res) {
      if (err) return done(err);

      expect(res.body.email).toEqual(users.user.email);
      expect(res.body.name).toEqual(users.user.name);
      expect(res.body.familyName).toEqual(users.user.familyName);
      expect(res.body.roles).toEqual(users.user.roles);

      return done();
    }
  };
};
