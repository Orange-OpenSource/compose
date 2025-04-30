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
  <div>
    <b-modal
      static
      size="xl"
      ref="modal"
      :title="$t('modals.console.addUser.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>
      <b-form-group
        :state="emailState"
        :label="$t('modals.console.addUser.form.email')"
        label-for="user-email"
        label-class="is-required"
        class="mt-3 mb-1"
      >
        <b-form-input
          id="user-email"
          type="email"
          v-model="email"
          :state="emailState"
          required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.addUser.form.invalidEmail') }}
        </b-form-invalid-feedback>
      </b-form-group>

      <b-form-group
        :state="passwordState"
        :label="$t('modals.console.addUser.form.password')"
        label-for="user-password"
        label-class="is-required"
        class="mt-3 mb-1"
      >
        <b-form-input
          id="user-password"
          type="password"
          v-model="password"
          :state="passwordState"
          required
        ></b-form-input>

        <b-form-invalid-feedback>
          {{ $t('modals.console.addUser.form.invalidPassword') }}
        </b-form-invalid-feedback>
      </b-form-group>


      <template v-slot:footer>
        <b-button
          variant="secondary"
          @click="handleCancel"
        >
          {{ $t('modals.console.addUser.form.cancel') }}
        </b-button>

        <b-button
          variant="primary"
          @click="handleOk"
        >
          {{ $t('modals.console.addUser.form.submit') }}
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
          <span class="visually-hidden">Loading...</span>
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next';

export default {
  components: {
    BModal
  },
  methods: {
    show(){
      this.$refs['modal'].show();
    },
    reset(){
      this.error = false;

      this.email = '';
      this.password = '';
    },
    submit(){
      let self = this;

      this.error = null;
      this.loading = true;

      this.$http.post(`/api/users`, {
        email: this.email,
        password: this.password
      }).then(function(){
        self.loading = false;
        self.$nextTick(() => {
          self.$refs['modal'].hide();
          self.$parent.loadData();
        });
      }).catch(function(error) {
        self.loading = false;
        self.error = `${error.message}`;
        console.error(error);
      });
    },
    validateData(){
      this.emailState = (this.email !== '' && /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(this.email));
      this.passwordState = (this.password !== '' && /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%!]).{8,}$/.test(this.password));

      return (this.passwordState && this.emailState);
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();

      if ( this.validateData() === true ){
        this.submit();
      }
    },
    handleCancel(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    },
  },
  data() {
    return {
      error: false,
      loading: false,
      emailState: null,
      passwordState: null,
      email: '',
      password: ''
    }
  }
}
</script>
