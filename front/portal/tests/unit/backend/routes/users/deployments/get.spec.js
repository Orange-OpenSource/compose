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

import {deployments} from "../../../utils/deployments";
import {errors} from "../../../utils/errors";

import {loginUser} from "../../../utils/authentication";

const {app, server} = require('../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Get deployment endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.get('/api/users/me/deployments/3ef1fb08-8d69-4085-a6b0-104ed92a3026')
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

    api.get('/api/users/me/deployments/3ef1fb08-8d69-4085-a6b0-104ed92a3026')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error if not found', function(done){
    axios.get.mockRejectedValueOnce(errors['404']);

    api.get('/api/users/me/deployments/3ef1fb08-8d69-4085-a6b0-104ed92a3026')
      .expect('Content-Type', /json/)
      .expect(404)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Get deployment', function(done){
    const mockApiResponse = {
      data: deployments.list[0]
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get('/api/users/me/deployments/3ef1fb08-8d69-4085-a6b0-104ed92a3026')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });

  test('Get deployment with user id', function(done){
    const mockApiResponse = {
      data: deployments.list[0]
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/deployments/3ef1fb08-8d69-4085-a6b0-104ed92a3026')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });
});
