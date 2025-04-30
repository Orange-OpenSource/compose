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

import {eventListeners} from "../../../../../utils/eventListeners";
import {errors} from "../../../../../utils/errors";

import {loginUser} from "../../../../../utils/authentication";

const {app, server} = require('../../../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('List event listeners endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.get('/api/users/me/contracts/04be0470-20af-4baa-8726-130bce0b98e8/events/NewStore/webhooks')
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

    api.get('/api/users/me/contracts/04be0470-20af-4baa-8726-130bce0b98e8/events/NewStore/webhooks')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Get event listeners list', function(done){
    const mockApiResponse = {
      data: eventListeners.list
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get('/api/users/me/contracts/04be0470-20af-4baa-8726-130bce0b98e8/events/NewStore/webhooks')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });

  test('Get event listeners list with user id', function(done){
    const mockApiResponse = {
      data: eventListeners.list
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/contracts/04be0470-20af-4baa-8726-130bce0b98e8/events/NewStore/webhooks')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });
});
