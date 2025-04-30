<!--
  - /*
  -  *
  -  *  * Software Name : Compose
  -  *  * SPDX-FileCopyrightText: Copyright (c) Orange SA
  -  *  * SPDX-License-Identifier:  MIT
  -  *  *
  -  *  * This software is distributed under the MIT License,
  -  *  * see the "LICENSE.txt" file for more details or https://spdx.org/licenses/MIT.html
  -  *  *
  -  *  * <Authors: optional: authors list / see CONTRIBUTORS>
  -  *
  -  */
  -->

<template>
  <div>
    <b-modal
      static
      size="xl"
      ref="modal"
      :title="$t('modals.console.displayDeploymentInfo.title')"
      no-close-on-backdrop
    >
      <div>
        <h2 id="comment-a-fonctionne-">Comment ça fonctionne ?</h2>
        <h3 id="options">Options</h3>
        <pre><code class="lang-json"><span class="hljs-string">"options"</span>: {
  <span class="hljs-string">"version"</span>: <span class="hljs-string">"0.8.14"</span>,
  <span class="hljs-string">"evmVersion"</span>: <span class="hljs-string">"byzantium"</span>,
  <span class="hljs-string">"optimizerRuns"</span>: <span class="hljs-number">200</span>,
  <span class="hljs-string">"httpProxy"</span>: <span class="hljs-string">"http://fpc.itn.intraorange:8080"</span>,
  <span class="hljs-string">"httpsProxy"</span>: <span class="hljs-string">"http://fpc.itn.intraorange:8080"</span>
}
</code></pre>
        <h4 id="description-des-options">Description des options</h4>
        <ul>
          <li><code>&quot;version&quot;: &quot;0.8.14&quot;</code> : requis, spécifie la version de Solidity à utiliser pour la compilation.</li>
          <li><code>&quot;evmVersion&quot;: &quot;byzantium&quot;</code> : requis, spécifie la version de l&#39;EVM pour laquelle compiler.</li>
          <li><code>&quot;optimizerRuns&quot;: 200</code> : requis, optimise le code en fonction du nombre d&#39;exécutions prévues. Les valeurs inférieures optimisent pour un faible coût de déploiement initial, tandis que les valeurs supérieures optimisent pour une utilisation fréquente.</li>
          <li><code>&quot;httpProxy&quot;: &quot;http://fpc.itn.intraorange:8080&quot;</code> : optionnel, permet d&#39;utiliser un proxy HTTP.</li>
          <li><code>&quot;httpsProxy&quot;: &quot;http://fpc.itn.intraorange:8080&quot;</code> : optionnel, permet d&#39;utiliser un proxy HTTPS.</li>
        </ul>
        <h3 id="migration">Migration</h3>
        <pre><code class="lang-json"><span class="hljs-string">"migration"</span>: [
  {
    <span class="hljs-string">"name"</span>: <span class="hljs-string">"LibraryName"</span>,
    <span class="hljs-string">"links"</span>: []
  },
  {
    <span class="hljs-string">"name"</span>: <span class="hljs-string">"OtherContractName"</span>,
    <span class="hljs-string">"links"</span>: [
      <span class="hljs-string">"LibraryName"</span>
    ],
    <span class="hljs-string">"parameters"</span>: []
  },
  {
    <span class="hljs-string">"name"</span>: <span class="hljs-string">"MainContractName"</span>,
    <span class="hljs-string">"parameters"</span>: [
      {
        <span class="hljs-string">"name"</span>: <span class="hljs-string">"a"</span>,
        <span class="hljs-string">"valueType"</span>: <span class="hljs-string">"uint256"</span>,
        <span class="hljs-string">"value"</span>: <span class="hljs-number">10</span>
      },
      {
        <span class="hljs-string">"name"</span>: <span class="hljs-string">"b"</span>,
        <span class="hljs-string">"valueType"</span>: <span class="hljs-string">"string"</span>,
        <span class="hljs-string">"value"</span>: <span class="hljs-string">"test"</span>
      },
      {
        <span class="hljs-string">"name"</span>: <span class="hljs-string">"c"</span>,
        <span class="hljs-string">"valueType"</span>: <span class="hljs-string">"address"</span>,
        <span class="hljs-string">"value"</span>: <span class="hljs-string">"0x0000000000000000000000000000000000000000"</span>
      },
      {
        <span class="hljs-string">"name"</span>: <span class="hljs-string">"d"</span>,
        <span class="hljs-string">"valueType"</span>: <span class="hljs-string">"bool"</span>,
        <span class="hljs-string">"value"</span>: false
      }
    ],
    <span class="hljs-string">"links"</span>: [
      <span class="hljs-string">"LibraryName"</span>,
      <span class="hljs-string">"OtherContractName"</span>
    ]
  }
]
</code></pre>
        <p>Migration contient une liste d&#39;objets correspondant aux contrats / librairies à déployer.</p>
        <ul>
          <li><code>&quot;name&quot;: &quot;MainContractName&quot;</code> : requis, nom du contrat / de la librairie à déployer.</li>
          <li><code>&quot;links&quot;: [ &quot;LibraryName&quot;, &quot;OtherContractName&quot; ]</code> : requis, liste des contrats / librairies à déployer que le contrat actuel utilise. Si le contrat actuel est lié à des interfaces ou des contrats abstraits qui n&#39;ont pas à être déployés, laisser la liste vide : <code>[]</code>.</li>
          <li><code>&quot;parameters&quot;: [ { &quot;name&quot;: &quot;a&quot;, &quot;valueType&quot;: &quot;uint256&quot;, &quot;value&quot;: 10 } ]</code> : optionnel si aucun paramètre, liste d&#39;objets correspondant aux paramètres du constructeur du contrat. <code>name</code> correspond au nom du paramètre dans le constructeur, <code>valueType</code> correspond au type du paramètre (bool, int, uint, string, address...) et <code>value</code> correspond à la valeur à assigner au paramètre.</li>
        </ul>
        <h2 id="exemples-d-utilisation">Exemples d&#39;utilisation</h2>
        <h3 id="un-seul-contrat-pas-de-param-tre-pas-de-d-pendance">Un seul contrat, pas de paramètre, pas de dépendance</h3>
        <ul>
          <li><p>Contrat :</p>
            <pre><code class="lang-solidity"><span class="hljs-comment">// SPDX-License-Identifier: UNLICENSED</span>
