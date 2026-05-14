<template>
  <AgentChat
    title="多模态问答导师"
    subtitle="输入你的问题，AI将提供结构化解答、图解建议和智能追问"
    placeholder="请输入你想问的问题，例如：请帮我讲解一下动态规划中的背包问题，我不太理解状态转移方程..."
    agent-type="tutor"
  />
</template>

<script setup>
import AgentChat from '@/components/AgentChat.vue'
</script>
