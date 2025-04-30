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
        :title="$t('modals.console.displayRequest.title')"
        @hidden="reset"
        no-close-on-backdrop
    >
      <ul class="nav nav-tabs nav-tabs-light mt-3" id="nav-tab" role="tablist">
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'curl'}" v-on:click="setTab('curl')" id="request-curl-tab" type="button" role="tab" aria-controls="curl" :aria-selected="activeTab === 'curl'">{{ $t(`modals.console.displayRequest.tabs.curl`) }}</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'php'}" v-on:click="setTab('php')" id="request-php-tab" type="button" role="tab" aria-controls="php" :aria-selected="activeTab === 'php'">{{ $t(`modals.console.displayRequest.tabs.php`) }}</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'java'}" v-on:click="setTab('java')" id="request-java-tab" type="button" role="tab" aria-controls="java" :aria-selected="activeTab === 'java'">{{ $t(`modals.console.displayRequest.tabs.java`) }}</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'js'}" v-on:click="setTab('js')" id="request-js-tab" type="button" role="tab" aria-controls="js" :aria-selected="activeTab === 'js'">{{ $t(`modals.console.displayRequest.tabs.js`) }}</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'ruby'}" v-on:click="setTab('ruby')" id="request-ruby-tab" type="button" role="tab" aria-controls="js" :aria-selected="activeTab === 'ruby'">{{ $t(`modals.console.displayRequest.tabs.ruby`) }}</button>
        </li>
      </ul>

      <div class="tab-content" style="border: none">
        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'curl'}" id="curl" role="tabpanel" aria-labelledby="request-curl-tab">
          <div class="row">
            <code class="mt-3">
              curl -X {{ HTTPVerb }} -H 'client-id: {{ applicationId }}' -H 'api-key: {{ apiKey }}' -H 'Content-type: application/json' -d '{{ JSON.stringify(parameters) }}' https://backend.compose.liveidentity.com/api/v3/blockchains/{{ blockchain }}/contracts/{{ contractAddress }}/functions/{{ method.description.name }}/{{ verb }}
            </code>
          </div>
        </div>

        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'php'}" id="php" role="tabpanel" aria-labelledby="request-php-tab">
          <div class="row">
            <v-ace-editor v-model:value="phpRequest" @init="editorInit" lang="php" theme="chrome" style="height: 300px"></v-ace-editor>
          </div>
        </div>

        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'java'}" id="java" role="tabpanel" aria-labelledby="request-java-tab">
          <div class="row">
            <v-ace-editor v-model:value="javaRequest" @init="editorInit" lang="java" theme="chrome" style="height: 300px"></v-ace-editor>
          </div>
        </div>

        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'js'}" id="js" role="tabpanel" aria-labelledby="request-js-tab">
          <div class="row">
            <v-ace-editor v-model:value="jsRequest" @init="editorInit" lang="javascript" theme="chrome" style="height: 300px"></v-ace-editor>
          </div>
        </div>

        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'ruby'}" id="ruby" role="tabpanel" aria-labelledby="request-ruby-tab">
          <div class="row">
            <v-ace-editor v-model:value="rubyRequest" @init="editorInit" lang="ruby" theme="chrome" style="height: 300px"></v-ace-editor>
          </div>
        </div>
      </div>

      <template v-slot:footer>
        <b-button
            variant="primary"
            @click="handleOk"
        >
          {{ $t('modals.console.displayRequest.form.ok') }}
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { VAceEditor } from 'vue3-ace-editor';
import { BModal } from 'bootstrap-vue-next'