<span class="hljs-keyword">pragma</span> solidity ^<span class="hljs-number">0.8</span>.0;

contract TextInput {
  <span class="hljs-built_in">string</span> inputText;

  <span class="hljs-built_in">function</span> setText(<span class="hljs-built_in">string</span> memory _text) <span class="hljs-keyword">public</span> {
    inputText = _text;
  }

  <span class="hljs-built_in">function</span> getText() <span class="hljs-keyword">public</span> view returns (<span class="hljs-built_in">string</span> memory) {
    <span class="hljs-keyword">return</span> inputText;
  }
}
</code></pre>
          </li>
          <li><p>Options pour le déploiement :</p>
            <pre><code class="lang-json">{
  <span class="hljs-attr">"options"</span>: {
    <span class="hljs-attr">"version"</span>: <span class="hljs-string">"0.8.14"</span>,
    <span class="hljs-attr">"evmVersion"</span>: <span class="hljs-string">"byzantium"</span>,
    <span class="hljs-attr">"optimizerRuns"</span>: <span class="hljs-number">200</span>
  },
  <span class="hljs-attr">"migration"</span>: [
    {
      <span class="hljs-attr">"name"</span>: <span class="hljs-string">"TextInput"</span>,
      <span class="hljs-attr">"links"</span>: []
    }
  ]
}
</code></pre>
          </li>
        </ul>
        <h3 id="un-seul-contrat-des-param-tres-pas-de-d-pendance">Un seul contrat, des paramètres, pas de dépendance</h3>
        <ul>
          <li><p>Contrats :</p>
            <pre><code class="lang-solidity"><span class="hljs-comment">// SPDX-License-Identifier: UNLICENSED</span>
pragma solidity ^<span class="hljs-number">0.8</span><span class="hljs-number">.0</span>;

