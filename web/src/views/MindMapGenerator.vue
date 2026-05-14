<template>
  <AgentChat
    title="知识框架思维导图生成师"
    subtitle="输入主题或文档，AI将生成结构化的思维导图（Markdown/Mermaid格式）"
    placeholder="请输入主题或需求，例如：请为' Java面向对象编程'生成一份思维导图，包括封装、继承、多态、抽象类等核心概念..."
    agent-type="mindmap"
  />
</template>

<script setup>
import AgentChat from '@/components/AgentChat.vue'
</script>
