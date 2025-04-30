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
        ref="dialog"
        :title="title"
        @hidden="reset"
        @ok="confirm"
        no-close-on-backdrop
    >
      <div class="alert alert-danger" role="alert" v-if="error">
        <span class="alert-icon"><span class="sr-only">Danger</span></span>
        <p>{{error}}</p>
      </div>
      <p>{{content}}</p>
      <template v-slot:footer>
        <b-button
            variant="secondary"
            @click="handleCancel"
            v-if="currentStep === 0"
        >
          {{ $t('dialogs.default.form.cancel') }}
        </b-button>

        <b-button
            variant="primary"
            @click="confirm"
            v-if="currentStep === 0"
        >
          {{ $t('dialogs.default.form.confirm') }}
          <span class="spinner-border spinner-border-sm ms-2" role="status" v-show="loading"></span>
          <span class="visually-hidden">Loading...</span>
        </b-button>

        <b-button
            variant="primary"
            @click="handleOk"
            v-if="currentStep === 1"
        >
          {{ $t('dialogs.default.form.ok') }}
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next'

export default {
  components: {
    BModal,
  },
  methods: {
    show(content, options){
      let self = this;

      this.content = content;
      this.options = Object.assign(this.options, options);

      this.$refs['dialog'].show();

      return new Promise((successCallback) => {
        self.successCallback = successCallback;
      });
    },
    reset(){
      this.loading = false;
      this.currentStep = 0;
      this.error = null;
      this.title = this.$i18n.t('dialogs.default.title.confirmation');
      this.content = '';
      this.successCallback = null;
      this.options.async = false;
    },
    handleOk(bvModalEvt){
      bvModalEvt.preventDefault();
      this.hide();
    },
    handleCancel(bvModalEvt){
      bvModalEvt.preventDefault();
      this.hide();
    },
    confirm(bvModalEvt){
      if ( this.options.async === true ){
        bvModalEvt.preventDefault();
        this.loading = true;
      } else {
        this.hide();
      }

      this.successCallback(this);
    },
    displaySuccess(content){
      this.title = this.$i18n.t('dialogs.default.title.success');
      this.content = content;
      this.currentStep = 1;
    },
    displayError(error){
      this.title = this.$i18n.t('dialogs.default.title.error');
      this.content = '';
      this.error = error;
      this.currentStep = 1;
    },
    hide(){
      this.$refs['dialog'].hide();
    }
  },
  data() {
    return {
      loading: false,
      currentStep: 0,
      error: null,
      title: this.$i18n.t('dialogs.default.title.confirmation'),
      content: '',
      successCallback: null,
      options: {
        async: false,
      }
    }
  }
}
</script>
