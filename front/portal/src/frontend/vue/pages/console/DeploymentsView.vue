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
          {{ $t('pages.console.deployments.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1>
          <span class="me-2">{{ $t('pages.console.deployments.title', {count: deploymentsCount}) }}</span>
          <a class="h3" href="javascript:void(0)" @click.prevent="displayDeploymentsSearch === true ? displayDeploymentsSearch = false:displayDeploymentsSearch = true" :aria-label=" $t('pages.console.deployments.search.title')">
            <i class="fas fa-search"></i>
          </a>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>

        <div class="row border border-light p-4 mb-5" v-if="displayDeploymentsSearch === true">
          <div class="col-lg-12">
            <b-form-group
              :label="$t('pages.console.deployments.search.title')"
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
                  :placeholder="$t('pages.console.deployments.search.placeholder')"
                ></b-form-input>

                <b-input-group-append>
                  <b-button :disabled="!search" @click="search = ''">{{ $t('pages.console.deployments.search.clear') }}</b-button>
                </b-input-group-append>
              </b-input-group>
            </b-form-group>
          </div>
        </div>

        <div style="overflow: auto">
          <b-table
              id="deployments-table"
              ref="table"
              :items="deployments"
              :fields="fields"
              sort-by="name"
              :per-page="deploymentsPerPage"
              :current-page="currentDeploymentsPage"
              :filter="search"
              :filter-included-fields="filterOn"
              @filtered="onFiltered"
              hover
          >
            <template v-slot:cell(name)="data">
                {{ data.value }}
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

            <template v-slot:cell(status)="data">
              <span class="badge rounded-pill" v-bind:class="{ 'bg-primary': data.value === 'done', 'bg-secondary': data.value === 'ongoing'}">{{ $t(`pages.console.deployments.status.${data.value}`) }}</span>
            </template>

            <template v-slot:cell(actions)="data">
              <a href="javascript:void(0)" class="me-2" v-on:click="displayDeployment(data.item)" :title="$t('pages.console.deployments.displayDeployment')">
                <i class="fas fa-eye"></i>
              </a>
            </template>
          </b-table>
        </div>

        <div class="float-start">
          <button class="btn btn-sm btn-primary pt-1 ms-2" v-on:click="deployContract()" >
            <i class="fas fa-share-square me-2" aria-hidden="true"></i>
            {{ $t('pages.console.deployments.deployContract') }}
          </button>
          <button class="btn btn-link" v-on:click="displayDeploymentInfo()">
            <i class="fa fa-circle-info"></i>
          </button>
        </div>

        <div
            v-if="rows > deploymentsPerPage"
            class="mt-3"
        >
          <b-pagination
              v-model="currentDeploymentsPage"
              @page-click="setPage"
              :total-rows="rows"
              :per-page="deploymentsPerPage"
              aria-controls="deployments-table"
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
    <deploy-contract ref="uploadDeploymentModal"></deploy-contract>
    <display-deployment ref="displayDeploymentModal"></display-deployment>
    <display-deployment-info ref="displayDeploymentInfoModal"></display-deployment-info>
  </div>
</template>
<script>
import DeployContract from "@/frontend/vue/modals/console/DeployContract.vue";
import DisplayDeployment from "@/frontend/vue/modals/console/DisplayDeployment.vue";
import DisplayDeploymentInfo from "@/frontend/vue/modals/console/DisplayDeploymentInfo.vue";

export default {
  components: {
    DisplayDeploymentInfo,
    DisplayDeployment,
    DeployContract
  },
  methods: {
    loadData() {
      this.loading = true;

      this.deployments = [];
      this.ongoingDeployments = [];
      this.doneDeployments = [];
      this.deploymentsCount = 0;

      let self = this;

      let doneDeploymentsUrl = `/api/users/me/deployments`;
      let ongoingDeploymentsUrl = `/api/users/me/deployments/ongoing`;

      let doneDeploymentsPromise = this.$http.get(doneDeploymentsUrl).then(result => {
        self.doneDeployments = result.data
      }, error => {
        self.loading = false;
        console.error(error);
      });

      let ongoingDeploymentsPromise = this.$http.get(ongoingDeploymentsUrl).then(result => {
        self.ongoingDeployments = result.data
      }, error => {
        self.loading = false;
        console.error(error);
      });

      Promise.all([doneDeploymentsPromise, ongoingDeploymentsPromise]).then(function() {
        self.loading = false;

        for (let i=0; i<self.doneDeployments.length; i++){
          self.deployments.push({...self.doneDeployments[i], ...{status: 'done'}});
          self.deploymentsCount++;
        }

        for (let j=0; j<self.ongoingDeployments.length; j++){
          self.deployments.push({...self.ongoingDeployments[j], ...{status: 'ongoing'}});
          self.deploymentsCount++;
        }
      });
    },
    deployContract(){
      this.$refs['uploadDeploymentModal'].show();
    },
    displayDeployment(deployment){
      this.$refs['displayDeploymentModal'].show(deployment);
    },
    displayDeploymentInfo(){
      this.$refs['displayDeploymentInfoModal'].show();
    },
    onFiltered(filteredItems) {
      this.deploymentsCount = filteredItems.length;
      this.currentDeploymentsPage = 1;
    },
    setPage(evt, page) {
      this.$router.push({ name: "Deployments", query: {page: page} });
    }
  },
  beforeMount(){
    if ( this.$route.query && this.$route.query.page ){
      this.currentDeploymentsPage = parseInt(this.$route.query.page);
    }

    this.loadData();
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.page) {
      this.currentDeploymentsPage = parseInt(to.query.page);
    } else {
      this.currentDeploymentsPage = 1;
    }
    next();
  },
  data() {
    return {
      loading: false,
      deployments: [],
      doneDeployments: [],
      ongoingDeployments: [],
      displayDeploymentsSearch: false,
      search: '',
      filterOn: ['name'],
      deploymentsPerPage: 7,
      currentDeploymentsPage: 1,
      deploymentsCount: 0,
      fields: [
        { key: 'name', label: this.$i18n.t('pages.console.deployments.table.name'), tdClass: 'align-middle ellipsis', sortable: true },
        { key: 'deploymentId', label: this.$i18n.t('pages.console.deployments.table.deploymentId'), tdClass: 'align-middle ellipsis long' },
        { key: 'blockchain', label: this.$i18n.t('pages.console.deployments.table.blockchain'), tdClass: 'align-middle', sortable: true, class: 'text-center' },
        { key: 'status', label: this.$i18n.t('pages.console.deployments.table.status'), tdClass: 'align-middle', sortable: true, class: 'text-center' },
        { key: 'actions', label: this.$i18n.t('pages.console.deployments.table.actions'), tdClass: 'align-middle actions', class: 'text-end' },
      ]
    }
  },
  computed: {
    rows() {
      return this.deploymentsCount;
    }
  }
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}

.btn-link {
  text-decoration: none;
}
</style>
