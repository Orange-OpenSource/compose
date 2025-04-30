/*
 *
 *  *
 *  *  * Software Name : Compose
 *  *  * SPDX-FileCopyrightText: Copyright (c) Orange SA
 *  *  * SPDX-License-Identifier:  MIT
 *  *  *
 *  *  * This software is distributed under the MIT License,
 *  *  * see the "LICENSE.txt" file for more details or https://spdx.org/licenses/MIT.html
 *  *  *
 *  *  * <Authors: optional: authors list / see CONTRIBUTORS>
 *  *
 *
 */

import { createApp } from 'vue';

// Utils
import axios from 'axios';
import VueAxios from 'vue-axios'

// Aces
import ace from 'ace-builds';
import modeJsonUrl from 'ace-builds/src-noconflict/mode-json?url';
import 'ace-builds/src-noconflict/mode-php';
import 'ace-builds/src-noconflict/mode-java';
import 'ace-builds/src-noconflict/mode-ruby';
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/theme-chrome';

ace.config.setModuleUrl('ace/mode/json', modeJsonUrl);

// Routing
import router from './frontend/vue/router';

router.beforeEach(async (to, from, next) => {
  // Set page title
  const nearestWithTitle = to.matched.slice().reverse().find((r) => r.meta && r.meta.title);
  if (nearestWithTitle) {
    document.title = nearestWithTitle.meta.title;
  }

  if (to.meta.requiresAuth) {
    try {
      let response =  await axios.get('/authentication/check');

      store.commit('setUser', response.data);

      next();
      return;
    } catch(e){
      console.log("Not connected, redirecting to login page", e);
      next({name: 'Login', params: to.params, query: to.query});
      return;
    }
  }

  if (to.meta.checkAuth) {
    try {
      let response =  await axios.get('/authentication/check');

      store.commit('setUser', response.data);

      console.log("Connected, redirecting to console home page");
      next({name: 'Contracts'});
      return;
    } catch(e) {
      console.log("Not connected", e);
      next();
      return;
    }
  }

  if (to.meta.signOut) {
    await axios.get(`/authentication/signout`);

    console.log("Signed out, redirecting to login page");
    next({name: 'Login', params: to.params});
    return;
  }

  next();
});

// Internationalization
import i18n from './frontend/i18n/i18n';

// Store
import store from './frontend/vue/store'

// Assets import
import BootstrapVueNext from 'bootstrap-vue-next';

import '@fortawesome/fontawesome-free/css/all.css';

import 'bootstrap-vue-next/dist/bootstrap-vue-next.css'

import 'boosted/dist/css/boosted-grid.css';
import 'boosted/dist/css/boosted-reboot.css';
import 'boosted/dist/css/boosted-utilities.css';
import 'boosted/dist/css/boosted.css';
import 'boosted/dist/js/boosted.bundle.min';

import './frontend/css/main.css';

// Helpers
import helper from '@/frontend/vue/utils/helpers'

// App
import App from '@/frontend/vue/App.vue';

const app = createApp(App);

app.use(router);
app.use(store);
app.use(helper, store);
app.use(VueAxios, axios);
app.use(i18n);
app.use(BootstrapVueNext)

app.mount('#app');
