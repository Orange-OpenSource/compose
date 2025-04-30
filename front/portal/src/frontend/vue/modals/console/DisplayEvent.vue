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
        :title="$t('modals.console.displayEvent.title')"
        @hidden="reset"
        no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error !== ''">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p v-html="error"></p>
      </div>

      <div class="row">
        <div class="col-12 text-center">
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </div>
      </div>

      <template v-slot:footer>
        <b-button
            variant="primary"
            @click="close"
        >
          {{ $t('modals.console.displayEvent.close') }}
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
    show(eventName, blockchain, contractAddress, applicationId, apiKey){
      let self = this;

      this.$refs['modal'].show();
      this.loading = true;

      let eventUrl = `/api/blockchains/${blockchain}/contracts/${contractAddress}/events/${eventName}`;
      let eventPromise = this.$http.get(eventUrl, {
        headers: {
          'application-id': applicationId,
          'api-key': apiKey
        }
      }).then(function(result){
        console.log(result);
      }).catch(function(error) {
        self.loading = false;
        self.error += `${error.response.statusText} (Error status ${error.response.status})<br />`;
        console.error(error);
      });

      let eventListenersUrl = `/api/users/me/contracts/${contractAddress}/events/${eventName}/webhooks`;
      let eventListenersPromise = this.$http.get(eventListenersUrl).then(function(result){
        console.log(result);
      }).catch(function(error) {
        self.loading = false;
        self.error += `${error.response.statusText} (Error status ${error.response.status})<br />`;
        console.error(error);
      });

      Promise.all([eventPromise, eventListenersPromise]).then(function() {
        self.loading = false;
      });
    },
    reset(){
      this.loading = false;
      this.error = '';
      this.event = null;
    },
    close(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    }
  },
  data() {
    return {
      loading: false,
      event: null,
      error: ''
    }
  }
}
</script>
