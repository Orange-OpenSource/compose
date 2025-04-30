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

const {app, server} = require('../../../../../src/backend/index.js');
const {errors} = require('../../utils/errors');

const request = require('supertest');

// Mocks
jest.mock('axios');

describe('Get status endpoint', () => {
    beforeEach(() => {
      jest.clearAllMocks()
    });

    afterAll(async () => {
      await server.close();
    });

    test('Returns an error in case of failure', async () => {
      axios.get.mockRejectedValueOnce(errors['500']);

      await request(app)
        .get('/api/status')
        .expect('Content-Type', /json/)
        .expect(500);
    });

    test('Get status', async () => {
      const mockApiResponse = {
        data: {
          "status":"❤"
        }
      };

      axios.get.mockResolvedValueOnce(mockApiResponse)

      await request(app)
          .get('/api/status')
          .expect('Content-Type', /json/)
          .expect(200)
          .then((response) => {
              expect(response.body).toEqual(mockApiResponse.data);
          });
    });
});
