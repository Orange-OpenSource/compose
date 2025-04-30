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

module.exports.blockchains = {
  list: ["local", "alastria", "abfTestnet"],
  abfTestnet: {
    "name": "abfTestnet",
    "type": "ethereum",
    "web3Client": {
      "version": "Geth/v1.11.5-stable-a38f4108/linux-amd64/go1.20.2",
      "url": "http://abf-testnet:8443",
      "chainId": "751264037"
    },
    "chainId": "751264037",
    "blockNumber": "461715",
    "gasPrice": "1000000007",
    "protocol": "null"
  }
}
