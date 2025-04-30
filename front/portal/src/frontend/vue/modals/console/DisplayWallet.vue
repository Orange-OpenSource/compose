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
      :title="$t('modals.console.displayWallet.title')"
      @hidden="reset"
      no-close-on-backdrop
    >
      <div class="row">
        <div class="col-12">
          <small class="d-block mt-3" v-html="$t('modals.console.displayWallet.address', {address: wallet.address})"></small>
          <small class="d-block" v-html="$t('modals.console.displayWallet.version', {version: wallet.version})"></small>

          <small class="d-block mt-4" v-html="$t('modals.console.displayWallet.crypto.title')"></small>

          <small class="d-block mt-3" v-html="$t('modals.console.displayWallet.crypto.cipher', {cipher: wallet.crypto.cipher})"></small>
          <small class="d-block" v-html="$t('modals.console.displayWallet.crypto.ciphertext', {ciphertext: wallet.crypto.ciphertext})"></small>
          <small class="d-block" v-html="$t('modals.console.displayWallet.crypto.cipherparams.iv', {iv: wallet.crypto.cipherparams.iv})"></small>
          <small class="d-block mt-3" v-html="$t('modals.console.displayWallet.crypto.kdf', {kdf: wallet.crypto.kdf})"></small>
          <small class="d-block" v-html="$t('modals.console.displayWallet.crypto.kdfparams', {kdfparams: JSON.stringify(wallet.crypto.kdfparams)})"></small>
          <small class="d-block mt-3" v-html="$t('modals.console.displayWallet.crypto.mac', {mac: wallet.crypto.mac})"></small>
        </div>
      </div>

      <template v-slot:footer>
        <b-button
          variant="primary"
          @click="close"
        >
          {{ $t('modals.console.displayWallet.close') }}
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
    show(blockchain, walletAddress){
      let url = `/api/users/me/wallets/${walletAddress}?blockchain=${encodeURIComponent(blockchain)}`;
      this.$http.get(url).then(function(result){
        console.log(result);
        this.$refs['modal'].show();
      });
    },
    reset(){
      this.wallet = {
        address: '',
        version: '',
        crypto: {
          cipher: '',
          ciphertext: '',
          cipherparams: {
            iv: ''
          },
          kdf: '',
          kdfparams: '',
          mac: ''
        }
      };
    },
    close(bvModalEvt){
      bvModalEvt.preventDefault();
      this.$refs['modal'].hide();
    }
  },
  data() {
    return {
      wallet: {
        address: '',
        version: '',
        crypto: {
          cipher: '',
          ciphertext: '',
          cipherparams: {
            iv: ''
          },
          kdf: '',
          kdfparams: '',
          mac: ''
        }
      }
    }
  }
}
</script>
