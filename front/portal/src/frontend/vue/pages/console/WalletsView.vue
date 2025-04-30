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
          {{ $t('pages.console.wallets.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1>
          <span class="me-2">{{ $t('pages.console.wallets.title', {count: walletsCount}) }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>

        <div style="overflow: auto">
          <b-table
            id="wallets-table"
            ref="table"
            :items="wallets"
            :fields="fields"
            sort-by="name"
            :per-page="walletsPerPage"
            :current-page="currentWalletsPage"
            hover
          >
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
              <a href="javascript:void(0)" class="me-2" v-on:click="displayBalance(data.item['blockchain'], data.item['address'])" :title="$t('pages.console.wallets.displayBalance')">
                <i class="fas fa-hand-holding-dollar"></i>
              </a>
              <a href="javascript:void(0)" v-on:click="deleteWallet(data.item['blockchain'], data.item['address'])" :title="$t('pages.console.wallets.deleteWallet')">
                <i class="fas fa-trash"></i>
              </a>
            </template>
          </b-table>
        </div>

        <div class="float-start">
          <div class="dropdown">
            <button class="btn btn-sm btn-primary pt-1 ms-2" style="line-height: 15px;" type="button" id="addWalletDropdown" data-bs-toggle="dropdown" aria-expanded="false">
              <i class="fa fa-plus me-2" aria-hidden="true"></i>
              {{ $t('pages.console.wallets.addWallet') }}
            </button>
            <ul class="dropdown-menu" aria-labelledby="addWalletDropdown">
              <li><a class="dropdown-item" href="javascript:void(0)" v-on:click="addWallet()">{{ $t('pages.console.wallets.createWallet') }}</a></li>
              <li><a class="dropdown-item" href="javascript:void(0)" v-on:click="importWallet()">{{ $t('pages.console.wallets.importWallet') }}</a></li>
            </ul>
          </div>
        </div>

        <div
          v-if="rows > walletsPerPage"
          class="mt-3"
        >
          <b-pagination
            v-model="currentWalletsPage"
            @page-click="setPage"
            :total-rows="rows"
            :per-page="walletsPerPage"
            aria-controls="wallets-table"
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
    <display-balance ref="displayBalanceModal"></display-balance>
    <display-wallet ref="displayWalletModal"></display-wallet>
    <add-wallet ref="addWalletModal"></add-wallet>
    <import-wallet ref="importWalletModal"></import-wallet>
  </div>
</template>
<script>
import DisplayWallet from "@/frontend/vue/modals/console/DisplayWallet.vue";
import DisplayBalance from "@/frontend/vue/modals/console/DisplayBalance.vue";
import AddWallet from "@/frontend/vue/modals/console/AddWallet.vue";
import importWallet from "@/frontend/vue/modals/console/ImportWallet.vue";

export default {
  components: {DisplayBalance, AddWallet, importWallet, DisplayWallet},
  methods: {
    loadData() {
      this.loading = true;
      let self = this;

      let url = `/api/users/me/wallets`;

      this.$http.get(url).then(result => {
          self.loading = false;
          self.wallets = result.data;
          self.walletsCount = self.wallets.length;
        }, error => {
          self.loading = false;
          console.error(error);
      });
    },
    addWallet(){
      this.$refs['addWalletModal'].show();
    },
    importWallet(){
      this.$refs['importWalletModal'].show();
    },
    displayWallet(wallet){
      this.$refs['displayWalletModal'].show(wallet.blockchain, wallet.address);
    },
    displayBalance(blockchain, address){
      this.$refs['displayBalanceModal'].show(blockchain, address);
    },
    deleteWallet(blockchain, address) {
      let self = this;

      this.$parent.$parent.$parent.displayDialog(self.$i18n.t('pages.console.wallets.deleteWalletConfirmation'), {async: true}).then(function(dialog){
        self.$http.delete(`/api/users/me/wallets/${address}?blockchain=${encodeURIComponent(blockchain)}`).then(function(){
          dialog.displaySuccess(self.$i18n.t('pages.console.wallets.deleteWalletSuccess'));
          self.loadData();
        }).catch(function(error){
          dialog.displayError(error);
        });
      });
    },
    setPage(evt, page) {
      this.$router.push({ name: "Wallets", query: {page: page} });
    }
  },
  beforeMount(){
    if ( this.$route.query && this.$route.query.page ){
      this.currentWalletsPage = parseInt(this.$route.query.page);
    }

    this.loadData();
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.page) {
      this.currentWalletsPage = parseInt(to.query.page);
    } else {
      this.currentWalletsPage = 1;
    }
    next();
  },
  data() {
    return {
      loading: false,
      wallets: [],
      walletsPerPage: 7,
      currentWalletsPage: 1,
      walletsCount: 0,
      fields: [
        { key: 'name', label: this.$i18n.t('pages.console.wallets.table.name'), tdClass: 'align-middle', sortable: true },
        { key: 'walletId', label: this.$i18n.t('pages.console.wallets.table.id'), tdClass: 'align-middle ellipsis', sortable: true },
        { key: 'address', label: this.$i18n.t('pages.console.wallets.table.address'), tdClass: 'align-middle', sortable: true },
        { key: 'blockchain', label: this.$i18n.t('pages.console.wallets.table.blockchain'), tdClass: 'align-middle', sortable: true },
        { key: 'actions', label: this.$i18n.t('pages.console.wallets.table.actions'), tdClass: 'align-middle actions', class: 'text-end' },
      ]
    }
  },
  computed: {
    rows() {
      return this.walletsCount;
    },
  }
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}
</style>
