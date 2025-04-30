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
      :title="$t('modals.console.displayBalance.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error !== ''">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p v-html="error"></p>
      </div>

      <div class="row">
        <div class="col-12">
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
          <p class="mt-3" v-html="$t('modals.console.displayBalance.balance', {balance: balance})"></p>
        </div>
      </div>

      <template v-slot:footer>
        <b-button
          variant="primary"
          @click="close"
        >
          {{ $t('modals.console.displayBalance.close') }}
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
    show(blockchain, walletAddress){
      let self = this;
      let url = `/api/users/me/wallets/${walletAddress}/balance?blockchain=${encodeURIComponent(blockchain)}`;

      this.loading = true;

      this.$http.get(url).then(function(result){
        console.log(result.data.amount);
        self.loading = false;
        self.balance = result.data.amount;
        self.$refs['modal'].show();
      }).catch(function(error) {
        self.loading = false;
        self.error += `${error.response.statusText} (Error status ${error.response.status})<br />`;
        console.error(error);
      });
    },
    reset(){
      this.error = '';
      this.balance = '';
    },
    close(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    }
  },
  data() {
    return {
      loading: false,
      error: '',
      balance: ''
    }
  }
}
</script>
