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
          {{ $t('pages.console.blocks.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1 class="mb-0">
          <span class="me-2">{{ $t('pages.console.blocks.title') }}</span>
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
          <label class="sr-only" for="inline-form-blockNumberOrHash">{{ $t(`pages.console.blocks.blockNumberOrHash.title`) }}</label>
          <b-input-group>
            <b-form-input
              id="inline-form-blockNumberOrHash"
              class="mb-2 mr-sm-2 mb-sm-0"
              :placeholder="$t(`pages.console.blocks.blockNumberOrHash.placeholder`)"
              v-model="blockNumberOrHash"
            ></b-form-input>
            <b-input-group-append>
              <b-button @click="getBlock" :disabled="blockNumberOrHash == '' || loading">
                {{ $t('pages.console.blocks.search') }}
                <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
              </b-button>
            </b-input-group-append>
          </b-input-group>
        </div>

        <div class="row mt-5">
          <div class="col-4" v-for="(block, index) in blocks" :key="index">
            <div class="card block mb-3">
              <div class="card-header">
                {{ $t('pages.console.blocks.card.header', {number: Number(block['number'])}) }}
              </div>
              <div class="card-body">
                <p class="card-text">
                  <small class="d-block" v-for="(value, key) in block" :key="key"><strong>{{ key }}</strong>: {{ value }}</small>
                </p>
              </div>
            </div>
          </div>
          <div v-if="blocks.length > 0 && hasParentBlock() === true" class="col-4 text-center">
            <button class="btn btn-primary mt-5" v-on:click="loadPreviousBlock(blocks[blocks.length-1].parentHash)">{{ $t('pages.console.blocks.loadPreviousBlock') }}</button>
          </div>
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
    getBlock(){
      let self = this;

      if ( this.apiKey === '' ){
        return this.$parent.$parent.$parent.displayModal(this.$i18n.t('pages.console.blocks.warningTitle'), this.$i18n.t('pages.console.blocks.warning'));
      }

      this.loading = true;

      let url;
      if ( this.blockNumberOrHash.startsWith('0x')){
        url = `/api/blockchains/${this.blockchain}/blocks/find/${this.blockNumberOrHash}`;
      } else {
        url = `/api/blockchains/${this.blockchain}/blocks/${this.blockNumberOrHash}`;
      }

      this.$http.get(url, {
        headers: {
          'application-id': this.applicationId,
          'api-key': this.apiKey
        }
      }).then(result => {
        self.loading = false;

        // Reset queue if block has nothing to do with previously loaded blocks
        if ( self.blocks.length > 0 ){
          if ( Number(result.data['number']) !== (Number(self.blocks[self.blocks.length-1]['number']) - 1) ){
            self.blocks = [];
          }
        }

        self.blocks.push(result.data);
      }, error => {
        self.loading = false;
        console.error(error);
        return this.$parent.$parent.$parent.displayModal(this.$i18n.t('pages.console.blocks.errorTitle'), error);
      });
    },
    loadPreviousBlock(hash){
      this.blockNumberOrHash = hash;
      this.getBlock();
    },
    hasParentBlock(){
      if ( this.blocks.length > 0 ){
        let block = this.blocks[this.blocks.length-1];
        if ( Number(block['parentHash']) !== 0 ){
          return true;
        }
      }
      return false;
    }
  },
  beforeMount(){
    if ( this.$route.query.blockNumber ){
      this.blockNumberOrHash = this.$route.query.blockNumber;
    }

    if ( this.$route.query.hash ){
      this.blockNumberOrHash = this.$route.query.hash;
    }
  },
  data() {
    return {
      loading: false,
      blockNumberOrHash: '',
      blockchain: '',
      applications: [],
      applicationId: '',
      apiKey: '',
      blocks: [],
      result: this.$i18n.t('pages.console.blocks.console.default'),
    }
  },
  computed: {}
}
</script>
<style scoped>
.block .card-body {
  height: 350px;
  overflow: auto;
}
</style>
<script setup lang="ts">
</script>
