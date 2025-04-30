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
  <div class="container pt-md-1 pb-md-4">
    <nav role="navigation" :aria-label="$t('aria.breadcrumb')" class="d-none d-md-block">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          {{ $t('pages.console.breadcrumb') }}
        </li>
        <li class="breadcrumb-item">
          <router-link to="/console/contracts">
            {{ $t('pages.console.contracts.breadcrumb') }}
          </router-link>
        </li>
        <li class="breadcrumb-item active" aria-current="location">
          {{ contract.address }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <div class="float-end">
          <span
            class="badge rounded-pill"
            v-bind:class="{
              'text-bg-primary': contract.blockchain === 'local',
              'text-white bg-info': contract.blockchain === 'alastria',
              'text-bg-light': contract.blockchain === 'abfTestnet',
              'text-bg-secondary': contract.blockchain !== 'alastria' && contract.blockchain !== 'abfTestnet' && contract.blockchain !== 'local'
            }">{{ $t(`blockchains.${contract.blockchain}`) }}</span>
        </div>
        <h1 class="mb-0">
          <span class="me-2">{{ contract.name }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>
        <small class="d-block text-muted">{{ $t('pages.console.contract.address', {address: contract.address}) }}</small>
        <small class="d-block text-muted">{{ $t('pages.console.contract.id', {id: contract.contractId}) }}</small>
      </div>
    </div>
    <div class="row mt-3">
      <div class="col-12">
        <test-configuration type="full" v-model:api-key="apiKey" v-model:application-id="applicationId" v-model:wallet-password="walletPassword"></test-configuration>
      </div>
    </div>
    <div class="row">
      <ul class="nav nav-tabs nav-tabs-light mt-4" id="nav-tab" role="tablist">
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'functions'}" v-on:click="setTab('functions')" id="contract-functions-tab" type="button" role="tab" aria-controls="functions" :aria-selected="activeTab === 'functions'">{{ $t(`pages.console.contract.tabs.functions`) }}</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'events'}" v-on:click="setTab('events')" id="contract-events-tab" type="button" role="tab" aria-controls="events" :aria-selected="activeTab === 'events'">{{ $t(`pages.console.contract.tabs.events`) }}</button>
        </li>
      </ul>
      <div class="tab-content" style="border: none">
        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'functions'}" id="functions" role="tabpanel" aria-labelledby="contract-functions-tab">
          <div class="row">
            <div class="col-md-7">
              <div class="mt-3 mb-3">
                <h5 class="mb-3">{{ $t(`pages.console.contract.functions.title`, {count: functions.length}) }}</h5>
                <div v-for="(method, index) in functions" :key="index">
                  <h6 class="mb-2" style="font-size: 14px">
                    {{ method.description.name }} ( <input type="text" class="parameter" v-bind:class="{'ms-1': index2 != 0}" v-for="(parameter, index2) in method.description.inputs" :key="index2" v-model="parameter.value" /> )
                    <input v-if="method.description.stateMutability === 'payable'" v-model="method.tokenValue" :placeholder="$t(`pages.console.contract.functions.tokenValue`)" type="text" class="parameter ms-1" style="width: 120px" />
                    <button type="button" class="btn btn-primary btn-sm ms-1" style="padding: 0px 3px" :disabled="isPlaying === true" v-on:click="playFunction(method)"><i class="fas fa-play" style="font-size: 7px"></i></button>
                    <a href="javascript:void(0)" class="ms-2" v-on:click="displayRequest(method)"><i class="fas fa-info-circle position-relative" style="top: 1px"></i></a>
                  </h6>
                  <v-ace-editor class="mb-4" v-model:value="method.documentation" @init="editorInit" lang="javascript" theme="chrome" width="100%" height="200"></v-ace-editor>
                </div>
              </div>
            </div>
            <div class="col-md-5">
              <div class="mt-3 mb-3 ms-3 position-sticky" style="width: 100%; top: 130px">
                <h6 class="mb-3">{{ $t(`pages.console.contract.console.title`) }}</h6>
                <div id="console" class="executionConsole" v-html="result">
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'events'}" id="events" role="tabpanel" aria-labelledby="contract-events-tab">
          <div class="row">
            <div class="col-12">
              <div class="mt-3 mb-3">
                <h5 class="mb-3">{{ $t(`pages.console.contract.events.title`, {count: events.length}) }}</h5>
                <b-table
                    v-if="events && events.length > 0"
                    id="events-table"
                    ref="table"
                    :items="events"
                    :fields="eventsFields"
                    hover
                >
                  <template v-slot:cell(name)="data">
                    <router-link :to="{path: `/console/contracts/${contractAddress}/events/${data.item['name']}`}" class="me-2" :title="$t('pages.console.contract.events.displayEvent')">
                      {{ data.value }}
                    </router-link>
                  </template>

                  <template v-slot:cell(inputs)="data">
                    {{ data.item.inputs.map(function(elem){return `${elem.name} (${elem.type})`;}).join(', ')  }}
                  </template>

                  <template v-slot:cell(actions)="data">
                    <router-link :to="{path: `/console/contracts/${contractAddress}/events/${data.item['name']}`}" class="me-2" :title="$t('pages.console.contract.events.displayEvent')">
                      <i class="fas fa-eye"></i>
                    </router-link>
                  </template>
                </b-table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <display-request ref="displayRequestModal"></display-request>
    <display-event ref="displayEventModal"></display-event>
  </div>
