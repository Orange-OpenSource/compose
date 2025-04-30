package smart_contracts.controllers.vm.evm

import org.specs2.mutable.Specification
import org.web3j.protocol.core.methods.response.Log
import smart_contracts.model.dao.Abi

import scala.jdk.CollectionConverters.SeqHasAsJava

class EvmEventDecoderTest extends Specification {
sequential
  /*
        event Winner(
                string messenger,
                string username,
                string nome,
                string cognome,
                string email
            );

            emit Winner ("Il Vincitore", "111111", "2222" , "3333","4444");

        "logs": [
                {
                    "address": "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8",
                    "blockHash": "0x2de8d9ff07b91523359c08a0004131d5839a7d4ff5aa1bd00843777a5e096ca1",
                    "blockNumber": 8726746,
                    "data": "0x00000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000c496c2056696e6369746f7265000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177716500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000477657771000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003717765000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177657100000000000000000000000000000000000000000000000000000000",
                    "logIndex": 16,
                    "removed": false,
                    "topics": [
                        "0x8cdd8faf489944941fc651bffdc4a404b57bddc1c35eccf8531de89a4422d777"
                    ],
                    "transactionHash": "0xf29b396e697713937dcf56e3f0c0dd2577beeac48c67a8168ebc084f75daf7b2",
                    "transactionIndex": 12,
                    "id": "log_a7c1ba91"
                }
            ]
         */



  "EvmEventDecoderTest" should {
    "decode" in {

      val abi: Abi = Abi(
        Some("_id1"),
        "my_abi",
        "abf-testnet",
        """
        [
        {
                "anonymous": false,
                "inputs": [
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "messenger",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "username",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "nome",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "cognome",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "email",
                    "type": "string"
                  }
                ],
                "name": "Winner",
                "type": "event"
              },
        {
          "inputs": [
            {
              "internalType": "uint8",
              "name": "value",
              "type": "uint8"
            }
          ],
          "name": "encodedEvent",
          "outputs": [
            {
              "internalType": "bytes32",
              "name": "",
              "type": "bytes32"
            }
          ],
          "stateMutability": "pure",
          "type": "function"
        }
      ]
      """, "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8", "userId")

      val log = new Log(
        false,
        "16",
        "4",
        "0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc",
        "0x2de8d9ff07b91523359c08a0004131d5839a7d4ff5aa1bd00843777a5e096ca1",
        "8726746",
        "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8",
        "0x00000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000c496c2056696e6369746f7265000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177716500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000477657771000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003717765000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177657100000000000000000000000000000000000000000000000000000000",
        "1",
        List("0x8cdd8faf489944941fc651bffdc4a404b57bddc1c35eccf8531de89a4422d777").asJava)

      val smartContract = EvmSmartContract(abi)
      val r = EvmEventDecoder(smartContract).decode(log)

      r.isSuccess mustEqual true
      r.get.event.name mustEqual "Winner"
      r.get.value.toString() mustEqual "{\"address\":\"0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8\",\"blockNumber\":8726746,\"name\":\"Winner\",\"parameters\":[{\"name\":\"cognome\",\"type\":\"string\",\"value\":\"qwe\"},{\"name\":\"nome\",\"type\":\"string\",\"value\":\"wewq\"},{\"name\":\"username\",\"type\":\"string\",\"value\":\"qwqe\"},{\"name\":\"email\",\"type\":\"string\",\"value\":\"qweq\"},{\"name\":\"messenger\",\"type\":\"string\",\"value\":\"Il Vincitore\"}]}"
    }

    "fail when trying to decode" in {

      val abi: Abi = Abi(
        Some("_id1"),
        "my_abi",
        "abf-testnet",
        """
        [
        {
                "anonymous": false,
                "inputs": [
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "messenger",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "username",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "nome",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "cognome",
                    "type": "string"
                  },
                  {
                    "indexed": false,
                    "internalType": "string",
                    "name": "email",
                    "type": "string"
                  }
                ],
                "name": "UnlknownEvent",
                "type": "event"
              },
        {
          "inputs": [
            {
              "internalType": "uint8",
              "name": "value",
              "type": "uint8"
            }
          ],
          "name": "encodedEvent",
          "outputs": [
            {
              "internalType": "bytes32",
              "name": "",
              "type": "bytes32"
            }
          ],
          "stateMutability": "pure",
          "type": "function"
        }
      ]
      """, "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8", "userId")

      val log = new Log(
        false,
        "16",
        "4",
        "0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc",
        "0x2de8d9ff07b91523359c08a0004131d5839a7d4ff5aa1bd00843777a5e096ca1",
        "8726746",
        "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8",
        "0x00000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000c496c2056696e6369746f7265000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177716500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000477657771000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003717765000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177657100000000000000000000000000000000000000000000000000000000",
        "1",
        List("0x8cdd8faf489944941fc651bffdc4a404b57bddc1c35eccf8531de89a4422d777").asJava)

      val smartContract = EvmSmartContract(abi)
      val r = EvmEventDecoder(smartContract).decode(log)
      r.isFailure mustEqual true
    }
    "decode bytes 32" in {

      val abi: Abi = Abi(
        Some("_id1"),
        "my_abi",
        "abf-testnet",
        """
            [
	{
		"anonymous": false,
		"inputs": [
			{
				"indexed": false,
				"internalType": "bytes32",
				"name": "_from",
				"type": "bytes32"
			},
			{
				"indexed": false,
				"internalType": "uint256",
				"name": "num",
				"type": "uint256"
			}
		],
		"name": "test",
		"type": "event"
	},
	{
		"inputs": [],
		"name": "get",
		"outputs": [
			{
				"internalType": "uint256",
				"name": "",
				"type": "uint256"
			}
		],
		"stateMutability": "view",
		"type": "function"
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
	}
]
          """, "0x2F8895b08D8F226b19895d46154faB7096fB2593", "userId")

      val log = new Log(
        false,
        "0x1",
        "0x3c",
        "0xfbd1ea839fa55452feb14d9256d16fe7c9e2c7676d7dae41a43ca9b437a7cd7d",
        "0x215770bad9d427116a3d470ed33306a7f635667c0a4793ca04aea233c1e9d6aa",
        "0x0",
        "0x2F8895b08D8F226b19895d46154faB7096fB2593",
        "0x3078373436353733373400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a",
        "1",
        List("0x857e382ff9b8fd5e21fee0def5f187d3884a71ab3a22480ca38a6843cd77fafb").asJava)

      val smartContract = EvmSmartContract(abi)
      val r = EvmEventDecoder(smartContract).decode(log)

      r.isSuccess mustEqual true
      r.get.event.name mustEqual "test"
      r.get.value.toString() mustEqual "{\"address\":\"0x2F8895b08D8F226b19895d46154faB7096fB2593\",\"blockNumber\":0,\"name\":\"test\",\"parameters\":[{\"name\":\"_from\",\"type\":\"bytes32\",\"value\":\"3078373436353733373400000000000000000000000000000000000000000000\"},{\"name\":\"num\",\"type\":\"uint256\",\"value\":\"10\"}]}"
    }

  }
}
