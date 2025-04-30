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

import {mount} from '@vue/test-utils';

import BootstrapVueNext from 'bootstrap-vue-next';
import i18n from '../../utils/i18nTest';

import DefaultDialog from '@/frontend/vue/dialogs/DefaultDialog.vue';

describe('Default Dialog', () => {
    let wrapper;

    beforeEach(() => {
      wrapper = mount(DefaultDialog, {
        attachTo: document.body,
        global: {
          plugins: [BootstrapVueNext, i18n]
        }
      });
    });

    it('renders the dialog with the correct title and content when show method is called', () => {
        const content = '<p>Dialog Content</p>';
        const options = {
          async: true
        };

        wrapper.vm.show(content, options);

        const dialog = wrapper.findComponent({ ref: 'dialog' });

        expect(dialog.exists()).toBe(true);
        expect(wrapper.vm.content).toBe('<p>Dialog Content</p>');
        expect(wrapper.vm.options.async).toBe(true);
    });

    it('resets the dialog state when the modal is hidden', () => {
      const content = '<p>Dialog Content</p>';
      const options = {
        async: true
      };

      wrapper.vm.show(content, options);
      wrapper.vm.reset();

      expect(wrapper.vm.content).toBe('');
      expect(wrapper.vm.options.async).toBe(false);
    });
});
