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
      :title="$t('modals.console.importWallet.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>

      <b-form-group
          :state="nameState"
          :label="$t('modals.console.importWallet.form.name')"
          label-for="wallet-name"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-input
            id="wallet-name"
            type="text"
            v-model="name"
            :state="nameState"
            required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.importWallet.form.invalidName') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group
        :state="passwordState"
        :label="$t('modals.console.importWallet.form.password')"
        label-for="wallet-password"
        label-class="is-required"
        class="mt-3 mb-1"
      >
        <b-form-input
          id="wallet-password"
          type="password"
          v-model="password"
          :state="passwordState"
          required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.importWallet.form.invalidPassword') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group
          :label="$t('modals.console.importWallet.form.blockchain')"
          label-for="wallet-blockchain"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-select
            id="wallet-blockchain"
            :options="blockchains"
            v-model="blockchain"
            required
        ></b-form-select>
      </b-form-group>

      <hr class="mt-5 mb-4" />

      <b-form-group
        :state="fileState"
        :label="$t('modals.console.importWallet.form.file')"
        label-for="wallet-file"
        label-class="is-required"
        class="mt-3 mb-1"
      >
        <b-form-input
          id="wallet-file"
          type="file"
          :state="fileState"
          accept="application/JSON"
          required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.importWallet.form.invalidFile') }}
        </b-form-invalid-feedback>
      </b-form-group>


      <b-form-group
        :label="$t('modals.console.importWallet.form.filePassword')"
        label-for="wallet-file-password"
        class="mt-3 mb-1"
      >
        <b-form-input
          id="wallet-file-password"
          type="password"
          v-model="filePassword"
        ></b-form-input>
      </b-form-group>

      <template v-slot:footer>
        <b-button
          variant="secondary"
          @click="handleCancel"
        >
          {{ $t('modals.console.importWallet.form.cancel') }}
        </b-button>

        <b-button
          variant="primary"
          @click="handleOk"
        >
          {{ $t('modals.console.importWallet.form.submit') }}
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
          <span class="visually-hidden">Loading...</span>
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
      let url = `/api/blockchains`;
      this.$http.get(url).then(function(result){
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
    reset(){
      this.error = false;
      this.step = 0;

      this.name = '';
      this.password = '';
      this.blockchain = '';
      this.file = '';
      this.filePassword = '';
      this.blockchains = [];
    },
    submit(){
      let self = this;

      this.error = null;
      this.loading = true;

      let url = `/api/users/me/wallets/import`;

      let options = {
        walletPassword: this.filePassword,
        configWallet: {
          name: this.name,
          password: this.password,
          blockchain: this.blockchain
        }
      };

      const form = new FormData();
      form.append('myBody', JSON.stringify(options));
      form.append('walletFile', this.file);

      this.$http.post(url, form).then(function(){
        self.loading = false;
        self.$parent.loadData();
        self.$refs['modal'].hide();
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.message}`;
        console.error(error);
      });
    },
    validateData(){
      this.passwordState =  (this.password !== '');
      this.nameState =  (this.name !== '');
      this.fileState =  (this.file !== null);
      return (this.passwordState && this.nameState && this.fileState);
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();

      this.file = document.getElementById('wallet-file').files[0];

      if ( this.validateData() === true ){
        this.submit();
      }
    },
    handleClose(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
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
      nameState: null,
      fileState: null,
      passwordState: null,
      blockchain: '',
      name: '',
      password: '',
      file: '',
      filePassword: '',
      blockchains: [],
      capitalizeFirstLetter
    }
  }
}
</script>
<style scoped>
  hr {
    border-top: 1px solid #ccc;
  }
</style>
