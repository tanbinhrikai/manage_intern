<script setup>
</script>

<template>
  <div class="relative min-h-svh w-full">
    <div
      class="pointer-events-none absolute inset-0 z-0"
      style="
        background-image:
          linear-gradient(to right, var(--auth-grid-color) 1px, transparent 1px),
          linear-gradient(to bottom, var(--auth-grid-color) 1px, transparent 1px),
          radial-gradient(125% 125% at 50% 10%, var(--auth-gradient-start) 40%, var(--auth-gradient-end) 100%);
        background-size:
          64px 64px,
          64px 64px,
          100% 100%;
      "
    />
    <div class="relative z-10 flex min-h-svh">
      <div class="flex min-h-svh w-full items-center justify-center p-6 md:p-10">
        <div class="w-full max-w-sm">
          <slot />
        </div>
      </div>
    </div>
  </div>
</template>
