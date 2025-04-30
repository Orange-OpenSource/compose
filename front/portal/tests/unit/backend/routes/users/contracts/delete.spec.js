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

describe('Delete contract endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.delete('/api/users/me/blockchains/abfTestnet/contracts/04be0470-20af-4baa-8726-130bce0b98e8')
      .expect('Content-Type', /json/)
      .expect(401)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Sign default user in', loginUser(api));

  test('Returns an error in case of failure', function(done){
    axios.delete.mockRejectedValueOnce(errors['500']);

    api.delete('/api/users/me/blockchains/abfTestnet/contracts/04be0470-20af-4baa-8726-130bce0b98e8')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Delete contract', function(done){
    const mockApiResponse = {
      data: null
    };

    axios.delete.mockResolvedValueOnce(mockApiResponse)

    api.delete('/api/users/me/blockchains/abfTestnet/contracts/04be0470-20af-4baa-8726-130bce0b98e8')
      .expect(204)
      .end(function(err, res){
        if (err) return done(err);
        done();
      });
  });

  test('Delete contract with user id', function(done){
    const mockApiResponse = {
      data: null
    };

    axios.delete.mockResolvedValueOnce(mockApiResponse)

    api.delete('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/blockchains/abfTestnet/contracts/04be0470-20af-4baa-8726-130bce0b98e8')
      .expect(204)
      .end(function(err, res){
        if (err) return done(err);
        done();
      });
  });
});
