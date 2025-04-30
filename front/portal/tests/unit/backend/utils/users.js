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

module.exports.users = {
  admin: {
    userId: "user01",
    companyId: "company01",
    email: "test@orange.com",
    name: "test",
    familyName: "test",
    roles: [
      "USER",
      "ADMIN"
    ],
  },
  user: {
    userId: "user02",
    companyId: "company01",
    email: "test2@orange.com",
    name: "test",
    familyName: "test",
    roles: [
      "USER"
    ],
  },
  list: [
    {
      "companyId": "409a20d2-086e-44c8-bd9f-489b3f941572",
      "credential": "0d393524-43a6-4f5f-ab4a-288357acb267",
      "familyName": "Admin",
      "roles": [
        "ADMIN",
        "USER"
      ],
      "name": "Super",
      "telephone": "0000000001",
      "machines": [
        "c59aa277-7d90-439f-9ab6-b97c39493eb9",
        "9dd79377-c5cb-44f4-8aa0-6d0d4a93498f",
        "0de1c50a-10cd-4634-8a50-aad61802832e"
      ],
      "userId": "f5453f60-2390-48cc-9caa-50c6d143f400",
      "email": "admin@compose.com"
    }
  ]
}
