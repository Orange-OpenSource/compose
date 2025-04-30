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
      :title="$t('modals.console.addApplication.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>
      <b-form-group
          :state="nameState"
          :label="$t('modals.console.addApplication.form.name')"
          label-for="application-name"
          label-class="is-required"
          class="mt-3 mb-1"
          :disabled="step === 1"
      >
        <b-form-input
            id="application-name"
            type="text"
            v-model="name"
            :state="nameState"
            required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.addApplication.form.invalidName') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group
          :label="$t('modals.console.addApplication.form.blockchain')"
          label-for="application-blockchain"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-select
            id="application-blockchain"
            :options="blockchains"
            v-model="blockchain"
            v-on:change="selectBlockchain"
            required
        ></b-form-select>
      </b-form-group>

      <b-form-group
          :label="$t('modals.console.addApplication.form.wallet')"
          label-for="application-wallet"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-select
            id="application-wallet"
            :options="selectedWallets"
            v-model="walletAddress"
            v-if="selectedWallets.length > 0"
            required
        ></b-form-select>

        <p v-if="selectedWallets.length === 0">
          {{ $t('modals.console.addApplication.form.invalidWallet') }}
        </p>
      </b-form-group>

      <b-form-group
        :label="$t('modals.console.addApplication.form.apiKey')"
        label-for="application-key"
        class="mt-3 mb-1"
        v-if="step === 1"
      >
        <b-input-group>
          <b-form-input
            id="application-key"
            type="text"
            v-model="apiKey"
            disabled="true"
          ></b-form-input>

          <b-input-group-append v-if="textCopied === false">
            <b-button variant="secondary" v-on:click="copyKey" :title="$t('modals.console.addApplication.form.copy')" :aria-label="$t('modals.console.addApplication.form.copy')"><i class="fa fa-clipboard" aria-hidden="true"></i></b-button>
          </b-input-group-append>

          <b-input-group-append v-if="textCopied === true">
            <b-button variant="secondary" disabled="true" :title="$t('modals.console.addApplication.form.copyOk')" :aria-label="$t('modals.console.addApplication.form.copyOk')"><i class="fa fa-check" style="color: green" aria-hidden="true"></i></b-button>
          </b-input-group-append>
        </b-input-group>

        <b-form-text id="applicationKeyHelp" class="d-block mt-1" v-html="$t('modals.console.addApplication.form.apiKeyDetails')"></b-form-text>
      </b-form-group>

      <template v-slot:footer>
        <b-button
          variant="secondary"
          @click="handleCancel"
          v-if="step === 0"
        >
          {{ $t('modals.console.addApplication.form.cancel') }}
        </b-button>

        <b-button
          variant="primary"
          @click="handleOk"
          :disabled="selectedWallets.length === 0"
          v-if="step === 0"
        >
          {{ $t('modals.console.addApplication.form.submit') }}
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
          <span class="visually-hidden">Loading...</span>
        </b-button>

        <b-button
          variant="primary"
          @click="handleClose"
          v-if="step === 1"
        >
          {{ $t('modals.console.addApplication.form.finish') }}
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next';
import { capitalizeFirstLetter } from '../../utils/strings';

export default {
  components: {
    BModal
  },
  methods: {
    show(){
      let self = this;

      this.$refs['modal'].show();

      let walletsUrl = `/api/users/me/wallets`;
      let walletsPromse = this.$http.get(walletsUrl).then(function(result){
        for ( let i=0; i<result.data.length; i++ ){
          self.wallets.push({
            value: result.data[i].address, text: result.data[i].name, blockchain: result.data[i].blockchain
          });
        }
      });

      let blockchainsUrl = `/api/blockchains`;
      let blockchainsPromse = this.$http.get(blockchainsUrl).then(function(result){
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

      Promise.all([walletsPromse, blockchainsPromse]).then(function(){
        self.selectBlockchain();
      });
    },
    reset(){
      this.error = false;
      this.step = 0;

      this.nameState = null;
      this.name = '';
      this.blockchains = [];
      this.blockchain = null;
      this.selectedWallets = [];
      this.wallets = [];
      this.walletAddress = null;
      this.apiKey = '';
      this.textCopied = false;
    },
    selectBlockchain(){
      this.selectedWallets = [];
      for ( let i=0; i<this.wallets.length; i++ ){
        if (this.wallets[i].blockchain === this.blockchain ){
          this.selectedWallets.push(this.wallets[i]);
        }
      }
      if ( this.selectedWallets.length > 0 ){
        this.walletAddress = this.selectedWallets[0].value;
      }
    },
    submit(){
      let self = this;

      this.error = null;
      this.loading = true;

      let url = `/api/users/me/applications`;

      this.$http.post(url, {
        name: this.name,
        walletAddress: this.walletAddress,
        blockchain: this.blockchain
      }).then(function(result){
        self.loading = false;
        self.apiKey = result.data['api_key'];
        self.step = 1;
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.message}`;
        console.error(error);
      });
    },
    async copyKey(){
      try {
        await navigator.clipboard.writeText(this.apiKey);
        this.textCopied = true;
      } catch($e) {
        console.log('Cannot copy');
      }
    },
    validateData(){
      this.nameState = (this.name !== '');
      return this.nameState;
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();

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
      step: 0,
      textCopied: false,
      blockchains: [],
      blockchain: null,
      wallets: [],
      selectedWallets: [],
      walletAddress: null,
      nameState: null,
      name: '',
      apiKey: '',
      capitalizeFirstLetter
    }
  }
}
</script>
