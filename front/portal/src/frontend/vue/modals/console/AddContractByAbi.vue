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
        :title="$t('modals.console.addContractByAbi.title')"
        @hidden="reset"
        no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>

      <b-form-group
          :state="nameState"
          :label="$t('modals.console.addContractByAbi.form.name')"
          label-for="contract-name"
          label-class="is-required"
          class="mt-3"
      >
        <b-form-input
            id="contract-name"
            v-model="name"
            :state="nameState"
            required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.addContractByAbi.form.invalidName') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group
          :label="$t('modals.console.addContractByAbi.form.blockchain')"
          label-for="contract-blockchain"
          label-class="is-required"
          class="mt-3"
      >
        <b-form-select
            id="contract-blockchain"
            v-model="blockchain"
            :options="blockchains"
            class="form-select"
        >
        </b-form-select>
      </b-form-group>

      <b-form-group
          :state="addressState"
          :label="$t('modals.console.addContractByAbi.form.address')"
          label-for="contract-address"
          label-class="is-required"
          class="mt-3"
      >
        <b-form-input
            id="contract-address"
            v-model="address"
            :placeholder="$t('modals.console.addContractByAbi.form.addressPlaceholder')"
            :state="addressState"
            required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.addContractByAbi.form.invalidAddress') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group
          :label="$t('modals.console.addContractByAbi.form.abi')"
          label-for="contract-abi"
          label-class="is-required"
          class="mt-3"
      >
        <v-ace-editor
          v-model:value="abi"
          @init="consoleInit"
          lang="json"
          theme="chrome"
          style="height: 300px">
        </v-ace-editor>
      </b-form-group>

      <template v-slot:footer>
        <b-button
            variant="secondary"
            @click="handleCancel"
        >
          {{ $t('modals.console.addContractByAbi.form.cancel') }}
        </b-button>

        <b-button
            variant="primary"
            @click="handleOk"
        >
          {{ $t('modals.console.addContractByAbi.form.submit') }}
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
import { capitalizeFirstLetter } from '../../utils/strings';

export default {
  components: {
    BModal,
    VAceEditor
  },
  methods: {
    show(blockchain, address, abi){
      let self = this;
      this.$refs['modal'].show();

      if ( address ){
        this.address = address;
      }

      if ( abi ){
        this.abi = abi;
      }

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

          if ( blockchain ){
            self.blockchain = blockchain;
          }
        }
      });
    },
    reset(){
      this.error = null;
      this.nameState = null;
      this.addressState = null;
      this.address = '';
      this.name = '';
      this.abi = '';
      this.blockchain = '';
      this.blockchains = [];
    },
    consoleInit(editor){
      editor.setOption('wrap', true);
    },
    submit(){
      let self = this;
      this.loading = true;

      this.$http.post(`/api/users/me/contracts`, {
        name: this.name,
        blockchain: this.blockchain,
        address: this.address,
        abi: this.abi
      }).then(function(){
        self.loading = false;
        self.$nextTick(() => {
          self.$refs['modal'].hide();
          self.$parent.loadData();
        });
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.statusText} (Error status ${error.status})`;
        console.error(error);
      });
    },
    validateData(){
      this.nameState = this.name !== '';
      this.addressState = (this.address !== '' && this.address.startsWith('0x'));
      return (this.nameState && this.addressState);
    },
    handleCancel(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();

      if ( this.validateData() === true ){
        this.submit();
      }
    },
  },
  data() {
    return {
      error: null,
      loading: false,
      nameState: null,
      addressState: null,
      address: '',
      name: '',
      abi: '',
      blockchain: '',
      blockchains: [],
      capitalizeFirstLetter
    }
  }
}
</script>
