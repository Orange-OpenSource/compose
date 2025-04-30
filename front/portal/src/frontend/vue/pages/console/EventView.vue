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
        <li class="breadcrumb-item">
          <router-link to="/console/contracts">
            {{ $t('pages.console.contracts.breadcrumb') }}
          </router-link>
        </li>
        <li class="breadcrumb-item">
          <router-link :to="`/console/contracts/${contractAddress}`">
            {{ contractAddress }}
          </router-link>
        </li>
        <li class="breadcrumb-item">
          <router-link :to="`/console/contracts/${contractAddress}?tab=events`">
            {{ $t('pages.console.events.breadcrumb') }}
          </router-link>
        </li>
        <li class="breadcrumb-item active" aria-current="location">
          {{ eventName }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1 class="mb-0">
          <span class="me-2">{{ eventName }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>
        <small class="d-block text-muted">{{ $t('pages.console.event.parameters', {parameters: event.inputs.map(function(elem){return `${elem.name} (${elem.type})`;}).join(', ')}) }}</small>
      </div>
    </div>
    <div class="row mt-3">
      <div class="col-12">
        <test-configuration type="light" v-model:api-key="apiKey" v-model:application-id="applicationId"></test-configuration>
      </div>
    </div>
    <div class="row">
      <ul class="nav nav-tabs nav-tabs-light mt-4" id="nav-tab" role="tablist">
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'listeners'}" v-on:click="setTab('listeners')" id="event-listeners-tab" type="button" role="tab" aria-controls="listeners" :aria-selected="activeTab === 'listeners'">{{ $t(`pages.console.event.tabs.listeners`) }}</button>
        </li>
        <li class="nav-item">
          <button class="nav-link" v-bind:class="{'active': activeTab === 'events'}" v-on:click="setTab('events')" id="event-events-tab" type="button" role="tab" aria-controls="events" :aria-selected="activeTab === 'events'">{{ $t(`pages.console.event.tabs.events`) }}</button>
        </li>
      </ul>
      <div class="tab-content" style="border: none">
        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'listeners'}" id="listeners" role="tabpanel" aria-labelledby="event-listeners-tab">
          <div class="row">
            <div class="col-md-12">
              <div class="mt-3 mb-3">
                <h5 class="mb-3">{{ $t(`pages.console.event.listeners.title`, {count: listeners.length}) }}</h5>

                <div style="overflow: auto">
                  <b-table
                    id="eventListeners-table"
                    ref="table"
                    :items="listeners"
                    :fields="eventListenersFields"
                    sort-by="webhookId"
                    :per-page="eventListenersPerPage"
                    :current-page="currentEventListenersPage"
                    hover
                  >
                    <template v-slot:cell(actions)="data">
                      <a href="javascript:void(0)" v-on:click="deleteEventListener(data.item.webhookId)" :title="$t('pages.console.event.listeners.deleteListener')">
                        <i class="fas fa-trash"></i>
                      </a>
                    </template>
                  </b-table>
                </div>

                <div class="float-start">
                  <div class="dropdown float-end">
                    <button class="btn btn-sm btn-primary pt-1 ms-2" v-on:click="addEventListener()" >
                      <i class="fas fa-plus me-2" aria-hidden="true"></i>
                      {{ $t('pages.console.event.addEventListener') }}
                    </button>
                  </div>
                </div>

                <div
                  v-if="eventListenersRows > eventListenersPerPage"
                  class="mt-3"
                >
                  <b-pagination
                    v-model="currentEventListenersPage"
                    @page-click="setEventListenersPage"
                    :total-rows="eventListenersRows"
                    :per-page="eventListenersPerPage"
                    aria-controls="eventListeners-table"
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
          </div>
        </div>
        <div class="tab-pane fade" v-bind:class="{'show active': activeTab === 'events'}" id="events" role="tabpanel" aria-labelledby="event-events-tab">
          <div class="row">
            <div class="col-12">
              <div class="mt-3 mb-3">
                <h5 class="mb-3">{{ $t(`pages.console.event.events.title`) }}</h5>

                <div class="alert alert-danger" role="alert" v-if="error">
                  <span class="alert-icon"><span class="sr-only">Danger</span></span>
                  <p>{{error}}</p>
                </div>

                <div style="overflow: auto" v-if="events !== null">
                  <b-table
                    id="events-table"
                    ref="table"
                    :items="events"
                    :fields="eventsFields"
                    :per-page="eventsPerPage"
                    :current-page="currentEventsPage"
                    hover
                  >
                    <template v-slot:cell(parameters)="data">
                      <div v-for="(parameter, index) in data.item.parameters" :key="index">
                        <strong>{{ parameter.name }}:</strong> {{ parameter.value }}
                      </div>
                    </template>
                  </b-table>
                </div>

                <div class="float-start">
                  <div class="dropdown float-end">
                    <button class="btn btn-sm btn-primary pt-1 ms-2" v-on:click="loadEvents()" >
                      <i class="fas fa-list me-2" aria-hidden="true"></i>
                      {{ $t('pages.console.event.loadEvents') }}
                    </button>
                  </div>
                </div>

                <div
                  v-if="eventsRows > eventsPerPage"
                  class="mt-3"
                >
                  <b-pagination
                    v-model="currentEventsPage"
                    :total-rows="eventsRows"
                    :per-page="eventsPerPage"
                    aria-controls="events-table"
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
          </div>
        </div>
      </div>
    </div>
    <add-event-listener ref="addEventListenerModal"></add-event-listener>
  </div>
