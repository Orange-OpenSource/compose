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

const rolesPlugin = {
  install(app, store) {
    app.config.globalProperties.$isAdmin = () => {
      for ( let i in store.getters.user.roles ){
        if ( store.getters.user.roles[i] === 'ADMIN'){
          return true;
        }
      }
      return false;
    }
  }
}

export default rolesPlugin;
