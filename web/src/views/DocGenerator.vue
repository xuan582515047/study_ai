<template>
  <AgentChat
    title="知识点深度讲解文档生成师"
    subtitle="输入需要讲解的知识点，AI将生成背景、原理、推导、应用、误区等深度文档"
    placeholder="请输入需要深度讲解的知识点，例如：请为我详细讲解微积分中的拉格朗日中值定理，包括几何意义和证明过程..."
    agent-type="doc"
  />
</template>

<script setup>
import AgentChat from '@/components/AgentChat.vue'
</script>
