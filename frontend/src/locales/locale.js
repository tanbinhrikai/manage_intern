import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import en from '@/constants/en.json'
import jp from '@/constants/jp.json'

const messages = { en, jp }

export const useLocaleStore = defineStore('locale', () => {
    const currentLocale = ref(localStorage.getItem('locale') || 'en')

    const t = computed(() => {
        return (key) => {
            const keys = key.split('.')
            let value = messages[currentLocale.value]

            for (const k of keys) {
                if (value && typeof value === 'object' && k in value) {
                    value = value[k]
                } else {
                    return key
                }
            }

            return value || key
        }
    })

    const setLocale = (locale) => {
        if (messages[locale]) {
            currentLocale.value = locale
            localStorage.setItem('locale', locale)
        }
    }

    const availableLocales = computed(() => Object.keys(messages))

    return {
        currentLocale,
        t,
        setLocale,
        availableLocales
    }
})