contract SimpleContract {
  uint256 <span class="hljs-keyword">public</span> <span class="hljs-built_in">number</span>;
  address <span class="hljs-keyword">public</span> userAddress;
  bool <span class="hljs-keyword">public</span> flag;

  <span class="hljs-keyword">constructor</span>(uint256 _number, address _userAddress, bool _flag) {
    <span class="hljs-built_in">number</span> = _number;
    userAddress = _userAddress;
    flag = _flag;
  }

  <span class="hljs-function"><span class="hljs-keyword">function</span> <span class="hljs-title">setValues</span>(<span class="hljs-params">uint256 _number, address _userAddress, bool _flag</span>) <span class="hljs-title">public</span> </span>{
    <span class="hljs-built_in">number</span> = _number;
    userAddress = _userAddress;
    flag = _flag;
  }
}
</code></pre>
          </li>
          <li><p>Options pour le déploiement :</p>
            <pre><code class="lang-json">  {
    <span class="hljs-attr">"options"</span>: {
      <span class="hljs-attr">"version"</span>: <span class="hljs-string">"0.8.14"</span>,
      <span class="hljs-attr">"evmVersion"</span>: <span class="hljs-string">"byzantium"</span>,
      <span class="hljs-attr">"optimizerRuns"</span>: <span class="hljs-number">200</span>
    },
    <span class="hljs-attr">"migration"</span>: [
      {
        <span class="hljs-attr">"name"</span>: <span class="hljs-string">"SimpleContract"</span>,
        <span class="hljs-attr">"links"</span>: [],
        <span class="hljs-attr">"parameters"</span>: [
          {
            <span class="hljs-attr">"name"</span>: <span class="hljs-string">"_number"</span>,
            <span class="hljs-attr">"valueType"</span>: <span class="hljs-string">"uint256"</span>,
            <span class="hljs-attr">"value"</span>: <span class="hljs-number">123</span>
          },
          {
            <span class="hljs-attr">"name"</span>: <span class="hljs-string">"_userAddress"</span>,
            <span class="hljs-attr">"valueType"</span>: <span class="hljs-string">"address"</span>,
            <span class="hljs-attr">"value"</span>: <span class="hljs-string">"0x0000000000000000000000000000000000000000"</span>
          },
          {
            <span class="hljs-attr">"name"</span>: <span class="hljs-string">"_flag"</span>,
            <span class="hljs-attr">"valueType"</span>: <span class="hljs-string">"bool"</span>,
            <span class="hljs-attr">"value"</span>: <span class="hljs-literal">true</span>
          }
        ]
      }
    ]
  }
</code></pre>
          </li>
        </ul>
        <h3 id="une-librairie-et-plusieurs-contrats-des-param-tres-plusieurs-d-pendances">Une librairie et plusieurs contrats, des paramètres, plusieurs dépendances</h3>
        <ul>
          <li><p>Contrats :</p>
            <pre><code class="lang-solidity"><span class="hljs-comment">// SPDX-License-Identifier: UNLICENSED</span>
pragma solidity ^<span class="hljs-number">0.8</span><span class="hljs-number">.0</span>;

library MathLib {
  <span class="hljs-function"><span class="hljs-keyword">function</span> <span class="hljs-title">add</span><span class="hljs-params">(uint256 a, uint256 b)</span> <span class="hljs-title">internal</span> <span class="hljs-title">pure</span> <span class="hljs-title">returns</span> <span class="hljs-params">(uint256)</span> </span>{
    <span class="hljs-keyword">return</span> a + b;
  }

  <span class="hljs-function"><span class="hljs-keyword">function</span> <span class="hljs-title">subtract</span><span class="hljs-params">(uint256 a, uint256 b)</span> <span class="hljs-title">internal</span> <span class="hljs-title">pure</span> <span class="hljs-title">returns</span> <span class="hljs-params">(uint256)</span> </span>{
    <span class="hljs-keyword">return</span> a - b;
  }
}
</code></pre>
            <pre><code class="lang-solidity"><span class="hljs-comment">// SPDX-License-Identifier: UNLICENSED</span>
pragma solidity ^<span class="hljs-number">0.8</span><span class="hljs-number">.0</span>;

