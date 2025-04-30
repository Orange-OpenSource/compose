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

module.exports.deployments = {
  list: [
    {
      "deploymentId": "3ef1fb08-8d69-4085-a6b0-104ed92a3026",
      "name": "Store.json",
      "userId": "f5453f60-2390-48cc-9caa-50c6d143f400",
      "blockchain": "abfTestnet",
      "abi": [
        {
          "inputs": [],
          "stateMutability": "nonpayable",
          "type": "constructor"
        },
        {
          "anonymous": false,
          "inputs": [
            {
              "indexed": false,
              "internalType": "address",
              "name": "sender",
              "type": "address"
            },
            {
              "indexed": false,
              "internalType": "uint256",
              "name": "amount",
              "type": "uint256"
            }
          ],
          "name": "NewStore",
          "type": "event"
        },
        {
          "anonymous": false,
          "inputs": [
            {
              "indexed": false,
              "internalType": "address",
              "name": "sender",
              "type": "address"
            },
            {
              "indexed": false,
              "internalType": "uint256",
              "name": "amount",
              "type": "uint256"
            }
          ],
          "name": "StoreChanged",
          "type": "event"
        },
        {
          "inputs": [
            {
              "internalType": "uint256",
              "name": "a",
              "type": "uint256"
            }
          ],
          "name": "set",
          "outputs": [],
          "stateMutability": "nonpayable",
          "type": "function"
        },
        {
          "inputs": [
            {
              "internalType": "uint256",
              "name": "b",
              "type": "uint256"
            }
          ],
          "name": "add",
          "outputs": [
            {
              "internalType": "uint256",
              "name": "",
              "type": "uint256"
            }
          ],
          "stateMutability": "view",
          "type": "function"
        }
      ],
      "compiler": {
        "name": "solc",
        "version": "0.8.14+commit.80d49f37.Emscripten.clang"
      },
      "bytecode": "0x60806040526000805534801561001457600080fd5b507f043cc306157a91d747b36aba0e235bbbc5771d75aba162f6e5540767d22673c63360006040516100479291906100e4565b60405180910390a161010d565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600061007f82610054565b9050919050565b61008f81610074565b82525050565b6000819050919050565b6000819050919050565b6000819050919050565b60006100ce6100c96100c484610095565b6100a9565b61009f565b9050919050565b6100de816100b3565b82525050565b60006040820190506100f96000830185610086565b61010660208301846100d5565b9392505050565b6102988061011c6000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c80631003e2d21461003b57806360fe47b11461006b575b600080fd5b6100556004803603810190610050919061011c565b610087565b6040516100629190610158565b60405180910390f35b6100856004803603810190610080919061011c565b61009e565b005b60008160005461009791906101a2565b9050919050565b7f41443eb6de95f473c52147375646c6b6d28c1bff4cfd7691affafc01522468e733826040516100cf929190610239565b60405180910390a18060008190555050565b600080fd5b6000819050919050565b6100f9816100e6565b811461010457600080fd5b50565b600081359050610116816100f0565b92915050565b600060208284031215610132576101316100e1565b5b600061014084828501610107565b91505092915050565b610152816100e6565b82525050565b600060208201905061016d6000830184610149565b92915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b60006101ad826100e6565b91506101b8836100e6565b9250827fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff038211156101ed576101ec610173565b5b828201905092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610223826101f8565b9050919050565b61023381610218565b82525050565b600060408201905061024e600083018561022a565b61025b6020830184610149565b939250505056fea2646970667358221220cc0e7877bbaad5081cdfacfd9a396e5f3a2bc2b771c3a4f835f9319c6659904464736f6c634300080e0033",
      "deployedBytecode": "0x608060405234801561001057600080fd5b50600436106100365760003560e01c80631003e2d21461003b57806360fe47b11461006b575b600080fd5b6100556004803603810190610050919061011c565b610087565b6040516100629190610158565b60405180910390f35b6100856004803603810190610080919061011c565b61009e565b005b60008160005461009791906101a2565b9050919050565b7f41443eb6de95f473c52147375646c6b6d28c1bff4cfd7691affafc01522468e733826040516100cf929190610239565b60405180910390a18060008190555050565b600080fd5b6000819050919050565b6100f9816100e6565b811461010457600080fd5b50565b600081359050610116816100f0565b92915050565b600060208284031215610132576101316100e1565b5b600061014084828501610107565b91505092915050565b610152816100e6565b82525050565b600060208201905061016d6000830184610149565b92915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b60006101ad826100e6565b91506101b8836100e6565b9250827fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff038211156101ed576101ec610173565b5b828201905092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610223826101f8565b9050919050565b61023381610218565b82525050565b600060408201905061024e600083018561022a565b61025b6020830184610149565b939250505056fea2646970667358221220cc0e7877bbaad5081cdfacfd9a396e5f3a2bc2b771c3a4f835f9319c6659904464736f6c634300080e0033",
      "networks": {},
      "updatedAt": "2023-12-10T15:01:01.999Z"
    }
  ],
  ongoing: [
    {
      "blockchain": "abfTestnet",
      "deploymentId": "8aa865be-a5e3-48d7-97c5-987317cbdf34",
      "userId": "f5453f60-2390-48cc-9caa-50c6d143f400",
      "updatedAt": "1702387478514"
    },
    {
      "blockchain": "abfTestnet",
      "deploymentId": "fe84e172-b349-4d66-aaab-076ece68b498",
      "userId": "f5453f60-2390-48cc-9caa-50c6d143f400",
      "updatedAt": "1703074498365"
    }
  ]
}
