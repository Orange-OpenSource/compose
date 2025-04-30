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
        <li class="breadcrumb-item active" aria-current="location">
          {{ $t('pages.console.transactions.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1 class="mb-0">
          <span class="me-2">{{ $t('pages.console.transactions.title') }}</span>
        </h1>
      </div>
    </div>
    <div class="row mt-3">
      <div class="col-12">
        <test-configuration v-model:api-key="apiKey" v-model:application-id="applicationId" v-model:blockchain="blockchain"></test-configuration>
      </div>
    </div>
    <div class="row">
      <div class="col-12">
        <div class="input-group mb-3">
          <label class="sr-only" for="inline-form-hash">{{ $t(`pages.console.transactions.hash.title`) }}</label>
          <b-input-group>
            <b-form-input
              id="inline-form-hash"
              class="mb-2 mr-sm-2 mb-sm-0"
              :placeholder="$t(`pages.console.transactions.hash.placeholder`)"
              v-model="hash"
            ></b-form-input>
            <b-input-group-append>
              <b-button @click="getTransaction" :disabled="hash == '' || loading">
                {{ $t('pages.console.transactions.search') }}
                <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
              </b-button>
            </b-input-group-append>
          </b-input-group>
        </div>

        <div id="console" class="executionConsole mt-3" v-html="result">
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import TestConfiguration from "@/frontend/vue/components/TestConfiguration.vue";

export default {
  components: {
    TestConfiguration
  },
  methods: {
    getTransaction(){
      let self = this;

      if ( this.apiKey === '' ){
        return this.$parent.$parent.$parent.displayModal(this.$i18n.t('pages.console.transactions.warningTitle'), this.$i18n.t('pages.console.transactions.warning'));
      }

      this.loading = true;

      self.result += this.$t('pages.console.transactions.console.executing', {hash: this.hash});

      let url = `/api/blockchains/${this.blockchain}/transactions/${this.hash}`;
      this.$http.get(url, {
        headers: {
          'application-id': this.applicationId,
          'api-key': this.apiKey
        }
      }).then(result => {
        self.loading = false;
        self.result +=  '<br /><span class="executionResult">' + JSON.stringify(result.data, null, 2) + '</span>';
        setTimeout(this.scroll, 300);
      }, error => {
        self.loading = false;
        console.error(error);
        self.result += '<br /><span class="executionError">' + JSON.stringify(error.message, null, 2) + '</span>';
        setTimeout(this.scroll, 300);
      });
    },
    scroll(){
      document.getElementById('console').scrollTop = '10000000';
    }
  },
  beforeMount(){
    if ( this.$route.query.hash ){
      this.hash = this.$route.query.hash;
    }
  },
  data() {
    return {
      loading: false,
      hash: '',
      blockchain: '',
      applications: [],
      applicationId: '',
      apiKey: '',
      result: this.$i18n.t('pages.console.transactions.console.default'),
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