<span class="hljs-meta"><span class="hljs-meta-keyword">import</span> "./MathLib.sol";</span>

contract FirstContract {
  using MathLib <span class="hljs-keyword">for</span> uint256;

  <span class="hljs-function"><span class="hljs-keyword">function</span> <span class="hljs-title">execute</span><span class="hljs-params">(uint256 a, uint256 b, uint256 c)</span> <span class="hljs-title">public</span> <span class="hljs-title">view</span> <span class="hljs-title">returns</span> <span class="hljs-params">(uint256)</span> </span>{
    <span class="hljs-keyword">return</span> a.add(b).add(c);
  }
}
</code></pre>
            <pre><code class="lang-solidity"><span class="hljs-comment">// SPDX-License-Identifier: UNLICENSED</span>
pragma solidity ^<span class="hljs-number">0.8</span>.<span class="hljs-number">0</span>;

<span class="hljs-keyword">import</span> <span class="hljs-string">"./FirstContract.sol"</span>;
<span class="hljs-keyword">import</span> <span class="hljs-string">"./MathLib.sol"</span>;

contract SecondContract <span class="hljs-keyword">is</span> FirstContract {
  using MathLib <span class="hljs-keyword">for</span> uint<span class="hljs-number">256</span>;

  uint<span class="hljs-number">256</span> public <span class="hljs-keyword">value</span>;

  constructor(uint<span class="hljs-number">256</span> <span class="hljs-number">_</span><span class="hljs-keyword">value</span>) {
    <span class="hljs-keyword">value</span> = <span class="hljs-number">_</span><span class="hljs-keyword">value</span>;
  }

  <span class="hljs-keyword">function</span> executeDerived(uint<span class="hljs-number">256</span> a, uint<span class="hljs-number">256</span> b) public view returns (uint<span class="hljs-number">256</span>) {
    uint<span class="hljs-number">256</span> result = execute(a, b);
    <span class="hljs-keyword">return</span> result.subtract(<span class="hljs-keyword">value</span>);
  }
}
</code></pre>
          </li>
          <li><p>Options pour le déploiement :</p>
            <pre><code class="lang-json">{
  <span class="hljs-attr">"options"</span>: {
    <span class="hljs-attr">"version"</span>: <span class="hljs-string">"0.8.14"</span>,
    <span class="hljs-attr">"evmVersion"</span>: <span class="hljs-string">"byzantium"</span>,
    <span class="hljs-attr">"optimizerRuns"</span>: <span class="hljs-number">200</span>
  },
  <span class="hljs-attr">"migration"</span>: [
    {
      <span class="hljs-attr">"name"</span>: <span class="hljs-string">"MathLib"</span>,
      <span class="hljs-attr">"links"</span>: []
    },
    {
      <span class="hljs-attr">"name"</span>: <span class="hljs-string">"FirstContract"</span>,
      <span class="hljs-attr">"links"</span>: [
        <span class="hljs-string">"MathLib"</span>
      ],
      <span class="hljs-attr">"parameters"</span>: []
    },
    {
      <span class="hljs-attr">"name"</span>: <span class="hljs-string">"SecondContract"</span>,
      <span class="hljs-attr">"links"</span>: [
        <span class="hljs-string">"MathLib"</span>,
        <span class="hljs-string">"FirstContract"</span>
      ],
      <span class="hljs-attr">"parameters"</span>: [
        {
          <span class="hljs-attr">"name"</span>: <span class="hljs-string">"_value"</span>,
          <span class="hljs-attr">"valueType"</span>: <span class="hljs-string">"uint256"</span>,
          <span class="hljs-attr">"value"</span>: <span class="hljs-number">123</span>
        }
      ]
    }
  ]
}
</code></pre>
          </li>
        </ul>

      </div>


      <template v-slot:footer>
        <b-button
          variant="primary"
          @click="handleOk"
        >
          {{ $t('modals.console.displayDeploymentInfo.form.ok') }}
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next'

export default {
  components: {
    BModal
  },
  methods: {
    show(){
      this.$refs['modal'].show();
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    },
  },
  data() {
    return {}
  }
}
</script>
