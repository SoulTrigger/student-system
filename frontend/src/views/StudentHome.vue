<template>
  <div>
    <h1>Student Dashboard</h1>
    <p>Welcome, {{ auth.name || auth.userId }}</p>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
</script>
