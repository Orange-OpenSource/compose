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
        :title="$t('modals.console.displayDeployment.title')"
        @hidden="reset"
        no-close-on-backdrop
    >
      <div class="row">
        <div class="col-12">
          <v-ace-editor
              v-model:value="deploymentString"
              lang="json"
              theme="chrome"
              @init="editorInit"
              style="height: 300px" />
        </div>
      </div>

      <template v-slot:footer>
        <b-button
          v-if="canAddContract() === true"
          variant="secondary"
          @click="addContractByAbi"
        >
          {{ $t('modals.console.displayDeployment.addContract') }}
        </b-button>
        <b-button
            variant="primary"
            @click="close"
        >
          {{ $t('modals.console.displayDeployment.close') }}
        </b-button>
      </template>
    </b-modal>
  </div>
</template>
<script>
import { BModal } from 'bootstrap-vue-next';
import { VAceEditor } from 'vue3-ace-editor';

export default {
  computed: {
  },
  components: {
    BModal,
    VAceEditor
  },
  methods: {
    show(deployment){
      this.deployment = deployment;
      this.deploymentString = JSON.stringify(deployment, null, 4);
      this.$refs['modal'].show();
    },
    canAddContract(){
      if ( this.deployment && this.deployment.networks && Object.keys(this.deployment.networks).length > 0 ){
        return true;
      }
      return false;
    },
    addContractByAbi(){
      this.$refs['modal'].hide();
      this.$router.push({ name: "Contracts", query: {
        action: "addContractByABI",
        blockchain: this.deployment.blockchain,
        address: Object.values(this.deployment.networks)[0].address,
        abi: JSON.stringify(this.deployment.abi, null, 4)
      }});
    },
    editorInit(editor){
      editor.setReadOnly(true);
      editor.setOption('wrap', true);
      editor.renderer.setShowGutter(false);
    },
    reset(){
      this.deployment = null;
      this.deploymentString = '';
    },
    close(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    }
  },
  data() {
    return {
      deployment: null,
      deploymentString: ''
    }
  }
}
</script>
