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

import { createRouter, createWebHistory } from 'vue-router';

import i18n from '../i18n/i18n';

import HomeLayout from '@/frontend/vue/layouts/home/LayoutView.vue';
import ConsoleLayout from '@/frontend/vue/layouts/console/LayoutView.vue';

import NotFoundPage from '@/frontend/vue/pages/NotFound.vue';

import LoginView from '@/frontend/vue/pages/LoginView.vue';
import HomeView from '@/frontend/vue/pages/public/HomeView.vue';
import ContractsView from "@/frontend/vue/pages/console/ContractsView.vue";
import ContractView from "@/frontend/vue/pages/console/ContractView.vue";
import EventView from "@/frontend/vue/pages/console/EventView.vue";
import DeploymentsView from "@/frontend/vue/pages/console/DeploymentsView.vue";
import ApplicationsView from "@/frontend/vue/pages/console/ApplicationsView.vue";
import WalletsView from "@/frontend/vue/pages/console/WalletsView.vue";
import BlockchainsView from "@/frontend/vue/pages/console/BlockchainsView.vue";
import TransactionsView from "@/frontend/vue/pages/console/TransactionsView.vue";
import BlocksView from "@/frontend/vue/pages/console/BlocksView.vue";
import CompanyView from "@/frontend/vue/pages/console/CompanyView.vue";
import ProfileView from "@/frontend/vue/pages/console/ProfileView.vue";

const routes = [
  {
    path: '/',
    component: HomeLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: HomeView,
        meta: {
          title: i18n.global.t('pages.home.meta.title'),
          checkAuth: true
        }
      }
    ]
  },
  {
    path: '/console',
    component: ConsoleLayout,
    redirect: '/console/contracts',
    children: [
      {
        path: 'contracts',
        name: 'Contracts',
        component: ContractsView,
        meta: {
          title: i18n.global.t('pages.console.contracts.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'contracts/:address',
        name: 'Contract',
        component: ContractView,
        meta: {
          title: i18n.global.t('pages.console.contract.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'contracts/:address/events/:eventName',
        name: 'Event',
        component: EventView,
        meta: {
          title: i18n.global.t('pages.console.event.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'deployments',
        name: 'Deployments',
        component: DeploymentsView,
        meta: {
          title: i18n.global.t('pages.console.deployments.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'applications',
        name: 'Applications',
        component: ApplicationsView,
        meta: {
          title: i18n.global.t('pages.console.applications.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'wallets',
        name: 'Wallets',
        component: WalletsView,
        meta: {
          title: i18n.global.t('pages.console.wallets.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'blockchains',
        name: 'Blockchains',
        component: BlockchainsView,
        meta: {
          title: i18n.global.t('pages.console.blockchains.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: TransactionsView,
        meta: {
          title: i18n.global.t('pages.console.transactions.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'blocks',
        name: 'Blocks',
        component: BlocksView,
        meta: {
          title: i18n.global.t('pages.console.blocks.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'company',
        name: 'Company',
        component: CompanyView,
        meta: {
          title: i18n.global.t('pages.console.company.meta.title'),
          requiresAuth: true,
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfileView,
        meta: {
          title: i18n.global.t('pages.console.profile.meta.title'),
          requiresAuth: true,
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: {
      title: i18n.global.t('pages.login.meta.title'),
      checkAuth: true,
    }
  },
  {
    path: '/logout',
    name: 'Logout',
    meta: {
      signOut: true,
    },
  },
  {
    path: "/:catchAll(.*)",
    name: "NotFound",
    component: NotFoundPage,
  },
];

const router = createRouter({
  linkActiveClass: "active",
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
