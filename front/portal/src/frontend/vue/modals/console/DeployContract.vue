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
      :title="$t('modals.console.deployContract.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>
      <b-form-group
          :label="$t('modals.console.deployContract.form.blockchain')"
          label-for="contract-blockchain"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-select
            id="contract-blockchain"
            :options="blockchains"
            v-model="blockchain"
            required
        ></b-form-select>
      </b-form-group>

      <b-form-group
          :state="walletState"
          :label="$t('modals.console.deployContract.form.wallet')"
          label-for="contract-wallet"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-select
            id="contract-wallet"
            :options="wallets"
            :state="walletState"
            v-model="walletAddress"
            required
        ></b-form-select>

        <b-form-invalid-feedback>
          {{ $t('modals.console.deployContract.form.invalidWallet') }}
        </b-form-invalid-feedback>
      </b-form-group>
      <b-form-group
          :state="passwordState"
          :label="$t('modals.console.deployContract.form.walletPassword')"
          label-for="contract-wallet-password"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-input-group>
          <b-form-input
              id="contract-wallet-password"
              :type="inputType"
              v-model="walletPassword"
              :state="passwordState"
              required
          ></b-form-input>

          <b-input-group-append>
            <b-input-group-text v-on:click="toggleInputType">
              <i v-if="inputType === 'password'" class="fa fa-eye-slash" aria-hidden="true"></i>
              <i v-if="inputType === 'text'" class="fa fa-eye" aria-hidden="true"></i>
            </b-input-group-text>
          </b-input-group-append>
        </b-input-group>

        <b-form-invalid-feedback>
          {{ $t('modals.console.deployContract.form.invalidPassword') }}
        </b-form-invalid-feedback>
      </b-form-group>
      <b-form-group
          :label="$t('modals.console.deployContract.form.walletOptions')"
          label-for="contract-wallet-settings"
          label-class="is-required"
          class="mt-3 mb-1"
       >
      <v-ace-editor
          v-model:value="settings"
          lang="json"
          theme="chrome"
          @init="editorInit"
          style="height: 300px" />
      </b-form-group>
      <div class="mt-3 mb-1" role="group">
        <label for="zipData" class="form-label d-block is-required">{{ $t('modals.console.deployContract.form.file') }}</label>
        <div>
          <input
            type="file"
            class="form-control"
            id="zipData"
            required
            accept="zip,application/octet-stream,application/zip,application/x-zip,application/x-zip-compressed" />
        </div>
      </div>

      <template v-slot:footer>
        <b-button
          variant="secondary"
          @click="handleCancel"
        >
          {{ $t('modals.console.deployContract.form.cancel') }}
        </b-button>

        <b-button
          variant="primary"
          @click="handleOk"
        >
          {{ $t('modals.console.deployContract.form.submit') }}
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
          <span class="visually-hidden">Loading...</span>
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next';
import { VAceEditor } from 'vue3-ace-editor';
import {capitalizeFirstLetter} from "@/frontend/vue/utils/strings";

export default {
  components: {
    BModal,
    VAceEditor
  },
  methods: {
    show(){
      let self = this;

      this.settings = JSON.stringify(this.body, null, 4);
      this.$refs['modal'].show();

      let walletsUrl = `/api/users/me/wallets`;
      this.$http.get(walletsUrl).then(function(result){
        for ( let i=0; i<result.data.length; i++ ){
          self.wallets.push({
            value: result.data[i].address, text: result.data[i].name
          });

          if ( i === 0 ){
            self.walletAddress = result.data[i].address;
          }
        }
      });

      let blockchainsUrl = `/api/blockchains`;
      this.$http.get(blockchainsUrl).then(function(result){
        for ( let i=0; i<result.data.length; i++ ){
          if ( result.data[i] === 'abfTestnet') {
            self.blockchains.push({
              value: result.data[i], text: 'ABF Testnet'
            });
          } else {
            self.blockchains.push({
              value: result.data[i], text: self.capitalizeFirstLetter(result.data[i])
            });
          }

          if ( i === 0 ){
            self.blockchain = result.data[i];
          }
        }
      });
    },
    editorInit(editor){
      editor.setOption('wrap', true);
      editor.renderer.setShowGutter(false);
    },
    reset(){
      this.error = false;
      this.wallets = [];
      this.contractFile = null;
      this.walletAddress = '';
      this.blockchain = '';
      this.blockchains = [];
      this.walletPassword = '';
      this.walletState = null;
      this.passwordState = null;
    },
    submit(){
      let self = this;

      this.error = null;
      this.loading = true;

      let url = `/api/users/me/deployments`;

      let settings = JSON.parse(this.settings);

      settings.wallet = {
        address: this.walletAddress,
        password: this.walletPassword
      };

      settings.options.network = this.blockchain;

      const form = new FormData();
      form.append('myBody', JSON.stringify(settings));
      form.append('projectFile', this.contractFile);

      this.$http.post(url, form).then(function(){
        self.loading = false;
        self.$nextTick(() => {
          self.$refs['modal'].hide();
          self.$parent.loadData();
        });
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.message}`;
        console.error(error);
      });
    },
    setData(){
      this.contractFile = document.getElementById('zipData').files[0];
    },
    validateData(){
      this.walletState = this.walletAddress !== '';
      this.passwordState = this.walletPassword !== '';
      return (this.walletState && this.passwordState);
    },
    toggleInputType(e){
      e.preventDefault();
      (this.inputType === 'password')?this.inputType='text':this.inputType='password';
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();
      this.setData();
      if ( this.validateData() === true ){
        this.submit();
      }
    },
    handleClose(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
      this.$parent.loadData();
    },
    handleCancel(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    },
  },
  data() {
    return {
      error: false,
      loading: false,
      contractFile: null,
      wallets: [],
      blockchains: [],
      walletState: null,
      passwordState: null,
      inputType: 'password',
      walletAddress: '',
      blockchain: '',
      walletPassword: '',
      option: "",
      body: {
        options: {
          version: "0.8.14",
          evmVersion: "byzantium",
          optimizerRuns: 200
        },
        migration: [
          {
            name: "BigInt",
            links: []
          },
          {
            name: "RSARingSignature",
            links: [ "BigInt" ],
            parameters: []
          },
          {
            name: "Forge",
            parameters: [
              {
                name: "a",
                valueType: "uint256",
                value: "0"
              }
            ],
            links: [ "BigInt", "RSARingSignature" ]
          }
        ]
      },
      capitalizeFirstLetter
    }
  }
}
</script>
<style scoped>
  .input-group-text {
    background-color: #ccc;
    cursor: pointer;
    width: 40px;
  }
</style>
