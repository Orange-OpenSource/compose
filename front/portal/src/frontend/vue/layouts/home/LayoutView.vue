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
    <Navbar/>
    <content-layout></content-layout>
    <default-modal ref="defaultModal"></default-modal>
    <default-dialog ref="defaultDialog"></default-dialog>
  </div>
</template>
<script>
import Navbar from "@/frontend/vue/navbars/home/NavbarView.vue";
import DefaultModal from "@/frontend/vue/modals/DefaultModal.vue";
import DefaultDialog from "@/frontend/vue/dialogs/DefaultDialog.vue";

import ContentLayout from "./ContentView.vue";

export default {
  name: "LayoutView",
  components: {
    Navbar,
    ContentLayout,
    DefaultModal,
    DefaultDialog,
  },
  beforeCreate: function() {
    document.documentElement.setAttribute("lang", this.$i18n.locale);

    document.documentElement.className = 'home';
    document.body.className = '';

    setTimeout(function(){
      document.getElementById('app').className = '';
    }, 0);
  },
  methods: {
    displayModal(title, content, options){
      this.$refs['defaultModal'].show(title, content, options);
    },
    displayDialog(content, options){
      return this.$refs['defaultDialog'].show(content, options);
    },
  }
}
</script>
