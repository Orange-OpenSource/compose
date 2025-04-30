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
          {{ $t('pages.console.applications.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1>
          <span class="me-2">{{ $t('pages.console.applications.title', {count: applicationsCount}) }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>

        <div style="overflow: auto">
          <b-table
            id="applications-table"
            ref="table"
            :items="applications"
            :fields="fields"
            sort-by="name"
            :per-page="applicationsPerPage"
            :current-page="currentApplicationsPage"
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
              <a href="javascript:void(0)" v-on:click="deleteApplication(data.item['applicationId'])" :title="$t('pages.console.applications.deleteApplication')">
                <i class="fas fa-trash"></i>
              </a>
            </template>
          </b-table>
        </div>

        <div class="float-start">
          <button class="btn btn-sm btn-primary pt-1 ms-2" v-on:click="addApplication()" >
            <i class="fa fa-plus me-2" aria-hidden="true"></i>
            {{ $t('pages.console.applications.addApplication') }}
          </button>
        </div>

        <div
          v-if="rows > applicationsPerPage"
          class="mt-3"
        >
          <b-pagination
            v-model="currentApplicationsPage"
            @page-click="setPage"
            :total-rows="rows"
            :per-page="applicationsPerPage"
            aria-controls="applications-table"
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
    <add-application ref="addApplicationModal"></add-application>
  </div>
</template>
<script>
import AddApplication from "@/frontend/vue/modals/console/AddApplication.vue";

export default {
  components: {
    AddApplication
  },
  methods: {
    loadData() {
      this.loading = true;
      let self = this;

      let url = `/api/users/me/applications`;

      this.$http.get(url).then(result => {
        self.loading = false;
        self.applications = result.data;
        self.applicationsCount = self.applications.length;
      }, error => {
        self.loading = false;
        console.error(error);
      });
    },
    addApplication(){
      this.$refs['addApplicationModal'].show();
    },
    deleteApplication(id){
      let self = this;

      this.$parent.$parent.$parent.displayDialog(self.$i18n.t('pages.console.applications.deleteApplicationConfirmation'), {async: true}).then(function(dialog){
        self.$http.delete(`/api/users/me/applications/${id}`).then(function(){
          dialog.displaySuccess(self.$i18n.t('pages.console.applications.deleteApplicationSuccess'));
          self.loadData();
        }).catch(function(error){
          dialog.displayError(error);
        });
      });
    },
    setPage(evt, page) {
      this.$router.push({ name: "Applications", query: {page: page} });
    }
  },
  beforeMount(){
    if ( this.$route.query && this.$route.query.page ){
      this.currentApplicationsPage = parseInt(this.$route.query.page);
    }

    this.loadData();
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.page) {
      this.currentApplicationsPage = parseInt(to.query.page);
    } else {
      this.currentApplicationsPage = 1;
    }
    next();
  },
  data() {
    return {
      loading: false,
      applications: [],
      applicationsPerPage: 7,
      currentApplicationsPage: 1,
      applicationsCount: 0,
      fields: [
        { key: 'name', label: this.$i18n.t('pages.console.applications.table.name'), tdClass: 'align-middle ellipsis', sortable: true },
        { key: 'applicationId', label: this.$i18n.t('pages.console.applications.table.id'), tdClass: 'align-middle', sortable: true },
        { key: 'blockchain', label: this.$i18n.t('pages.console.applications.table.blockchain'), tdClass: 'align-middle', sortable: true },
        { key: 'actions', label: this.$i18n.t('pages.console.applications.table.actions'), tdClass: 'align-middle actions', class: 'text-end' },
      ]
    }
  },
  computed: {
    rows() {
      return this.applicationsCount;
    }
  }
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}
</style>
