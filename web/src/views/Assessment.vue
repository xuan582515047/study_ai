<template>
  <div class="assessment">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习效果评估</span>
          <el-button type="primary" @click="genDialogVisible = true">生成测验</el-button>
        </div>
      </template>

      <el-dialog v-model="genDialogVisible" title="生成测验题目" width="400px">
        <el-form :model="genForm" label-width="100px">
          <el-form-item label="学科">
            <el-input v-model="genForm.subject" placeholder="如：计算机科学" />
          </el-form-item>
          <el-form-item label="知识点">
            <el-input v-model="genForm.knowledgePoint" placeholder="如：二叉树遍历" />
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="genForm.difficulty">
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="genForm.count" :min="1" :max="10" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="genDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="generateQuestions" :loading="genLoading">生成</el-button>
        </template>
      </el-dialog>

      <div v-if="questions.length > 0">
        <h3 style="margin-bottom: 20px;">测验题目</h3>
        <div v-for="(q, idx) in questions" :key="q.id" class="question-item">
          <div class="question-title">{{ idx + 1 }}. [{{ q.difficulty }}] {{ q.questionText }}</div>
          <div v-if="q.options" class="question-options">
            <el-radio-group v-if="q.questionType === 'single_choice'" v-model="answers[idx]">
              <el-radio v-for="(opt, key) in JSON.parse(q.options)" :key="key" :label="key">{{ key }}. {{ opt }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else-if="q.questionType === 'multiple_choice'" v-model="answers[idx]">
              <el-checkbox v-for="(opt, key) in JSON.parse(q.options)" :key="key" :label="key">{{ key }}. {{ opt }}</el-checkbox>
            </el-checkbox-group>
          </div>
          <el-input v-else v-model="answers[idx]" placeholder="请输入答案" />
        </div>
        <el-button type="primary" @click="submit" :loading="submitLoading" style="margin-top: 20px;">提交评估</el-button>
      </div>

      <el-empty v-else description="暂无测验题目，请先生成" />
    </el-card>

    <el-card v-if="result" style="margin-top: 20px;">
      <template #header><span>评估结果</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="总分">{{ result.totalScore }} / {{ result.maxScore }}</el-descriptions-item>
        <el-descriptions-item label="得分率">
          <el-progress :percentage="result.scorePercent" :status="result.scorePercent >= 60 ? 'success' : 'exception'" />
        </el-descriptions-item>
        <el-descriptions-item label="掌握程度">
          <el-tag :type="masteryColor(result.masteryLevel)">{{ masteryMap[result.masteryLevel] || result.masteryLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="薄弱领域">{{ result.weakAreas }}</el-descriptions-item>
        <el-descriptions-item label="优势领域">{{ result.strongAreas }}</el-descriptions-item>
        <el-descriptions-item label="AI分析">{{ result.aiAnalysis }}</el-descriptions-item>
        <el-descriptions-item label="改进建议" :span="2">{{ result.improvementSuggestions }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateQuestions as apiGenerateQuestions, submitAssessment } from '../api/assessment'

const studentId = 1
const genDialogVisible = ref(false)
const genForm = ref({ subject: '', knowledgePoint: '', difficulty: 'medium', count: 5 })
const questions = ref([])
const answers = ref([])
const result = ref(null)
const genLoading = ref(false)
const submitLoading = ref(false)

const masteryMap = { beginner: '入门', intermediate: '中级', advanced: '高级', expert: '专家' }
const masteryColor = (level) => {
  const map = { beginner: 'danger', intermediate: 'warning', advanced: 'success', expert: '' }
  return map[level] || ''
}

const generateQuestions = async () => {
  if (!genForm.value.knowledgePoint) {
    ElMessage.warning('请输入知识点')
    return
  }
  genLoading.value = true
  try {
    const res = await apiGenerateQuestions(genForm.value)
    if (res.code === 200) {
      questions.value = res.data
      answers.value = questions.value.map(() => '')
      ElMessage.success('题目生成成功')
      genDialogVisible.value = false
    }
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    genLoading.value = false
  }
}

const submit = async () => {
  submitLoading.value = true
  try {
    const answerList = questions.value.map((q, idx) => ({
      questionId: q.id,
      answer: Array.isArray(answers.value[idx]) ? answers.value[idx].join(',') : answers.value[idx],
      timeSpentSeconds: 60
    }))
    const res = await submitAssessment({
      studentId,
      assessmentType: 'diagnostic',
      subject: genForm.value.subject,
      questionIds: questions.value.map(q => q.id),
      answers: answerList
    })
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('评估完成')
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.question-item { margin-bottom: 20px; padding: 15px; background: #f5f7fa; border-radius: 8px; }
.question-title { font-weight: bold; margin-bottom: 10px; }
.question-options { padding-left: 20px; }
</style>
