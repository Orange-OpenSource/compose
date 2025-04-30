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
    <div class="header">
      <div class="particles_container">
        <particles-cloud />
      </div>
      <h1 class="text-white mb-0">{{ $t('pages.home.title') }}</h1>
      <h3 class="text-white">{{ $t('pages.home.subtitle') }}</h3>
    </div>

    <div class="container">
      <div class="row featurette mt-3">
        <div class="col-md-7">
          <h2 class="featurette-heading" v-html="$t('pages.home.feature1.title')"></h2>
          <p class="lead">{{ $t('pages.home.feature1.description') }}</p>
        </div>
        <div class="col-md-5 text-center">
          <img src="../../../assets/smartchain.svg" alt="" height="300" style="margin-top: 120px;">
        </div>
      </div>
      <hr class="featurette-divider">
      <div class="row featurette">
        <div class="col-md-7 order-md-2">
          <h2 class="featurette-heading" v-html="$t('pages.home.feature2.title')"></h2>
          <p class="lead">{{ $t('pages.home.feature2.description') }}</p>
        </div>
        <div class="col-md-5 order-md-1">
          <img src="../../../assets/coder.png" alt="" width="400" style="margin-top: 120px;">
        </div>
      </div>
      <hr class="featurette-divider">
      <div class="row featurette">
        <div class="col-md-7">
          <h2 class="featurette-heading" v-html="$t('pages.home.feature3.title')"></h2>
          <p class="lead">{{ $t('pages.home.feature3.description') }}</p>
        </div>
        <div class="col-md-5 text-center">
          <img src="../../../assets/innov.png" alt="" style="height: 350px; margin-top: 75px;">
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import ParticlesCloud from "../../components/Particles.vue";

export default {
  components: {
    ParticlesCloud
  },
  mounted() {
    setTimeout(function(){
      window.dispatchEvent(new Event('resize'));
    }, 1);
  },
  beforeCreate: function() {
    document.documentElement.className = 'homepage h-100';
    document.body.className = 'd-flex h-100';

    setTimeout(function(){
      document.getElementById('app').className = 'd-flex w-100 h-100 mx-auto flex-column';
    }, 0);
  },
}
</script>
<style>
.header {
  position: relative;
  height: 350px;
  overflow: hidden;
}

.particles_container {
  position: absolute;
  height: 350px;
  top: 0;
  width: 100%;
  z-index: 100;
}

#particles-js {
  background: #111;
  height: 350px;
  width: 100%;
}

.header h1,
.header h3 {
  padding: 10px;
  position: absolute;
  bottom: 100px;
  left: 20%;
  font-weight: 300;
  line-height: 1;
  letter-spacing: -.05rem;
  font-size: 50px;
  z-index: 999;
}

.header h3 {
  padding: 7px;
  bottom: 40px;
  left: 20%;
  font-weight: 300;
  line-height: 1;
  letter-spacing: -.05rem;
  font-size: 30px;
  z-index: 999;
}

.encart_1 small {
  color: #464646;
  width: 300px;
}

.featurette-divider {
  border-width: 1px;
  margin: 5rem 0;
  height: 1px !important;
}

.featurette-heading {
  font-weight: 300;
  line-height: 1;
  letter-spacing: -.05rem;
}

@media (min-width: 40em) {
  .featurette-heading {
    font-size: 50px;
  }
}

@media (min-width: 62em) {
  .featurette-heading {
    margin-top: 7rem;
  }
}

@media (max-width: 768px) {
  .section_1 {
    height: 375px;
  }

  .encart_1 {
    margin-top: 65px !important;
  }

  .encart_1 small {
    width: 100%;
  }

  .featurette img {
    margin-top: 15px !important;
    height: auto !important;
    width: 100% !important;
  }

  .featurette-heading {
    font-size: 30px;
  }
}
</style>
