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

import {transactions} from "../../../utils/transactions";
import {errors} from "../../../utils/errors";

import {loginUser} from "../../../utils/authentication";

const {app, server} = require('../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Get transaction endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.get('/api/blockchains/abfTestnet/transactions/0xa5fba3c56f60cbe5342ca5a4c7b561d670c72d84e2ad51ca1800d984d7cc789d')
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

    api.get('/api/blockchains/abfTestnet/transactions/0xa5fba3c56f60cbe5342ca5a4c7b561d670c72d84e2ad51ca1800d984d7cc789d')
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error if not found', function(done){
    axios.get.mockRejectedValueOnce(errors['404']);

    api.get('/api/blockchains/abfTestnet/transactions/0xa5fba3c56f60cbe5342ca5a4c7b561d670c72d84e2ad51ca1800d984d7cc789d')
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(404)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Get transaction details', function(done){
    const mockApiResponse = {
      data: transactions.list[0]
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get(`/api/blockchains/abfTestnet/transactions/0xa5fba3c56f60cbe5342ca5a4c7b561d670c72d84e2ad51ca1800d984d7cc789d`)
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });
});
