<template>
  <AgentChat
    title="同类考题智能生成师"
    subtitle="输入原题或知识点，AI将分析题型并生成同类练习题及详细解析"
    placeholder="请输入原题或需求，例如：请根据这道题生成同类练习：已知函数f(x)=x^2+2x+1，求其在x=1处的导数。要求变换数据生成3道类似题目..."
    agent-type="quiz"
  />
</template>

<script setup>
import AgentChat from '@/components/AgentChat.vue'
</script>
