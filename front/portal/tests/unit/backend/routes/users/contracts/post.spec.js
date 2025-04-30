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

import {contracts} from "../../../utils/contracts";
import {errors} from "../../../utils/errors";

import {loginUser} from "../../../utils/authentication";

const {app, server} = require('../../../../../../src/backend/index.js');

jest.mock('axios');

const request = require('supertest');
const api = request.agent(app);

describe('Create contract endpoint', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  });

  afterAll(async () => {
    await server.close();
  });

  test('Do not authorize the access if not authenticated', function(done){
    api.post('/api/users/me/contracts')
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

    api.post('/api/users/me/contracts')
      .expect('Content-Type', /json/)
      .expect(500)
      .end(function(err, res){
        if (err) return done(err);
        done()
      });
  });

  test('Create contract', function(done){
    const mockApiResponse = {
      data: contracts.list[1]
    };

    axios.post.mockResolvedValueOnce(mockApiResponse)

    api.post('/api/users/me/contracts')
      .send({
        abi: "[\n        {\n            \"inputs\": [],\n            \"stateMutability\": \"nonpayable\",\n            \"type\": \"constructor\"\n        },\n        {\n            \"anonymous\": false,\n            \"inputs\": [\n                {\n                    \"indexed\": false,\n                    \"internalType\": \"address\",\n                    \"name\": \"sender\",\n                    \"type\": \"address\"\n                },\n                {\n                    \"indexed\": false,\n                    \"internalType\": \"uint256\",\n                    \"name\": \"amount\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"name\": \"NewStore\",\n            \"type\": \"event\"\n        },\n        {\n            \"inputs\": [\n                {\n                    \"internalType\": \"uint256\",\n                    \"name\": \"a\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"name\": \"set\",\n            \"outputs\": [],\n            \"stateMutability\": \"nonpayable\",\n            \"type\": \"function\"\n        },\n        {\n            \"inputs\": [\n                {\n                    \"internalType\": \"uint256\",\n                    \"name\": \"b\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"name\": \"add\",\n            \"outputs\": [\n                {\n                    \"internalType\": \"uint256\",\n                    \"name\": \"\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"stateMutability\": \"view\",\n            \"type\": \"function\"\n        }\n    ]",
        address: "0x588aE8F1860bfad7e0E3B085fE5fDD0495983910",
        blockchain: "abfTestnet",
        name: "Test"
      })
      .expect('Content-Type', /json/)
      .expect(200)
      .end(function(err, res){
        if (err) return done(err);
        expect(res.body).toEqual(mockApiResponse.data);
        done();
      });
  });

  test('Create contract with user id', function(done){
    const mockApiResponse = {
      data: contracts.list[1]
    };

    axios.post.mockResolvedValueOnce(mockApiResponse)

    api.post('/api/users/f5453f60-2390-48cc-9caa-50c6d143f400/contracts')
      .send({
        abi: "[\n        {\n            \"inputs\": [],\n            \"stateMutability\": \"nonpayable\",\n            \"type\": \"constructor\"\n        },\n        {\n            \"anonymous\": false,\n            \"inputs\": [\n                {\n                    \"indexed\": false,\n                    \"internalType\": \"address\",\n                    \"name\": \"sender\",\n                    \"type\": \"address\"\n                },\n                {\n                    \"indexed\": false,\n                    \"internalType\": \"uint256\",\n                    \"name\": \"amount\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"name\": \"NewStore\",\n            \"type\": \"event\"\n        },\n        {\n            \"inputs\": [\n                {\n                    \"internalType\": \"uint256\",\n                    \"name\": \"a\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"name\": \"set\",\n            \"outputs\": [],\n            \"stateMutability\": \"nonpayable\",\n            \"type\": \"function\"\n        },\n        {\n            \"inputs\": [\n                {\n                    \"internalType\": \"uint256\",\n                    \"name\": \"b\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"name\": \"add\",\n            \"outputs\": [\n                {\n                    \"internalType\": \"uint256\",\n                    \"name\": \"\",\n                    \"type\": \"uint256\"\n                }\n            ],\n            \"stateMutability\": \"view\",\n            \"type\": \"function\"\n        }\n    ]",
        address: "0x588aE8F1860bfad7e0E3B085fE5fDD0495983910",
        blockchain: "abfTestnet",
        name: "Test"
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
