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
          {{ $t('pages.console.company.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <h1>
          <span class="me-2">{{ $t('pages.console.company.title') }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>
        <div v-if="$isAdmin() === true">
          <h3>{{ $t('pages.console.company.users.title') }}</h3>
          <b-table
            id="users-table"
            ref="users-table"
            :items="company.users"
            :fields="fields"
            :per-page="usersPerPage"
            :current-page="currentUsersPage"
            hover
          >

            <template v-slot:cell(actions)="data">
              <a href="javascript:void(0)" v-on:click="deleteUser(data.item.id)" :aria-label="$t('pages.console.company.users.actions.deleteUser')" :title="$t('pages.console.company.users.actions.deleteQuestionnaire')">
                <i class="fa fa-trash" aria-hidden="true"></i>
              </a>
            </template>
          </b-table>

          <div class="mt-3">
            <button class="btn btn-primary float-start" v-on:click="addUser">
              {{ $t('pages.console.company.users.actions.addUser') }}
            </button>

            <b-pagination
              v-model="currentUsersPage"
              v-if="rows > usersPerPage"
              @page-click="setPage"
              :total-rows="rows"
              :per-page="usersPerPage"
              aria-controls="users-table"
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
    <add-user ref="addUserModal"></add-user>
  </div>
</template>
<script>
import AddUser from "@/frontend/vue/modals/console/AddUser.vue";
export default {
  components: {
    AddUser
  },
  methods: {
    loadData() {
      this.loading = true;
      let self = this;

      let url = `/api/companies/${this.$store.getters.user.companyId}`;

      this.$http.get(url).then(result => {
        self.loading = false;
        self.company = result.data;
      }, error => {
        self.loading = false;
        console.error(error);
      });
    },
    addUser(){
      this.$refs["addUserModal"].show();
    },
    deleteUser(id){
      console.log(id)
    },
    setPage(evt, page) {
      this.$router.push({ name: "Company", query: {page: page} });
    }
  },
  beforeMount(){
    if ( this.$route.query && this.$route.query.page ){
      this.currentUsersPage = parseInt(this.$route.query.page);
    }

    this.loadData();
  },
  beforeRouteUpdate (to, from, next) {
    if(to.query.page) {
      this.currentUsersPage = parseInt(to.query.page);
    } else {
      this.currentUsersPage = 1;
    }
    next();
  },
  data() {
    return {
      loading: false,
      company: {
        users: []
      },
      usersPerPage: 7,
      currentUsersPage: 1,
      usersCount: 0,
      fields: [
        { key: 'id', label: this.$i18n.t('pages.console.company.users.table.id'), tdClass: 'align-middle ellipsis', sortable: true, isRowHeader: true },
        { key: 'actions', label: this.$i18n.t('pages.console.company.users.table.actions'), tdClass: 'align-middle', class: 'text-end' },
      ]
    }
  },
  computed: {
    rows() {
      return this.usersCount;
    },
  }
}
</script>
