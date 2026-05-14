<template>
  <AgentChat
    title="PPT智能生成师"
    subtitle="输入主题或需求，AI将生成完整的PPT大纲和每页内容建议"
    placeholder="请输入PPT主题或需求，例如：帮我生成一份关于'计算机网络OSI七层模型'的课程PPT，面向大二学生..."
    agent-type="ppt"
  />
</template>

<script setup>
import AgentChat from '@/components/AgentChat.vue'
</script>
