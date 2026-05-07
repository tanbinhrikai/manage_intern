import { ref } from 'vue'

export const useRoadmapStore = () => {
  const sessionId = ref(null)
  const chatHistory = ref([])
  const routeTree = ref(null)
  const loading = ref(false)
  const error = ref(null)
  return { sessionId, chatHistory, routeTree, loading, error }
}