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
          {{ $t('pages.console.contracts.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1>
          <span class="me-2">{{ $t('pages.console.contracts.title', {count: contractsCount}) }}</span>
          <a class="h3" href="javascript:void(0)" @click.prevent="displayContractsSearch === true ? displayContractsSearch = false:displayContractsSearch = true" :aria-label=" $t('pages.console.contracts.search.title')">
            <i class="fas fa-search"></i>
          </a>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>

        <div class="row border border-light p-4 mb-5" v-if="displayContractsSearch === true">
          <div class="col-lg-12">
            <b-form-group
              :label="$t('pages.console.contracts.search.title')"
              label-for="search-input"
              label-cols-sm="3"
              label-align-sm="right"
            >
              <b-input-group>
                <b-form-input
                  id="search-input"
                  autocomplete="off"
                  v-model="search"
                  type="search"
                  :placeholder="$t('pages.console.contracts.search.placeholder')"
                ></b-form-input>

                <b-input-group-append>
                  <b-button :disabled="!search" @click="search = ''">{{ $t('pages.console.contracts.search.clear') }}</b-button>
                </b-input-group-append>
              </b-input-group>
            </b-form-group>
          </div>
        </div>

        <div style="overflow: auto">
          <b-table
              id="contracts-table"
              ref="table"
              :items="contracts"
              :fields="fields"
              sort-by="name"
              :per-page="contractsPerPage"
              :current-page="currentContractsPage"
              :filter="search"
              :filter-included-fields="filterOn"
              @filtered="onFiltered"
              hover
          >
            <template v-slot:cell(name)="data">
              <router-link :to="{path: `/console/contracts/${data.item['address']}`}" :title="$t('pages.console.contracts.displayContract')">
                {{ data.value }}
              </router-link>
            </template>

            <template v-slot:cell(blockchain)="data">
              <span
                class="badge rounded-pill"
                v-bind:class="{
                  'text-bg-primary': data.value === 'local',
                  'text-white bg-info': data.value === 'alastria',
                  'text-bg-light': data.value === 'abfTestnet',
                  'text-bg-secondary': data.value !== 'alastria' && data.value !== 'abfTestnet' && data.value !== 'local'
                }">{{ $t(`blockchains.${data.value}`) }}</span>
            </template>

            <template v-slot:cell(actions)="data">
              <router-link :to="{path: `/console/contracts/${data.item['address']}`}" class="me-2" :title="$t('pages.console.contracts.displayContract')">
                <i class="fas fa-eye"></i>
              </router-link>
              <a href="javascript:void(0)" class="me-2" v-on:click="exportContract(data.item)" :title="$t('pages.console.contracts.exportContract')">
                <i class="fa fa-download"></i>
              </a>
              <a href="javascript:void(0)" v-on:click="deleteContract(data.item.blockchain, data.item.address)" :title="$t('pages.console.contracts.deleteContract')">
                <i class="fas fa-trash"></i>
              </a>
            </template>
          </b-table>
        </div>

        <div class="float-start">
          <div class="dropdown float-end">
            <button class="btn btn-sm btn-primary pt-1 ms-2" v-on:click="addContract('abi')" >
              <i class="fas fa-plus me-2" aria-hidden="true"></i>
              {{ $t('pages.console.contracts.uploadContract') }}
            </button>
          </div>
        </div>

        <div
            v-if="rows > contractsPerPage"
            class="mt-3"
        >
          <b-pagination
              v-model="currentContractsPage"
              @page-click="setPage"
              :total-rows="rows"
              :per-page="contractsPerPage"
              aria-controls="contracts-table"
              align="end"
              first-class="pagination-first"
              prev-class="pagination-previous"
              next-class="pagination-next"
              last-class="pagination-last"
              first-text=""
              prev-text=""
              next-text=""
              last-text=""
              hide-ellipsis
          ></b-pagination>
        </div>
      </div>
    </div>
    <add-contract-by-abi ref="addContractByAbiModal"></add-contract-by-abi>
  </div>
</template>
<script>
import AddContractByAbi from "@/frontend/vue/modals/console/AddContractByAbi.vue";

export default {
  components: {
    AddContractByAbi
  },
  props: ['action'],
  methods: {
    loadData() {
      this.loading = true;
      let self = this;

      let url = `/api/users/me/contracts`;

      this.$http.get(url).then(result => {
        self.loading = false;
        self.contracts = result.data;
        self.contractsCount = self.contracts.length;
      }, error => {
        self.loading = false;
        console.error(error);
      });
    },
    exportContract(contract){
      const dataStr = JSON.stringify(JSON.parse(contract.abi));
      const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
      const exportFileDefaultName = `${contract.address}.json`;
      const linkElement = document.createElement('a');

      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', exportFileDefaultName);
      linkElement.click();
    },
    addContract(type, blockchain, address, abi){
      if ( type === 'abi' ){
        this.$refs['addContractByAbiModal'].show(blockchain, address, abi);
      }
    },
    deleteContract(blockchain, address){
      let self = this;

      this.$parent.$parent.$parent.displayDialog(self.$i18n.t('pages.console.contracts.deleteContractConfirmation'), {async: true}).then(function(dialog){
        self.$http.delete(`/api/users/me/blockchains/${blockchain}/contracts/${address}`).then(function(){
          dialog.displaySuccess(self.$i18n.t('pages.console.contracts.deleteContractSuccess'));
          self.loadData();
        }).catch(function(error){
          dialog.displayError(error);
        });
      });
    },
    onFiltered(filteredItems) {
      this.contractsCount = filteredItems.length;
      this.currentContractsPage = 1;
    },
    setPage(evt, page) {
      this.$router.push({ name: "Contracts", query: {page: page} });
    }
  },
  beforeMount(){
    if ( this.$route.query ){
      if ( this.$route.query.page ){
        this.currentContractsPage = parseInt(this.$route.query.page);
      }
    }

    this.loadData();
  },
  mounted() {
    if ( this.$route.query ){
      if ( this.$route.query.action && this.$route.query.action === 'addContractByABI' ){
        this.addContract('abi', this.$route.query.blockchain, this.$route.query.address, this.$route.query.abi);
      }
    }
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.page) {
      this.currentContractsPage = parseInt(to.query.page);
    } else {
      this.currentContractsPage = 1;
    }
    next();
  },
  data() {
    return {
      loading: false,
      contracts: [],
      displayContractsSearch: false,
      search: '',
      filterOn: ['name'],
      contractsPerPage: 7,
      currentContractsPage: 1,
      contractsCount: 0,
      fields: [
        { key: 'name', label: this.$i18n.t('pages.console.contracts.table.name'), tdClass: 'align-middle ellipsis', sortable: true },
        { key: 'contractId', label: this.$i18n.t('pages.console.contracts.table.id'), tdClass: 'align-middle ellipsis', sortable: true },
        { key: 'address', label: this.$i18n.t('pages.console.contracts.table.address'), tdClass: 'align-middle long' },
        { key: 'blockchain', label: this.$i18n.t('pages.console.contracts.table.blockchain'), tdClass: 'align-middle', sortable: true, class: 'text-end' },
        { key: 'actions', label: this.$i18n.t('pages.console.contracts.table.actions'), tdClass: 'align-middle actions', class: 'text-end' },
      ]
    }
  },
  computed: {
    rows() {
      return this.contractsCount;
    }
  }
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}
</style>
