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
  <header class="sticky-top">
    <nav class="navbar navbar-dark navbar-expand-lg bg-dark supra" aria-label="Supra navigation">
      <div class="container-xxl">
        <ul class="navbar-nav ms-auto">
          <li class="nav-item"><a class="nav-link" v-bind:class="{'active': lang === 'en' }" href="javascript:void(0)" v-on:click="setLang('en')">{{ $i18n.t('navbars.console.langs.en') }}</a></li>
          <li class="nav-item"><a class="nav-link" v-bind:class="{'active': lang === 'fr' }" href="javascript:void(0)" v-on:click="setLang('fr')">{{ $i18n.t('navbars.console.langs.fr') }}</a></li>
        </ul>
      </div>
    </nav>
    <nav class="navbar navbar-dark bg-dark navbar-expand-lg">
      <div class="container-xxl">
        <div class="navbar-brand me-auto me-lg-4">
          <router-link class="stretched-link" to="/">
            <img src="../../../assets/orange_logo.svg" width="50" height="50" alt="SCaaS - Back to Home" loading="lazy">
          </router-link>
          <h1 class="title">{{ $i18n.t('navbars.home.title') }}</h1>
        </div>
        <button class="navbar-toggler collapsed" type="button" data-bs-toggle="collapse" data-bs-target=".bd-navbar" aria-controls="bd-navbar1" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div id="bd-navbar1" class="navbar-collapse collapse me-lg-auto bd-navbar mb-0 border-0">
          <ul class="navbar-nav">
            <li class="nav-item">
              <router-link to="/console/contracts/" class="nav-link">{{ $i18n.t('navbars.console.tabs.contracts') }}</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/console/deployments/" class="nav-link">{{ $i18n.t('navbars.console.tabs.deployments') }}</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/console/applications/" class="nav-link">{{ $i18n.t('navbars.console.tabs.applications') }}</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/console/wallets/" class="nav-link">{{ $i18n.t('navbars.console.tabs.wallets') }}</router-link>
            </li>
            <li class="nav-item ms-5">
              <router-link to="/console/blockchains/" class="nav-link">{{ $i18n.t('navbars.console.tabs.blockchains') }}</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/console/blocks/" class="nav-link">{{ $i18n.t('navbars.console.tabs.blocks') }}</router-link>
            </li>
            <li class="nav-item">
              <router-link to="/console/transactions/" class="nav-link">{{ $i18n.t('navbars.console.tabs.transactions') }}</router-link>
            </li>
          </ul>
        </div>
        <div id="bd-navbar2" class="navbar-collapse collapse border-0">
          <ul class="navbar-nav flex-row">
            <li class="nav-item dropdown d-flex">
              <a href="#" class="nav-link" data-bs-toggle="dropdown" aria-expanded="false" data-bs-display="static">
                <img src="../../../assets/avatar.svg" width="33" height="33" role="img" alt="User" loading="lazy">
                <span class="visually-hidden">{{ $store.getters.user.name }}</span>
              </a>
              <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark position-absolute">
                <li>
                  <router-link to="/console/profile/" class="dropdown-item">{{ $i18n.t('navbars.console.dropdown.profile') }}</router-link>
                </li>
                <!--<li>
                  <router-link to="/console/company" class="nav-link">{{ $i18n.t('navbars.console.dropdown.company') }}</router-link>
                </li>-->
                <li><hr class="dropdown-divider"></li>
                <li>
                  <router-link to="/logout" class="nav-link">{{ $i18n.t('navbars.console.dropdown.signOut') }}</router-link>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </header>
</template>
<script>
export default {
  methods: {
    setLang(lang) {
      this.$i18n.locale = lang;

      // Set lang cookie
      document.cookie = `lang=${lang}; expires= Thu, 22 Feb 2100 20:00:00 UTC; path=/ `;

      // Refresh page
      location.reload();
    }
  },
  data() {
    return {
      lang: this.$i18n.locale,
      user: {},
    }
  },
  computed: {}
}
</script>
