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
          {{ $t('pages.console.profile.breadcrumb') }}
        </li>
      </ol>
    </nav>
    <div class="row">
      <div class="col-md-12">
        <div class="float-end">
          <span class="badge rounded-pill bg-primary" v-if="user.roles.includes('ADMIN')">{{ $t(`pages.console.profile.userRoles.admin`) }}</span>
          <span class="badge rounded-pill bg-secondary ms-1" v-if="user.roles.includes('USER')">{{ $t(`pages.console.profile.userRoles.user`) }}</span>
        </div>

        <h1 class="mb-0">
          <span class="me-2">{{ $t('pages.console.profile.title') }}</span>
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading">
            <span class="visually-hidden">Loading...</span>
          </span>
        </h1>

        <form ref="form" @submit.stop.prevent="submit" class="mt-4">
          <div class="alert alert-danger" role="alert" v-if="error">
            <span class="alert-icon"><span class="sr-only">Danger</span></span>
            <p>{{error}}</p>
          </div>
          <b-form-group
            :state="nameState"
            :label="$t('pages.console.profile.form.name')"
            label-for="user-name"
            label-class="is-required"
          >
            <b-form-input
              id="user-name"
              v-model="user.name"
              :state="nameState"
              aria-errormessage="invalidName"
              :aria-invalid="!nameState"
              required
            ></b-form-input>

            <b-form-invalid-feedback id="invalidName" class="mt-2">
              {{ $t('pages.console.profile.form.invalidName') }}
            </b-form-invalid-feedback>
          </b-form-group>

          <b-form-group
            :state="familyNameState"
            :label="$t('pages.console.profile.form.familyName')"
            label-for="user-familyName"
            label-class="is-required"
            class="mt-3"
          >
            <b-form-input
              id="user-lastName"
              v-model="user.familyName"
              :state="familyNameState"
              aria-errormessage="invalidfamilyName"
              :aria-invalid="!familyNameState"
              required
            ></b-form-input>

            <b-form-invalid-feedback id="invalidLastName" class="mt-2">
              {{ $t('pages.console.profile.form.invalidLastName') }}
            </b-form-invalid-feedback>
          </b-form-group>

          <b-form-group
            :state="telephoneState"
            :label="$t('pages.console.profile.form.telephone')"
            label-for="user-telephone"
            label-class="is-required"
            class="mt-3"
          >
            <b-form-input
              id="user-telephone"
              type="phone"
              v-model="user.telephone"
              :state="telephoneState"
              aria-errormessage="invalidTelephone"
              :aria-invalid="!telephoneState"
              required
            ></b-form-input>

            <b-form-invalid-feedback id="invalidTelephone" class="mt-2">
              {{ $t('pages.console.profile.form.invalidTelephone') }}
            </b-form-invalid-feedback>
          </b-form-group>

          <b-form-group
            :label="$t('pages.console.profile.form.email')"
            label-for="user-email"
            class="mt-3"
          >
            <b-form-input
              id="user-email"
              type="email"
              v-model="user.email"
              disabled="true"
            ></b-form-input>
          </b-form-group>
          <div class="text-end mt-3">
            <b-button
              variant="primary"
              @click="submit()"
              :disabled="saving"
            >
              {{ $t('pages.console.profile.form.submit') }}
              <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="saving"></span>
              <span class="visually-hidden">Loading...</span>
            </b-button>
          </div>
        </form>

        <h2 class="mt-5">{{ $t('pages.console.profile.passwordChangeTitle') }}</h2>
        <form ref="form" @submit.stop.prevent="submitNewPassword" class="mt-4">
          <div class="alert alert-danger" role="alert" v-if="newPasswordError">
            <span class="alert-icon"><span class="sr-only">Danger</span></span>
            <p>{{newPasswordError}}</p>
          </div>
          <b-form-group
              :state="newPasswordState"
              :label="$t('pages.console.profile.form.newPassword')"
              label-for="profile-newPassword"
              label-class="is-required"
          >
            <b-form-input
                id="profile-newPassword"
                v-model="newPassword.value"
                :state="newPasswordState"
                aria-describedby="newPasswordHelp"
                aria-errormessage="invalidNewPassword"
                :aria-invalid="!newPasswordState"
                required
            ></b-form-input>

            <b-form-text id="newPasswordHelp" class="d-block mt-1" v-html="$t('pages.console.profile.form.newPasswordHelp')"></b-form-text>

            <b-form-invalid-feedback id="invalidNewPassword" class="mt-2">
              {{ $t('pages.console.profile.form.invalidNewPassword') }}
            </b-form-invalid-feedback>
          </b-form-group>

          <b-form-group
              :state="newPasswordConfirmationState"
              :label="$t('pages.console.profile.form.newPasswordConfirmation')"
              label-for="profile-newPasswordConfirmation"
              label-class="is-required"
              class="mt-3"
          >
            <b-form-input
                id="profile-newPasswordConfirmation"
                v-model="newPassword.confirmation"
                :state="newPasswordConfirmationState"
                aria-errormessage="invalidNewPasswordConfirmation"
                :aria-invalid="newPasswordConfirmationState"
                required
            ></b-form-input>

            <b-form-invalid-feedback id="invalidNewPasswordConfirmation" class="mt-2">
              {{ $t('pages.console.profile.form.invalidNewPasswordConfirmation') }}
            </b-form-invalid-feedback>
          </b-form-group>
          <div class="text-end mt-3">
            <b-button
                variant="primary"
                @click="submitNewPassword()"
                :disabled="savingNewPassword"
            >
              {{ $t('pages.console.profile.form.submitNewPassword') }}
              <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="savingNewPassword"></span>
              <span class="visually-hidden">Loading...</span>
            </b-button>
          </div>
        </form>
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
      let url = `/api/users/me`;

      this.$http.get(url).then(result => {
        self.loading = false;
        self.user = result.data;
      }, error => {
        self.loading = false;
        console.error(error);
      });
    },
    submit(){
      let self = this;

      this.error = null;
      if ( this.validateData() === false ){
        return;
      }

      this.saving = true;
      this.$http.put(`/api/users/me`, this.user).then(function(response){
        self.saving = false;
        self.$nextTick(() => {
          self.user = response.data;
          self.$store.commit('setUser', {
            userId: response.data['_id'],
            ...response.data
          });
          self.$parent.$parent.$parent.displaySuccess(self.$i18n.t('pages.console.profile.form.updateUserSuccess'));
        });
      }).catch(function(error) {
        self.saving = false;
        self.error = `${error.message}`;
        console.error(error);
      });
    },
    validateData(){
      this.nameState = this.user.name !== '';
      this.familyNameState = this.user.familyName !== '';
      this.telephoneState = this.user.telephone !== '';

      return (this.nameState && this.familyNameState && this.telephoneState);
    },
    submitNewPassword(){
      let self = this;

      this.newPasswordError = null;
      if ( this.validateNewPassword() === false ){
        return;
      }

      this.savingNewPassword = true;
      this.$http.put(`/api/users/me/credentials`, {password: this.newPassword.value}).then(function(){
        self.savingNewPassword = false;
        self.$parent.$parent.$parent.displaySuccess(self.$i18n.t('pages.console.profile.form.newPasswordSuccess'));
      }).catch(function(error) {
        self.savingNewPassword = false;
        self.newPasswordError = `${error.message}`;
        console.error(error);
      });
    },
    validateNewPassword(){
      const strongRegex = new RegExp("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])(?=.{8,})");
      this.newPasswordState = strongRegex.test(this.newPassword.value);
      this.newPasswordConfirmationState = (this.newPassword.value === this.newPassword.confirmation);

      return this.newPasswordState && this.newPasswordConfirmationState;
    }
  },
  beforeMount(){
    this.loadData();
  },
  data() {
    return {
      loading: false,
      saving: false,
      savingNewPassword: false,
      error: null,
      newPasswordError: null,
      user: {
        name: '',
        familyName: '',
        email: '',
        telephone: '',
        roles: []
      },
      newPassword: {
        value: '',
        confirmation: ''
      },
      nameState: null,
      familyNameState: null,
      telephoneState: null,
      newPasswordState: null,
      newPasswordConfirmationState: null
    }
  },
}
</script>
<style scoped>
.badge {
  padding: 5px 10px;
}
</style>
