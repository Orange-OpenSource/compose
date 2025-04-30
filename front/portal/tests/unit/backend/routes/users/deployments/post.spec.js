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

describe('Create deployment endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.post('/api/users/me/deployments')
      .expect('Content-Type', /json/)
      .expect(401)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Sign default user in', loginUser(api));

  test('Returns an error in case of failure', function(done){
    axios.post.mockRejectedValueOnce(errors['500']);

    api.post('/api/users/me/deployments')
      .field('myBody', '{"options":{"version":"0.8.14","evmVersion":"byzantium","optimizerRuns":200},"migration":[{"function":"deploy","contracts":["Storage"]}],"wallet":{"address":"0xE782cfA233b9887bFdE14988302eF945aECb98dC","password":"abc"},"blockchain":"abfTestnet"}')
      .attach('projectFile', 'tests/contract.zip')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Create deployment', function(done){
    const mockApiResponse = {
      data: {
        "deploymentId":"547d41e9-13c1-486f-a8da-63a3c79c88a1"
      }
    };

    axios.post.mockResolvedValueOnce(mockApiResponse)

    api.post('/api/users/me/deployments')
      .field('myBody', '{"options":{"version":"0.8.14","evmVersion":"byzantium","optimizerRuns":200},"migration":[{"function":"deploy","contracts":["Storage"]}],"wallet":{"address":"0xE782cfA233b9887bFdE14988302eF945aECb98dC","password":"abc"},"blockchain":"abfTestnet"}')
      .attach('projectFile', 'tests/contract.zip')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });

  test('Create deployment with user id', function(done){
    const mockApiResponse = {
      data: {
        "deploymentId":"547d41e9-13c1-486f-a8da-63a3c79c88a1"
      }
    };

    axios.post.mockResolvedValueOnce(mockApiResponse)

    api.post('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/deployments')
      .field('myBody', '{"options":{"version":"0.8.14","evmVersion":"byzantium","optimizerRuns":200},"migration":[{"function":"deploy","contracts":["Storage"]}],"wallet":{"address":"0xE782cfA233b9887bFdE14988302eF945aECb98dC","password":"abc"},"blockchain":"abfTestnet"}')
      .attach('projectFile', 'tests/contract.zip')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });
});
