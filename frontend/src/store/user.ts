import { defineStore } from 'pinia';
import { userService } from '@/api';
import type { UserVO } from '@/types';

interface LoginData {
    userAccount: string;
    userPassword: string;
}

export const useUserStore = defineStore('user', {
    state: () => ({
        currentUser: null as UserVO | null,
    }),
    getters: {
        isLoggedIn: (state) => !!state.currentUser,
    },
    actions: {
        async fetchUser() {
            try {
                const user = await userService.getLoginUser();
                this.currentUser = user;
                return user;
            } catch (error) {
                this.currentUser = null;
                throw error;
            }
        },
        async login(loginData: LoginData) {
            try {
                const user = await userService.login(loginData);
                this.currentUser = user;
                return user;
            } catch (error) {
                this.currentUser = null;
                throw error;
            }
        },
        async logout() {
            try {
                await userService.logout();
                this.currentUser = null;
            } catch (error) {
                console.error("无法退出");
            }
        },
        setUser(user: UserVO | null) {
            this.currentUser = user;
        },
    },
});
