# Smart Contract as a Service (SCaaS) - Portal

[![Quality Gate Status](https://sqaas.dos.tech.orange/api/project_badges/measure?project=smartchain_compose_v3_frontend_portal_AYlOTKchFhmrILXu8HtU&metric=alert_status&token=sqb_4fcbe96239181240ecc71517f68f91ffb313f155)](https://sqaas.dos.tech.orange/dashboard?id=smartchain_compose_v3_frontend_portal_AYlOTKchFhmrILXu8HtU)

## Description

This is the admin portal for the Smart Chain as a Service (SCaaS) backend of the Compose project. It allows SCaaS users to fully manage their applications, deployments and contracts lifecycle. It also provides features for users to test their smart contract functions once they have been deployed to a blockchain. Through the interface you also have full access to all the events and transactions handled on by the platform. 

## Backend

The backend of the portal has been developed in JavaScript, on top of NodeJS (v 16+). The web server is based on Express. The authentication layer is provided by Passport.

| Library      | Version | Description              |
|--------------|---------|--------------------------|
| Express      | 4.18.2  | Web server               | 
| Passport     | 0.6.0   | Authentication           |
| Axios        | 1.5.0   | HTTP Client              |

## Frontend Libraries

| Library            | Version | Description                                 |
|--------------------|---------|---------------------------------------------|
| Vue                | 3.3.4   | The core JS Framework of the portal         | 
| Boosted            | 5.3.2   | The Orange version of the bootstrap project |
| Bootstrap Vue next | 0.14.10 | An integration layer for Vue and Bootstrap  |
| Font awesome       | 6.4.2   | A library for font icons                    |

## Run

To run the dev environment, you will need to launch ```npm run serve```. The development environment variables can be found under config/developpment.json

## Build

To build the application, launch ```npm run build```.

## Tests

To run unit tests, just launch ```npm run test```.