</template>
<script>
import DisplayRequest from "@/frontend/vue/modals/console/DisplayRequest.vue";
import DisplayEvent from "@/frontend/vue/modals/console/DisplayEvent.vue";
import { VAceEditor } from 'vue3-ace-editor';
import TestConfiguration from "@/frontend/vue/components/TestConfiguration.vue";

export default {
  components: {
    TestConfiguration,
    DisplayEvent,
    DisplayRequest,
    VAceEditor
  },
  methods: {
    loadData() {
      this.loading = true;
      let self = this;

      let url = `/api/users/me/contracts/${this.contractAddress}`;

      this.$http.get(url).then(result => {
        self.loading = false;
        self.contract = result.data;

        let abi = JSON.parse(self.contract.abi);

        for ( let i=0; i<abi.length; i++ ){
          if ( abi[i].type === "function" ){
            let f = abi[i];
            if ( f.inputs) {
              for (let j = 0; j < f.inputs.length; j++) {
                f.inputs[j].value = '';
              }
            }

            self.functions.push({
              documentation: self.getMethod(abi[i]),
              description: abi[i]
            })
          }

          if ( abi[i].type === "event" ){
            self.events.push(abi[i])
          }
        }

        console.log(self.functions,self.events)
      }, error => {
        self.loading = false;
        console.error(error);
      });
    },
    displayRequest(method){
      this.$refs['displayRequestModal'].show(method, this.contract.blockchain, this.contractAddress, this.applicationId, this.apiKey, this.walletPassword);
    },
    displayEvent(event){
      this.$refs['displayEventModal'].show(event.name, this.contract.blockchain, this.contractAddress, this.applicationId, this.apiKey)
    },
    playFunction(method){
      let self = this;

      if ( this.apiKey === '' || this.walletPassword === '' ){
        return this.$parent.$parent.$parent.displayModal(this.$i18n.t('pages.console.contract.functions.warningTitle'), this.$i18n.t('pages.console.contract.functions.warning'));
      }

      self.result += this.$t('pages.console.contract.console.executing', {method: method.description.name});
      self.isPlaying = true;

      let url = `/api/blockchains/${this.contract.blockchain}/contracts/${this.contract.address}/functions/${method.description.name}/`;
      let data = {
        parameters: method.description.inputs
      };
      if ( method.description.stateMutability === 'view' || method.description.stateMutability === 'pure' ){
        url += 'read';
      } else {
        url += 'eval';
        data.walletPassword = this.walletPassword;
        data.tokenValue = method.tokenValue;
      }

      this.$http.post(url, data, {
        headers: {
          'application-id': this.applicationId,
          'api-key': this.apiKey
        }
      }).then(result => {
        self.result +=  '<br /><span class="executionResult">' + JSON.stringify(result.data) + '</span>';

        if ( method.description.stateMutability !== 'view' && method.description.stateMutability !== 'pure' ){
          self.result += this.$t('pages.console.contract.console.checking');
          self.statusInterval = setInterval(self.checkStatus.bind(self, result.data.hash), 3000);
        } else {
          self.isPlaying = false;
        }

        setTimeout(this.scroll, 300);
      }, error => {
        self.result += '<br /><span class="executionError">' + JSON.stringify(error.message) + '</span>';
        self.isPlaying = false;

        setTimeout(this.scroll, 300);
      });
    },
    scroll(){
      document.getElementById('console').scrollTop = '10000000';
    },
    checkStatus(hash){
      let self = this;

      this.$http.get(`/api/blockchains/${this.contract.blockchain}/transactions/${hash}?applicationId=${this.applicationId}&apiKey=${this.apiKey}`, {
        headers: {
          'application-id': this.applicationId,
          'api-key': this.apiKey
        }
      }).then(result => {
        self.result += '<br /><span class="executionResult">' + JSON.stringify(result.data, null, 2) + '</span>';

        clearInterval(self.statusInterval);
        setTimeout(this.scroll, 300);

        self.isPlaying = false;
        self.retries = 0;
      }, error => {
        self.retries++;

        // Stop trying after 5 attempts
        if ( self.retries === 5 ){
          clearInterval(self.statusInterval);

          self.isPlaying = false;
          self.retries = 0;
        }

        if ( error.response.status !== 500 ) {
          self.result += '<br /><span class="executionError">' + JSON.stringify(error.message, null, 2) + '</span>';
          setTimeout(this.scroll, 300);
        } else {
          self.retries++;
          self.result += '.'
        }
      });
    },
    getMethod(method){
      // Generate cartridge
      let template = '/**\r\n';

      template += ` * ${method.name}`;

      if ( method.stateMutability === 'view' || method.stateMutability === 'pure' ){
        template += ` (read-only)`;
      }

      template += `\r\n`;

      if ( method.inputs.length > 0 ){
        template += ' *\r\n';
        for ( let i=0; i< method.inputs.length; i++ ){
          template += ` * @param ${method.inputs[i].name} ${method.inputs[i].type}\r\n`;
        }
      }

      if ( method.outputs.length > 0 ){
        template += ' *\r\n';
        for ( let i=0; i< method.outputs.length; i++ ){
          template += ` * @return ${method.outputs[i].name} ${method.outputs[i].type}\r\n`;
        }
      }

      template += ' **/\r\n';

      // Add method
      template += `function ${method.name}(${method.inputs.map(function(elem){return elem.name;}).join(', ')}){}`;

      return template;
    },
    editorInit(editor) {
      editor.setReadOnly(true);
      editor.renderer.setShowGutter(false);

      // Automatically set the height of the editor
      editor.setOptions({
        maxLines: Infinity
      });
    },
    setTab(tab) {
      this.activeTab = tab;
      this.$router.push({ name: "Contract", query: {tab: this.activeTab} });
    },
  },
  beforeMount(){
    this.contractAddress = this.$route.params.address;

    if (this.$route.query && this.$route.query.tab){
      this.activeTab = this.$route.query.tab;
    }

    this.loadData();
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.tab) {
      this.activeTab = to.query.tab;
    } else {
      this.activeTab = 'functions';
    }
    next();
  },
  data() {
    return {
      loading: false,
      activeTab: 'functions',
      contractAddress: null,
      contract: {
        name: null,
        address: null,
        contractId: null,
        abi: ""
      },
      functions: [],
      events: [],
      applications: [],
      applicationId: '',
      apiKey: '',
      walletPassword: '',
      isPlaying: false,
      retries: 0,
      result: this.$i18n.t('pages.console.contract.console.default'),
      statusInterval: null,
      eventsFields: [
        { key: 'name', label: this.$i18n.t('pages.console.contract.events.table.name'), tdClass: 'align-middle', sortable: true },
        { key: 'inputs', label: this.$i18n.t('pages.console.contract.events.table.parameters'), tdClass: 'align-middle' },
        { key: 'actions', label: this.$i18n.t('pages.console.contract.events.table.actions'), tdClass: 'align-middle actions', class: 'text-end' }
      ]
    }
  },
  computed: {}
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}
</style>
