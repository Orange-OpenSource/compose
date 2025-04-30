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

import {applications} from "../../../utils/applications";
import {errors} from "../../../utils/errors";

import {loginUser} from "../../../utils/authentication";

const {app, server} = require('../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Create application endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.post('/api/users/me/applications')
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

    api.post('/api/users/me/applications')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Create application', function(done){
    const mockApiResponse = {
      data: {
        "api_key": "e49ded4d625334da4f9596ae42c6c68f70136d95"
      }
    };

    axios.post.mockResolvedValueOnce(mockApiResponse)

    api.post('/api/users/me/applications')
      .send({
        "name": "Test",
        "ipAddr": "0.0.0.0",
        "walletAddress": "0x0000",
        "blockchain": "abfTestnet"
      })
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });

  test('Create application with user id', function(done){
    const mockApiResponse = {
      data: {
        "api_key": "e49ded4d625334da4f9596ae42c6c68f70136d95"
      }
    };

    axios.post.mockResolvedValueOnce(mockApiResponse)

    api.post('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/applications')
      .send({
        "name": "Test",
        "ipAddr": "0.0.0.0",
        "walletAddress": "0x0000",
        "blockchain": "abfTestnet"
      })
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });
});