export default {
  components: {
    BModal,
    VAceEditor
  },
  methods: {
    show(method, blockchain, contractAddress, applicationId, apiKey, walletPassword){
      this.method = method;

      this.contractAddress = contractAddress;
      this.blockchain = blockchain;
      this.applicationId = (applicationId !== '')?applicationId:'YOUR_APPLICATION_ID';
      this.apiKey = (apiKey !== '')?apiKey:'YOUR_API_KEY';
      this.walletPassword = (walletPassword !== '')?walletPassword:'YOUR_WALLET_PASSWORD';
      this.tokenValue = (method.tokenValue)?method.tokenValue:'YOUR_TOKEN_VALUE';

      if ( this.method.description.stateMutability === 'view' || this.method.description.stateMutability === 'pure' ){
        this.readOnly = true;
        this.verb = 'read';
        this.HTTPVerb = 'PUT'
        this.parameters = {
          wallet: {
            address: this.contractAddress
          },
          parameters: this.method.description.inputs
        }
      } else {
        this.readOnly = false;
        this.verb = 'eval';
        this.HTTPVerb = 'POST'
        this.parameters = {
          walletPassword: this.walletPassword,
          tokenValue: this.tokenValue,
          parameters: this.method.description.inputs
        }
      }

      this.phpRequest = `<?php
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://backend.compose.liveidentity.com/api/v3/blockchains/${this.blockchain}/contracts/${this.contractAddress}/functions/${this.method.description.name}/${this.verb}');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, '${this.HTTPVerb}');
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'client-id: ${this.applicationId}',
    'api-key: ${this.apiKey}',
    'Content-type: application/json',
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, '${JSON.stringify(this.parameters)}');

$response = curl_exec($ch);

curl_close($ch);`

      this.javaRequest = `import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create('https://backend.compose.liveidentity.com/api/v3/blockchains/${this.blockchain}/contracts/${this.contractAddress}/functions/${this.method.description.name}/${this.verb}'))
    .${this.HTTPVerb}(BodyPublishers.ofString('${JSON.stringify(this.parameters)}'))
    .setHeader("client-id", '${this.applicationId}')
    .setHeader("api-key", '${this.apiKey}')
    .setHeader("Content-type", "application/json")
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());`;

      this.jsRequest = `fetch('https://backend.compose.liveidentity.com/api/v3/blockchains/${this.blockchain}/contracts/${this.contractAddress}/functions/${this.method.description.name}/${this.verb}', {
  method: '${this.HTTPVerb}',
  headers: {
    'client-id': '${this.applicationId}',
    'api-key': '${this.apiKey}',
    'Content-type': 'application/json'
  },
  body: ${JSON.stringify(this.parameters, null, 4)}
});`;

      this.rubyRequest = `require 'net/http'
require 'json'

uri = URI('https://backend.compose.liveidentity.com/api/v3/blockchains/${this.blockchain}/contracts/${this.contractAddress}/functions/${this.method.description.name}/${this.verb}')
req = Net::HTTP::${this.HTTPVerb.toLowerCase().charAt(0).toUpperCase() + this.HTTPVerb.toLowerCase().slice(1)};
      }.new(uri)
req.content_type = 'application/json'
req['client-id'] = '${this.applicationId}'
req['api-key'] = '${this.apiKey}'

req.body = ${JSON.stringify(this.parameters, null, 4)}.to_json

req_options = {
  use_ssl: uri.scheme == "https"
}
res = Net::HTTP.start(uri.hostname, uri.port, req_options) do |http|
  http.request(req)
end`;

      this.$refs['modal'].show();
    },
    editorInit(editor){
      editor.setReadOnly(true);
      editor.setOption('wrap', true);
      editor.setOption('fontSize', '15px');

      editor.renderer.setShowGutter(false);

      // Automatically set the height of the v-ace-editor
      editor.setOptions({
        maxLines: Infinity
      });
    },
    reset(){
      this.method = {
        description: {
          name: ''
        }
      };

      this.parameters = null;
      this.contractAddress = null;
    },
    setTab(tab) {
      this.activeTab = tab;
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    },
  },
  data() {
    return {
      activeTab: 'curl',
      blockchain: null,
      contractAddress: null,
      applicationId: '',
      apiKey: '',
      walletPassword: '',
      tokenValue: '',
      jwt: null,
      phpRequest: null,
      javaRequest: null,
      jsRequest: null,
      rubyRequest: null,
      parameters: null,
      method: {
        description: {
          name: ''
        }
      },
      readOnly: true,
      verb: 'read',
      HTTPVerb: 'POST'
    }
  }
}
</script>
