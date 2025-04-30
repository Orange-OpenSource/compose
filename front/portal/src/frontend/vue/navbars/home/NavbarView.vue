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
    <nav class="navbar navbar-dark bg-dark navbar-expand-lg">
      <div class="container-xxl">
        <div class="navbar-brand me-auto me-lg-4">
          <router-link class="stretched-link" to="/">
            <img src="../../../assets/orange_logo.svg" width="50" height="50" alt="Boosted - Back to Home" loading="lazy">
          </router-link>
          <h1 class="title">{{ $i18n.t('navbars.home.title') }}</h1>
        </div>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse me-lg-auto">
          <!-- Empty -->
        </div>
        <div class="collapse navbar-collapse" id="bdNavbar">
          <ul class="navbar-nav flex-row flex-wrap bd-navbar-nav pt-2 py-md-0" style="width: 100%">
          </ul>
          <ul class="navbar-nav flex-row flex-wrap bd-navbar-nav pt-2 py-md-0">
            <li class="nav-item col-6 col-md-auto text-end">
              <router-link :to="`/console`" class="nav-link">{{ $i18n.t('navbars.home.tabs.signIn') }}</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </header>
</template>
<script setup>
</script>