</template>
<script>
import TestConfiguration from "@/frontend/vue/components/TestConfiguration.vue";
import AddEventListener from "@/frontend/vue/modals/console/AddEventListener.vue";

export default {
  components: {
    AddEventListener,
    TestConfiguration
  },
  methods: {
    loadData() {
      let self = this;
      this.loading = true;

      let contractUrl = `/api/users/me/contracts/${this.contractAddress}`;
      let contractPromise = this.$http.get(contractUrl).then(function(result){
        self.contract = result.data;
        let abi = JSON.parse(self.contract.abi);

        for ( let i=0; i<abi.length; i++ ){
          if ( abi[i].type === "event" && abi[i].name === self.eventName ){
            self.event = abi[i];
          }
        }
      }).catch(function(error) {
        self.loading = false;
        self.error += `${error.response.statusText} (Error status ${error.response.status})<br />`;
        console.error(error);
      });

      let eventListenersUrl = `/api/users/me/contracts/${this.contractAddress}/events/${this.eventName}/webhooks`;
      let eventListenersPromise = this.$http.get(eventListenersUrl).then(function(result){
        self.listeners = result.data;
        self.eventListenersCount = self.listeners.length;
      }).catch(function(error) {
        self.loading = false;
        self.error += `${error.response.statusText} (Error status ${error.response.status})<br />`;
        console.error(error);
      });

      Promise.all([contractPromise, eventListenersPromise]).then(function() {
        self.loading = false;
      });
    },
    loadEvents(){
      this.error = null;
      let self = this;

      if ( this.apiKey === '' ){
        return this.$parent.$parent.$parent.displayModal(this.$i18n.t('pages.console.event.events.warningTitle'), this.$i18n.t('pages.console.event.events.warning'));
      }

      let eventsUrl = `/api/blockchains/${this.contract.blockchain}/contracts/${this.contractAddress}/events/${this.eventName}`;
      this.$http.get(eventsUrl, {
        headers: {
          'application-id': this.applicationId,
          'api-key': this.apiKey
        }
      }).then(function(result){
        self.events = result.data;
        self.eventsCount = self.events.length;
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.response.statusText} (Error status ${error.response.status})`;
        console.error(error);
      });
    },
    addEventListener(){
      this.$refs['addEventListenerModal'].show(this.contract.blockchain, this.contractAddress, this.eventName);
    },
    deleteEventListener(id){
      let self = this;

      this.$parent.$parent.$parent.displayDialog(self.$i18n.t('pages.console.event.listeners.deleteEventListenerConfirmation'), {async: true}).then(function(dialog){
        self.$http.delete(`/api/users/me/contracts/${self.contractAddress}/events/${self.eventName}/webhooks/${id}`).then(function(){
          dialog.displaySuccess(self.$i18n.t('pages.console.event.listeners.deleteEventListenerSuccess'));
          self.loadData();
        }).catch(function(error){
          dialog.displayError(error);
        });
      });
    },
    setEventListenersPage(evt, page){
      this.$router.push({ name: "Event", query: {eventListenersPage: page} });
    },
    setTab(tab) {
      this.activeTab = tab;
      this.$router.push({ name: "Event", query: {tab: this.activeTab} });
    },
  },
  beforeMount(){
    this.contractAddress = this.$route.params.address;
    this.eventName = this.$route.params.eventName;

    if (this.$route.query && this.$route.query.tab){
      this.activeTab = this.$route.query.tab;
    }

    this.loadData();
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.tab) {
      this.activeTab = to.query.tab;
    } else {
      this.activeTab = 'events';
    }

    if(to.query.eventListenersPage) {
      this.currentEventListenersPage = parseInt(to.query.eventListenersPage);
    } else {
      this.currentEventListenersPage = 1;
    }
    next();
  },
  data() {
    return {
      loading: false,
      error: null,
      activeTab: 'listeners',
      contractAddress: null,
      eventName: null,
      contract: null,
      event: {
        inputs: []
      },
      events: null,
      eventsPerPage: 7,
      currentEventsPage: 1,
      eventsCount: 0,
      listeners: [],
      eventListenersPerPage: 7,
      currentEventListenersPage: 1,
      eventListenersCount: 0,
      applicationId: '',
      apiKey: '',
      eventListenersFields: [
        { key: 'webhookId', label: this.$i18n.t('pages.console.event.listeners.table.id'), tdClass: 'align-middle', sortable: true },
        { key: 'url', label: this.$i18n.t('pages.console.event.listeners.table.url'), tdClass: 'align-middle' },
        { key: 'actions', label: this.$i18n.t('pages.console.event.listeners.table.actions'), tdClass: 'align-middle actions', class: 'text-end' }
      ],
      eventsFields: [
        { key: 'blockNumber', label: this.$i18n.t('pages.console.event.events.table.blockNumber'), tdClass: 'align-middle' },
        { key: 'name', label: this.$i18n.t('pages.console.event.events.table.name'), tdClass: 'align-middle' },
        { key: 'address', label: this.$i18n.t('pages.console.event.events.table.address'), tdClass: 'align-middle' },
        { key: 'parameters', label: this.$i18n.t('pages.console.event.events.table.parameters'), tdClass: 'align-middle' }
      ]
    }
  },
  computed: {
    eventListenersRows() {
      return this.eventListenersCount;
    },
    eventsRows(){
      return this.eventsCount;
    }
  }
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}
</style>
