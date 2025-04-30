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

import {blocks} from "../../../utils/blocks";
import {errors} from "../../../utils/errors";

import {loginUser} from "../../../utils/authentication";

const {app, server} = require('../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Get block endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.get('/api/blockchains/abfTestnet/blocks/012345')
      .expect('Content-Type', /json/)
      .expect(401)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.get('/api/blockchains/abfTestnet/blocks/find/0x9919a1779d2ec7eeae403128fc573624d6ab927ba73c7014c9627994ce54ec7e')
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

    api.get('/api/blockchains/abfTestnet/blocks/012345')
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error in case of failure', function(done){
    axios.get.mockRejectedValueOnce(errors['500']);

    api.get('/api/blockchains/abfTestnet/blocks/find/0x9919a1779d2ec7eeae403128fc573624d6ab927ba73c7014c9627994ce54ec7e')
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error if block number not found', function(done){
    axios.get.mockRejectedValueOnce(errors['404']);

    api.get('/api/blockchains/abfTestnet/blocks/012345')
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(404)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Returns an error if hash not found', function(done){
    axios.get.mockRejectedValueOnce(errors['404']);

    api.get('/api/blockchains/abfTestnet/blocks/find/0x9919a1779d2ec7eeae403128fc573624d6ab927ba73c7014c9627994ce54ec7e')
      .set('application-id', 'c59aa277-7d90-439f-9ab6-b97c39493eb9')
      .set('api-key', 'e49ded4d625334da4f9596ae42c6c68f70136d95')
      .expect('Content-Type', /json/)
      .expect(404)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Get block details by number', function(done){
    const mockApiResponse = {
      data: blocks.list[0]
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get(`/api/blockchains/abfTestnet/blocks/012345`)
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

  test('Get block details by hash', function(done){
    const mockApiResponse = {
      data: blocks.list[0]
    };

    axios.get.mockResolvedValueOnce(mockApiResponse)

    api.get(`/api/blockchains/abfTestnet/blocks/find/0x9919a1779d2ec7eeae403128fc573624d6ab927ba73c7014c9627994ce54ec7e`)
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
