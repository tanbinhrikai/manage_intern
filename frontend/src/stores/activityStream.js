import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ACTIVITY_STREAM_URL } from '@/api/dashboard'

export const useActivityStreamStore = defineStore('activityStream', () => {
  const activities = ref([])
  const eventSource = ref(null)
  let closeTimeout = null

  const setActivities = (list) => {
    activities.value = list
  }

  const addActivity = (activity) => {
    // Avoid duplicate activities in the list
    const exists = activities.value.some(
      (a) => a.timestamp === activity.timestamp && a.text === activity.text
    )
    if (!exists) {
      activities.value = [activity, ...activities.value]
    }
  }

  const initConnection = () => {
    if (eventSource.value) {
      return
    }

    console.log("Establishing global SSE connection...")
    const es = new EventSource(ACTIVITY_STREAM_URL, { withCredentials: true })

    es.addEventListener("activity", (event) => {
      try {
        const newActivity = JSON.parse(event.data)
        addActivity(newActivity)
      } catch (err) {
        console.error("Failed to parse activity stream event in global store:", err)
      }
    })

    es.onerror = (err) => {
      console.error("Global activity stream connection error:", err)
    };

    eventSource.value = es
  }

  const closeConnection = () => {
    if (eventSource.value) {
      console.log("Closing global SSE connection...")
      eventSource.value.close()
      eventSource.value = null
    }
    if (closeTimeout) {
      clearTimeout(closeTimeout)
      closeTimeout = null
    }
  }

  const registerLayout = () => {
    if (closeTimeout) {
      clearTimeout(closeTimeout)
      closeTimeout = null
    }
    initConnection()
  }

  const unregisterLayout = () => {
    if (closeTimeout) {
      clearTimeout(closeTimeout)
    }
    closeTimeout = setTimeout(() => {
      closeConnection()
    }, 200) // Small delay to allow the next layout to mount and clear timeout
  }

  return {
    activities,
    setActivities,
    addActivity,
    initConnection,
    closeConnection,
    registerLayout,
    unregisterLayout
  }
})
