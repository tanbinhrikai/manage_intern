import http from './http'

export const login = (email, password) => {
    return http.post('/auth/login', { email, password })
}


export const refreshToken = () => {
    return http.post('/auth/refresh')
}

export const logout = () => {
    return http.post('/auth/logout')
}
