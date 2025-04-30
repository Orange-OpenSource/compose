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

import DefaultModal from '@/frontend/vue/modals/DefaultModal.vue';

describe('Default Modal', () => {
    let wrapper;

    beforeEach(() => {
      wrapper = mount(DefaultModal, {
        attachTo: document.body,
        global: {
          plugins: [BootstrapVueNext, i18n]
        }
      });
    });

    it('renders the modal with the correct title and content when show method is called', () => {
        const title = 'Modal Title';
        const content = '<p>Modal Content</p>';
        const options = {
            okOnly: false,
            okTitle: 'OK',
            cancelTitle: 'Cancel',
            type: 'info',
        };

        wrapper.vm.show(title, content, options);

        const modal = wrapper.findComponent({ ref: 'modal' });

        expect(modal.exists()).toBe(true);
        expect(wrapper.vm.title).toBe('Modal Title');
        expect(wrapper.vm.content).toBe('<p>Modal Content</p>');
        expect(wrapper.vm.options.okOnly).toBe(false);
        expect(wrapper.vm.options.okTitle).toBe('OK');
        expect(wrapper.vm.options.cancelTitle).toBe('Cancel');
    });

    it('resets the modal state when the modal is hidden', () => {
        const title = 'Modal Title';
        const content = '<p>Modal Content</p>';
        const options = {
            okOnly: false,
            okTitle: 'OK',
            cancelTitle: 'Cancel',
            type: 'info',
        };

        wrapper.vm.show(title, content, options);
        wrapper.vm.reset();

        const modal = wrapper.findComponent({ ref: 'modal' });

        expect(wrapper.vm.title).toBe('');
        expect(wrapper.vm.content).toBe('');
        expect(wrapper.vm.options.okOnly).toBe(true);
        expect(wrapper.vm.options.okTitle).toBe('OK');
        expect(wrapper.vm.options.cancelTitle).toBe('Annuler');
    });
});
