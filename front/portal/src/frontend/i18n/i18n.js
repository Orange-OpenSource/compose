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

import { createI18n } from 'vue-i18n'

const merge = (target, source) => {
  if (target === undefined) {
    target = {};
  }

  for (const key of Object.keys(source)) {
    if (source[key] instanceof Object) Object.assign(source[key], merge(target[key], source[key]));
  }

  // Join `target` and modified `source`
  Object.assign(target || {}, source);
  return target;
};

function loadLocaleMessages() {
  const locales = require.context('.', true, /[A-Za-z0-9-_,\s]+\.json$/i);
  const messages = {};
  locales.keys().forEach((key) => {
    const matched = key.match(/([A-Za-z0-9-_]+)\./i);
    if (matched && matched.length > 1) {
      const locale = matched[1];
      messages[locale] = merge(messages[locale], locales(key));
    }
  });
  return messages;
}

function getLang() {
  let lang = (window.navigator.language || window.navigator.userLanguage).substring(0, 2);

  const cookies = document.cookie.split(';').map((x) => x.trim().split('=')).reduce((a, b) => {
    a[b[0]] = b[1];
    return a;
  }, {});

  if (cookies.lang) {
    lang = cookies.lang;
  }

  return lang;
}

const i18n = createI18n({
  locale: getLang(),
  fallbackLocale: 'fr',
  messages: loadLocaleMessages(),
});

export default i18n;
