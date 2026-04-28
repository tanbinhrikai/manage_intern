# Composables

Tập hợp các composables tái sử dụng cho Vue 3 Composition API.

## usePagination

Quản lý trạng thái pagination.

### Usage

```javascript
import { usePagination } from '@/composables'

const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 10,
  onPageChange: () => fetchData() // Optional callback
})

// Access state
pagination.currentPage.value
pagination.pageSize.value
pagination.totalItems.value

// Use computed values
pagination.totalPages.value
pagination.hasNextPage.value
pagination.hasPrevPage.value
pagination.apiParams.value // { page: 0, limit: 10 } for backend

// Methods
pagination.setPage(2)
pagination.nextPage()
pagination.prevPage()
pagination.setTotalItems(100)
pagination.setPageSize(20)
pagination.reset()
```

## useLoading

Quản lý trạng thái loading.

### Usage

```javascript
import { useLoading } from '@/composables'

const { loading, withLoading, isLoading } = useLoading({
  initialValue: false
})

// Simple loading
loading.value = true
await fetchData()
loading.value = false

// With wrapper
await withLoading(async () => {
  await fetchData()
})

// Multiple loading states
setLoadingState('fetching', true)
setLoadingState('saving', true)
isLoading('fetching') // true
```

## useApi

Wrapper cho API calls với error handling tự động.

### Usage

```javascript
import { useApi } from '@/composables'

const { execute, executeAndExtract } = useApi({
  showSuccessMessage: true,
  showErrorMessage: true,
  onSuccess: (response) => console.log('Success', response),
  onError: (error, message) => console.error('Error', message)
})

// Basic usage
try {
  const response = await execute(() => getUsers())
  // response.data...
} catch (error) {
  // Error already handled by useApi
}

// With success message
await execute(
  () => createUser(data),
  'userManagement.messages.createSuccess', // i18n key
  true // Show error if fails
)

// Extract data directly
const users = await executeAndExtract(
  () => getUsers(),
  'data.data.items', // Path to data
  [] // Default value
)
```

## useForm

Quản lý form state và validation.

### Usage

```javascript
import { useForm } from '@/composables'

const form = useForm({
  initialValues: {
    name: '',
    email: ''
  },
  rules: {
    name: [{ required: true, message: 'Name is required' }],
    email: [{ required: true, type: 'email' }]
  },
  onSubmit: async (payload) => {
    return await createUser(payload)
  }
})

// Access form data
form.formData.name
form.formData.email

// Set values
form.setFieldValue('name', 'John')
form.setFieldsValue({ name: 'John', email: 'john@example.com' })

// Validation
const isValid = await form.validate()
await form.validateField('email')

// Submit
try {
  const result = await form.submit()
  // Success
} catch (error) {
  // Error
}

// Reset
form.resetFields()
form.resetFieldsTo({ name: '', email: '' })
```

## Examples

### Complete List View Example

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { usePagination, useLoading, useApi } from '@/composables'
import { getUsers } from '@/api/user'

const pagination = usePagination({
  initialPage: 1,
  initialPageSize: 10,
  onPageChange: () => fetchUsers()
})

const { loading, withLoading } = useLoading()
const { execute } = useApi()

const users = ref([])

async function fetchUsers() {
  await withLoading(async () => {
    const res = await execute(() => getUsers(pagination.apiParams.value))
    users.value = res.data?.data?.items || []
    pagination.setTotalItems(res.data?.data?.totalItems || 0)
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <el-table :data="users" v-loading="loading">
    <!-- table columns -->
  </el-table>
  
  <el-pagination
    :current-page="pagination.currentPage.value"
    :page-size="pagination.pageSize.value"
    :total="pagination.totalItems.value"
    @current-change="pagination.setPage"
  />
</template>
```
