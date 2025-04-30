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
      :title="$t('modals.console.addEventListener.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>

      <b-form-group
          :state="urlState"
          :label="$t('modals.console.addEventListener.form.url')"
          label-for="listener-address"
          label-class="is-required"
          class="mt-3 mb-1"
      >
        <b-form-input
            id="listener-url"
            type="text"
            v-model="url"
            :state="urlState"
            required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.addEventListener.form.invalidUrl') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <template v-slot:footer>
        <b-button
          variant="secondary"
          @click="handleCancel"
        >
          {{ $t('modals.console.addEventListener.form.cancel') }}
        </b-button>

        <b-button
          variant="primary"
          @click="handleOk"
        >
          {{ $t('modals.console.addEventListener.form.submit') }}
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
          <span class="visually-hidden">Loading...</span>
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next';

export default {
  components: {
    BModal
  },
  methods: {
    show(blockchain, contractAddress, eventName){
      this.blockchain = blockchain;
      this.contractAddress = contractAddress;
      this.eventName = eventName;
      this.$refs['modal'].show();
    },
    reset(){
      this.error = false;

      this.urlState = null;
      this.url = '';
    },
    submit(){
      let self = this;

      this.error = null;
      this.loading = true;

      let url = `/api/users/me/contracts/${this.contractAddress}/events/${this.eventName}/webhooks`;

      this.$http.post(url, {
        blockchain: this.blockchain,
        url: this.url
      }).then(function(){
        self.loading = false;
        self.$parent.loadData();
        self.$refs['modal'].hide();
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.message}`;
        console.error(error);
      });
    },
    validateUrl(url) {
      let address;

      try {
        address = new URL(url);
      } catch (e) {
        console.log(e)
        return false;
      }

      return address.protocol === "http:" || address.protocol === "https:";
    },
    validateData(){
      this.urlState =  (this.url !== '' && this.validateUrl(this.url));
      return this.urlState;
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();

      if ( this.validateData() === true ){
        this.submit();
      }
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
      blockchain: null,
      contractAddress: null,
      eventName: null,
      urlState: null,
      url: ''
    }
  }
}
</script>
