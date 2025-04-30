# Guide de déploiement

## Comment ça fonctionne ?

### Options

```json
"options": {
  "version": "0.8.14",
  "evmVersion": "byzantium",
  "optimizerRuns": 200,
  "httpProxy": "http://fpc.itn.intraorange:8080",
  "httpsProxy": "http://fpc.itn.intraorange:8080"
}
```

#### Description des options

- `"version": "0.8.14"` : requis, spécifie la version de Solidity à utiliser pour la compilation.
- `"evmVersion": "byzantium"` : requis, spécifie la version de l'EVM pour laquelle compiler.
- `"optimizerRuns": 200` : requis, optimise le code en fonction du nombre d'exécutions prévues. Les valeurs inférieures optimisent pour un faible coût de déploiement initial, tandis que les valeurs supérieures optimisent pour une utilisation fréquente.
- `"httpProxy": "http://fpc.itn.intraorange:8080"` : optionnel, permet d'utiliser un proxy HTTP.
- `"httpsProxy": "http://fpc.itn.intraorange:8080"` : optionnel, permet d'utiliser un proxy HTTPS.

### Migration

```json
"migration": [
  {
    "name": "LibraryName",
    "links": []
  },
  {
    "name": "OtherContractName",
    "links": [
      "LibraryName"
    ],
    "parameters": []
  },
  {
    "name": "MainContractName",
    "parameters": [
      {
        "name": "a",
        "valueType": "uint256",
        "value": 10
      },
      {
        "name": "b",
        "valueType": "string",
        "value": "test"
      },
      {
        "name": "c",
        "valueType": "address",
        "value": "0x0000000000000000000000000000000000000000"
      },
      {
        "name": "d",
        "valueType": "bool",
        "value": false
      }
    ],
    "links": [
      "LibraryName",
      "OtherContractName"
    ]
  }
]
```

Migration contient une liste d'objets correspondant aux contrats / librairies à déployer.

- `"name": "MainContractName"` : requis, nom du contrat / de la librairie à déployer.
- `"links": [ "LibraryName", "OtherContractName" ]` : requis, liste des contrats / librairies à déployer que le contrat actuel utilise. Si le contrat actuel est lié à des interfaces ou des contrats abstraits qui n'ont pas à être déployés, laisser la liste vide : `[]`.
- `"parameters": [ { "name": "a", "valueType": "uint256", "value": 10 } ]` : optionnel si aucun paramètre, liste d'objets correspondant aux paramètres du constructeur du contrat. `name` correspond au nom du paramètre dans le constructeur, `valueType` correspond au type du paramètre (bool, int, uint, string, address...) et `value` correspond à la valeur à assigner au paramètre.

## Exemples d'utilisation

### Un seul contrat, pas de paramètre, pas de dépendance

- Contrat :

  ```solidity
  // SPDX-License-Identifier: UNLICENSED
  pragma solidity ^0.8.0;

  contract TextInput {
    string inputText;

    function setText(string memory _text) public {
      inputText = _text;
    }

    function getText() public view returns (string memory) {
      return inputText;
    }
  }
  ```

- Options pour le déploiement :

  ```json
  {
    "options": {
      "version": "0.8.14",
      "evmVersion": "byzantium",
      "optimizerRuns": 200
    },
    "migration": [
      {
        "name": "TextInput",
        "links": []
      }
    ]
  }
  ```

### Un seul contrat, des paramètres, pas de dépendance

- Contrats :

  ```solidity
  // SPDX-License-Identifier: UNLICENSED
  pragma solidity ^0.8.0;

  contract SimpleContract {
    uint256 public number;
    address public userAddress;
    bool public flag;

    constructor(uint256 _number, address _userAddress, bool _flag) {
      number = _number;
      userAddress = _userAddress;
      flag = _flag;
    }

    function setValues(uint256 _number, address _userAddress, bool _flag) public {
      number = _number;
      userAddress = _userAddress;
      flag = _flag;
    }
  }
  ```

- Options pour le déploiement :

    ```json
    {
      "options": {
        "version": "0.8.14",
        "evmVersion": "byzantium",
        "optimizerRuns": 200
      },
      "migration": [
        {
          "name": "SimpleContract",
          "links": [],
          "parameters": [
            {
              "name": "_number",
              "valueType": "uint256",
              "value": 123
            },
            {
              "name": "_userAddress",
              "valueType": "address",
              "value": "0x0000000000000000000000000000000000000000"
            },
            {
              "name": "_flag",
              "valueType": "bool",
              "value": true
            }
          ]
        }
      ]
    }
    ```

### Une librairie et plusieurs contrats, des paramètres, plusieurs dépendances

- Contrats :

  ```solidity
  // SPDX-License-Identifier: UNLICENSED
  pragma solidity ^0.8.0;

  library MathLib {
    function add(uint256 a, uint256 b) internal pure returns (uint256) {
      return a + b;
    }

    function subtract(uint256 a, uint256 b) internal pure returns (uint256) {
      return a - b;
    }
  }
  ```

  ```solidity
  // SPDX-License-Identifier: UNLICENSED
  pragma solidity ^0.8.0;

  import "./MathLib.sol";

  contract FirstContract {
    using MathLib for uint256;

    function execute(uint256 a, uint256 b, uint256 c) public view returns (uint256) {
      return a.add(b).add(c);
    }
  }
  ```

  ```solidity
  // SPDX-License-Identifier: UNLICENSED
  pragma solidity ^0.8.0;

  import "./FirstContract.sol";
  import "./MathLib.sol";

  contract SecondContract is FirstContract {
    using MathLib for uint256;

    uint256 public value;

    constructor(uint256 _value) {
      value = _value;
    }

    function executeDerived(uint256 a, uint256 b) public view returns (uint256) {
      uint256 result = execute(a, b);
      return result.subtract(value);
    }
  }
  ```

- Options pour le déploiement :

  ```json
  {
    "options": {
      "version": "0.8.14",
      "evmVersion": "byzantium",
      "optimizerRuns": 200
    },
    "migration": [
      {
        "name": "MathLib",
        "links": []
      },
      {
        "name": "FirstContract",
        "links": [
          "MathLib"
        ],
        "parameters": []
      },
      {
        "name": "SecondContract",
        "links": [
          "MathLib",
          "FirstContract"
        ],
        "parameters": [
          {
            "name": "_value",
            "valueType": "uint256",
            "value": 123
          }
        ]
      }
    ]
  }
  ```
