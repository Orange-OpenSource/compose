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
    <div class="signin">
      <div class="text-center" style="position: relative; z-index: 999">
        <form class="form-signin" v-on:submit="signIn">
          <router-link to="/">
            <img src="../../assets/smartchain.svg" alt="SmartChain" height="200">
          </router-link>

          <h1 class="h2 mb-1 font-weight-normal">
            {{ $t('pages.login.title') }}
          </h1>

          <h2 class="h5 mb-4 font-weight-normal">
            {{ $t('pages.login.subtitle') }}
          </h2>

          <div class="alert alert-warning" role="alert" v-if="unauthorized">
              {{ $t('pages.login.error') }}
          </div>

          <div class="alert alert-danger text-start" role="alert" v-if="error">
            <span class="alert-icon"><span class="sr-only">Danger</span></span>
            <p>{{error}}</p>
          </div>

          <label for="inputEmail" class="sr-only">{{ $t('pages.login.form.username') }}</label>
          <input type="text" id="inputEmail" class="form-control mb-2" autocomplete="username" :placeholder="$t('pages.login.form.username')" v-model="username" required autofocus>

          <label for="inputPassword" class="sr-only">{{ $t('pages.login.form.password') }}</label>
          <div class="input-group mb-2">
            <input :type="inputType" id="inputPassword" class="form-control mb-0" autocomplete="current-password" :placeholder="$t('pages.login.form.password')" v-model="password" required>
            <span class="inputTypeToggle" v-on:click="toggleInputType">
              <i v-if="inputType === 'password'" class="fa fa-eye-slash" aria-hidden="true"></i>
              <i v-if="inputType === 'text'" class="fa fa-eye" aria-hidden="true"></i>
            </span>
          </div>

          <button class="btn btn-lg btn-primary btn-block" type="submit">
              {{ $t('pages.login.form.submit') }}
          </button>

          <p class="mt-5 mb-3 text-muted">&copy; 2023</p>
        </form>
      </div>
    </div>
</template>
<script>
export default {
  components: {},
  beforeCreate: function() {
    document.documentElement.className = 'login';
    document.body.className = '';

    setTimeout(function(){
      document.getElementById('app').className = '';
    }, 0);
  },
  methods: {
    toggleInputType: function(e) {
      e.preventDefault();

      if (this.inputType === 'password'){
        this.inputType = 'text';
      } else {
        this.inputType = 'password';
      }
    },
    signIn: function(e){
      e.preventDefault();

      this.error = null;
      this.unauthorized = false;

      let data = {
        username: this.username,
        password: this.password
      };

      this.$http.post("/authentication/signin", data).then(() => {
        this.$router.push({name: "Contracts"});
      }).catch((error) => {
        if ( error.response.status === 500 ){
          this.error = `${error.response.data.message} (${error.response.data.description})`;
        } else {
          this.unauthorized = true;
        }
      });
    }
  },
  data() {
    return {
      error: null,
      unauthorized: false,
      inputType: 'password',
      username: '',
      password: '',
    }
  },
}
</script>
<style>
#inputPassword {
  padding-right: 30px;
}
.inputTypeToggle {
  cursor: pointer;
  position: absolute;
  right: 10px;
  top: 13px;
  color: #9d9c9c;
  z-index: 999;
}
</style>
