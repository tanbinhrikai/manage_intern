import http from './http'

/**
 * Send a chat message to the AI agent
 * @param {string} message - The message to send
 * @returns {Promise} - API response with ChatResponse
 */
export const sendMessage = (message) => {
    return http.post('/agent/chat', { message })
}
