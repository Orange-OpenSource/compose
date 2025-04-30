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

import {errors} from "../../../utils/errors";

import {loginUser} from "../../../utils/authentication";

const {app, server} = require('../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Get wallet balance endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.get('/api/users/me/wallets/0xE782cfA233b9887bFdE14988302eF945aECb98dC/balance')
      .expect('Content-Type', /json/)
      .expect(401)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Sign default user in', loginUser(api));

  test('Returns an error in case of failure', function(done){
    axios.get.mockRejectedValueOnce(errors['500']);

    api.get('/api/users/me/wallets/0xE782cfA233b9887bFdE14988302eF945aECb98dC/balance')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error if not found', function(done){
    axios.get.mockRejectedValueOnce(errors['404']);

    api.get('/api/users/me/wallets/0xE782cfA233b9887bFdE14988302eF945aECb98dC/balance')
      .expect('Content-Type', /json/)
      .expect(404)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Get wallet balance', function(done){
    const mockApiResponse = {
      data: {
        "amount": "100"
      }
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get('/api/users/me/wallets/0xE782cfA233b9887bFdE14988302eF945aECb98dC/balance')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });

  test('Get wallet balance with user id', function(done){
    const mockApiResponse = {
      data: {
        "amount": "100"
      }
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/wallets/0xE782cfA233b9887bFdE14988302eF945aECb98dC/balance')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });
});
