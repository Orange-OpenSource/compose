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
            ref="modal"
            size="lg"
            :title="title"
            @hidden="reset"
            :ok-only="options.okOnly"
            :ok-title="options.okTitle"
            :cancel-title="options.cancelTitle"
            no-close-on-backdrop
        >
            <p v-html="content"></p>
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
            show(title, content, options){
                this.title = title;
                this.content = content;

                this.options = Object.assign(this.options, options);

                if ( this.options.type === 'error' && !this.title ){
                    this.title = this.$i18n.t('modals.default.title.error');
                }

                if ( this.options.type === 'success' && !this.title ){
                    this.title = this.$i18n.t('modals.default.title.success');
                }

                this.$refs['modal'].show();
            },
            reset(){
                this.title = '';
                this.content = '';
                this.options.okOnly = true;
                this.options.okTitle = this.$i18n.t('modals.default.form.ok');
                this.options.cancelTitle = this.$i18n.t('modals.default.form.cancel');
            },
        },
        data() {
            return {
                title: '',
                content: '',
                options: {
                    okOnly: true,
                    okTitle: this.$i18n.t('modals.default.form.ok'),
                    cancelTitle: this.$i18n.t('modals.default.form.cancel'),
                }
            }
        }
    }
</script>
