<template>
  <n-card title="基本信息">
    <n-form :model="infoForm" label-placement="top">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="infoForm.name" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="infoForm.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="职位" path="title">
          <n-input v-model:value="infoForm.title" placeholder="请输入职位" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="infoForm.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="电话" path="phone">
          <n-input v-model:value="infoForm.phone" placeholder="请输入电话号码" />
        </n-form-item>
        <n-form-item label="位置" path="location">
          <n-input v-model:value="infoForm.location" placeholder="请输入所在城市" />
        </n-form-item>
      </div>
      <n-form-item label="头像URL" path="avatar">
        <div class="flex items-center gap-4">
          <div v-if="infoForm.avatar" class="w-16 h-16 rounded-full overflow-hidden border">
            <img :src="infoForm.avatar" class="w-full h-full object-cover" />
          </div>
          <n-input v-model:value="infoForm.avatar" placeholder="请输入头像图片URL" class="flex-1" />
        </div>
      </n-form-item>
      <n-form-item label="个人简介" path="summary">
        <n-input v-model:value="infoForm.summary" placeholder="请输入个人简介（简短）" :rows="8" type="textarea" />
      </n-form-item>
      <n-form-item label="详细介绍" path="bio">
        <n-input v-model:value="infoForm.bio" placeholder="请输入详细介绍（Markdown格式）" :rows="6" type="textarea" />
      </n-form-item>
      <n-form-item>
        <n-button type="primary" @click="saveInfo" :loading="infoSaving">保存基本信息</n-button>
      </n-form-item>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, useMessage } from 'naive-ui'
import { getUserInfo, updateUserInfo, type UserInfo } from '@/api/profile'

const message = useMessage()

const infoSaving = ref(false)

const infoForm = reactive({
  name: '',
  nickname: '',
  title: '',
  email: '',
  phone: '',
  location: '',
  avatar: '',
  summary: '',
  bio: ''
})

const saveInfo = async () => {
  infoSaving.value = true
  try {
    await updateUserInfo(infoForm)
    message.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    infoSaving.value = false
  }
}

const fetchInfo = async () => {
  try {
    const response = await getUserInfo()
    if (response.data) {
      const data = response.data as UserInfo
      infoForm.name = data.name || ''
      infoForm.nickname = data.nickname || ''
      infoForm.title = data.title || ''
      infoForm.email = data.email || ''
      infoForm.phone = data.phone || ''
      infoForm.location = data.location || ''
      infoForm.avatar = data.avatar || ''
      infoForm.summary = data.summary || ''
      infoForm.bio = data.bio || ''
    }
  } catch (error) {
    console.error('获取信息失败:', error)
  }
}

onMounted(() => {
  fetchInfo()
})
</script>
