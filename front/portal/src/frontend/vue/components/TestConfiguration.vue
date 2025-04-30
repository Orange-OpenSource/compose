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
  <div class="alert alert-info mb-4">
    <div style="width: 100%">
      <div>
        <strong>{{ $t(`components.testConfiguration.${type}.title`) }}</strong>
        <div class="float-end">
          <a href="javascript:void(0)" v-on:click="toggle">
            <i v-bind:class="{'far fa-minus-square': expanded === true, 'far fa-plus-square': expanded === false}"></i>
          </a>
        </div>
      </div>
      <div class="mt-3" v-show="expanded === true">
        <p>{{ $t(`components.testConfiguration.${type}.description`) }}</p>
        <br />
        <strong>{{ $t(`components.testConfiguration.application.title`) }}</strong>
        <div class="input-group mb-3">
          <b-form-select
            class="form-control"
            :options="applications"
            v-model="tempApplicationId"
            @change="selectApplication"
          ></b-form-select>

          <label class="sr-only" for="inline-form-api-key">{{ $t(`components.testConfiguration.application.apiKey`) }}</label>
          <b-form-input
            id="inline-form-api-key"
            class="mb-2 mr-sm-2 mb-sm-0"
            autocomplete="off"
            :placeholder="$t(`components.testConfiguration.application.apiKey`)"
            v-model="tempApiKey"
            @input="$emit('update:apiKey', tempApiKey)"
          ></b-form-input>
        </div>
        <div v-if="type === 'full'">
          <strong>{{ $t(`components.testConfiguration.wallet.title`) }}</strong>
          <div class="input-group">
            <label class="sr-only" for="inline-form-wallet-password">{{ $t(`components.testConfiguration.wallet.password`) }}</label>
            <b-input-group>
              <b-form-input
                id="inline-form-wallet-password"
                class="mb-2 mr-sm-2 mb-sm-0"
                autocomplete="off"
                :type="inputType"
                :placeholder="$t(`components.testConfiguration.wallet.password`)"
                v-model="tempWalletPassword"
                @input="$emit('update:walletPassword', tempWalletPassword)"
              ></b-form-input>

              <b-input-group-append>
                <b-input-group-text v-on:click="toggleInputType">
                  <i v-if="inputType === 'password'" class="fa fa-eye-slash" aria-hidden="true"></i>
                  <i v-if="inputType === 'text'" class="fa fa-eye" aria-hidden="true"></i>
                </b-input-group-text>
              </b-input-group-append>
            </b-input-group>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  components: {},
  props: {
    type: {
      default: function(){
        return 'light';
      }
    },
    walletPassword: {
      type: String
    },
    apiKey: {
      type: String
    },
    applicationId: {
      type: String
    },
    blockchain: {
      type: String
    }
  },
  emits: ['update:walletPassword', 'update:apiKey', 'update:applicationId', 'update:blockchain'],
  computed: {},
  methods: {
    loadApplications(){
      let self = this;
      let url = `/api/users/me/applications`;
      this.$http.get(url).then(result => {
        for ( let i=0; i<result.data.length; i++){
          self.applications.push({
            value: result.data[i].applicationId,
            text: `${self.capitalizeFirstLetter(this.$i18n.t(`blockchains.${result.data[i].blockchain}`))} / ${result.data[i].name}`,
            blockchain: result.data[i].blockchain
          });

          if ( i === 0 ){
            self.tempApplicationId = result.data[0].applicationId;
            self.selectApplication();
          }
        }

        // Sort applications by name
        self.applications.sort((a, b) => a.text.localeCompare(b.text));
      }, error => {
        console.error(error);
      });
    },
    capitalizeFirstLetter(str){
      return str.charAt(0).toUpperCase() + str.slice(1);
    },
    selectApplication(){
      for ( let i=0; i<this.applications.length; i++ ){
        if ( this.tempApplicationId === this.applications[i].value ){
          this.tempBlockchain = this.applications[i].blockchain;
        }
      }

      this.$emit('update:applicationId', this.tempApplicationId);
      this.$emit('update:blockchain', this.tempBlockchain);
    },
    toggle(){
      (this.expanded === true)?this.expanded = false:this.expanded = true;
    },
    toggleInputType(e){
      e.preventDefault();
      (this.inputType === 'password')?this.inputType='text':this.inputType='password';
    }
  },
  data() {
    return {
      expanded: true,
      inputType: 'password',
      applications: [],
      tempApplicationId: '',
      tempBlockchain: '',
      tempApiKey: '',
      tempWalletPassword: '',
    }
  },
  beforeMount(){
    this.loadApplications();
  },
};
</script>
<style scoped>
.input-group-text {
  background-color: #ccc;
  cursor: pointer;
  width: 40px;
}
</style>
