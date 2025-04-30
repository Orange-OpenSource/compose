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
    <navbar-view/>
    <content-layout></content-layout>
    <default-modal ref="defaultModal"></default-modal>
    <default-dialog ref="defaultDialog"></default-dialog>
  </div>
</template>
<script>
import NavbarView from "@/frontend/vue/navbars/console/NavbarView.vue";
import ContentLayout from "./ContentView.vue";
import DefaultModal from "@/frontend/vue/modals/DefaultModal.vue";
import DefaultDialog from "@/frontend/vue/dialogs/DefaultDialog.vue";

export default {
  name: "LayoutView",
  components: {
    NavbarView,
    ContentLayout,
    DefaultModal,
    DefaultDialog,
  },
  beforeCreate: function() {
    document.documentElement.setAttribute("lang", this.$i18n.locale);

    document.documentElement.className = 'console';
    document.body.className = '';

    setTimeout(function(){
      document.getElementById('app').className = '';
    }, 0);
  },
  methods: {
    displayModal(title, content, options){
      this.$refs['defaultModal'].show(title, content, options);
    },
    displayError(error){
      const content = `${error.message}`;
      this.$refs['defaultModal'].show(null, content, {type: 'error'});
      console.error(error);
    },
    displaySuccess(content){
      this.$refs['defaultModal'].show(null, content, {type: 'success'});
    },
    displayDialog(content, options){
      return this.$refs['defaultDialog'].show(content, options);
    },
  }
}
</script>
