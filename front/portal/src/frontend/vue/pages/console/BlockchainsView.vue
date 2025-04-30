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
    <nav role="navigation" aria-label="breadcrumb" class="d-none d-md-block">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          {{ $t('pages.console.breadcrumb') }}
        </li>
        <li class="breadcrumb-item active" aria-current="location">
          {{ $t('pages.console.blockchains.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1 class="mb-0">
          <span class="me-2">{{ $t('pages.console.blockchains.title') }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>
        <small class="text-muted">
          {{ $t('pages.console.blockchains.description') }}
        </small>
      </div>
    </div>
    <div class="row mt-4">
      <div class="col-4" v-for="(blockchain, index) in blockchains" :key="index">
        <div class="card mb-3" v-bind:class="{
            'corner corner-primary': blockchain.name === 'local',
            'corner corner-info': blockchain.name === 'alastria',
            'corner corner-light': blockchain.name === 'abfTestnet',
            'corner corner-secondary': blockchain.name !== 'alastria' && blockchain.name !== 'abfTestnet' && blockchain.name !== 'local'
          }">
          <div class="card-body">
            <h2 class="card-subtitle mt-1 mb-3 text-capitalize">{{ $t(`blockchains.${blockchain.name}`) }}</h2>
            <p class="card-text">
              <small class="d-block" v-html="$t('pages.console.blockchains.type', {type: $t(`blockchainsTypes.${blockchain.type}`)})"></small>
            </p>
            <p class="card-text">
              <small class="d-block" v-if="blockchain['chainId'] !== undefined" v-html="$t('pages.console.blockchains.chainInfo.chainId', {id: blockchain['chainId']})"></small>
              <small v-if="blockchain['blockNumber'] !== undefined" v-html="$t('pages.console.blockchains.chainInfo.blockNumber', {blockNumber: blockchain['blockNumber']})"></small><router-link :to="`/console/blocks?blockNumber=${blockchain['blockNumber']}`" class="ms-1"><i class="fa fa-magnifying-glass" style="font-size: 11px"></i></router-link>
              <small class="d-block" v-if="blockchain['gasPrice'] !== undefined" v-html="$t('pages.console.blockchains.chainInfo.gasPrice', {gasPrice: blockchain['gasPrice']})"></small>
              <small class="d-block" v-if="blockchain['protocol'] !== undefined" v-html="$t('pages.console.blockchains.chainInfo.protocol', {protocol: blockchain['protocol']})"></small>
            </p>
            <h6 class="mt-4 mb-2" style="font-size: 14px">{{ $t('pages.console.blockchains.web3Client.title') }}</h6>
            <p class="card-text">
              <!-- <small class="d-block" v-if="blockchain['web3Client']['chainId'] !== undefined" v-html="$t('pages.console.blockchains.web3Client.chainId', {id: blockchain['web3Client']['chainId']})"></small> -->
              <small class="d-block" v-if="blockchain['web3Client']['url'] !== undefined" v-html="$t('pages.console.blockchains.web3Client.url', {url: blockchain['web3Client']['url']})"></small>
              <small class="d-block" v-if="blockchain['web3Client']['version'] !== undefined" v-html="$t('pages.console.blockchains.web3Client.version', {version: blockchain['web3Client']['version']})"></small>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  components: {},
  methods: {
    loadData() {
      let self = this;
      this.loading = true;

      this.$http.get(`/api/blockchains`).then(result => {
        let promises = [];

        for ( let i=0; i<result.data.length; i++ ){
          promises.push(this.$http.get(`/api/blockchains/${result.data[i]}`).then(result => {
            self.blockchains.push(result.data);
          }, error => {
            console.error(error);
          }));
        }

        Promise.all(promises).then(function() {
          console.log(self.blockchains)
          self.loading = false;
        });
      }, error => {
        console.error(error);
      });
    }
  },
  beforeMount(){
    this.loadData();
  },
  data() {
    return {
      loading: false,
      blockchains: [],
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
