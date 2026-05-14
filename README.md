# StudyAI - 基于大模型的个性化资源生成与学习多智能体系统

## 技术栈
- **后端**: Spring Boot 3.2.12 + MyBatis-Plus + MySQL + WebSocket
- **前端**: Vue3 + Element Plus + Vite
- **AI**: DeepSeek API + Qwen API
- **Java版本**: 17

## 快速启动

### 1. 数据库准备
```bash
mysql -u root -p < database/schema.sql
数据库的密码需要调整为自己的本地数据库密码！！！！！
编辑'src/main/resources/application.yml'第10行修改
```

### 2. 配置DeepSeek API Key
编辑 `src/main/resources/application.yml`:
```yaml
deepseek:
  api-key: sk-your-actual-api-key
```

### 3. 启动后端
```bash
./mvnw spring-boot:run
```

### 4. 启动前端
```bash
cd web
npm install
npm run dev
```

## 核心功能模块
1. **对话式学习画像构建** - 通过自然对话自动抽取8维学生画像
2. **多智能体协同资源生成** - 并行生成6种类型学习资源
3. **个性化学习路径规划** - AI规划从基础到进阶的完整路径
4. **智能辅导** - 基于画像的个性化答疑
5. **学习效果评估** - AI出题+智能分析+改进建议

## API接口
- `POST /api/profile/build` - 构建画像
- `POST /api/resource/generate` - 生成资源
- `POST /api/path/plan` - 规划路径
- `POST /api/tutor/chat` - 智能辅导
- `POST /api/assessment/questions/generate` - 生成题目
- `POST /api/assessment/submit` - 提交评估
- `WS /ws/chat` - WebSocket实时辅导
