<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex items-center gap-3">
      <n-button type="primary" @click="openContactModal">
        <Plus class="w-4 h-4 mr-2" />
        添加联系方式
      </n-button>
      <n-button @click="fetchContacts" circle size="small" title="刷新">
        <Refresh class="w-4 h-4" />
      </n-button>
      </div>
      <n-button text size="small" :title="viewMode === 'table' ? '切换到卡片视图' : '切换到表格视图'" @click="toggleViewMode">
        <Grid class="w-4 h-4" v-if="viewMode === 'table'" /><Menu class="w-4 h-4" v-else />
      </n-button>
    </div>
    <n-card class="table-card" v-if="viewMode === 'table'">
      <n-data-table
        :columns="contactColumns"
        :data="contacts"
        :bordered="true"
        :loading="contactsLoading"
        :scroll-x="700"
      >
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <p class="text-gray-400">暂无联系方式</p>
            <n-button type="primary" size="small" @click="openContactModal" class="mt-4">
              <Plus class="w-4 h-4 mr-2" />
              添加一个联系方式
            </n-button>
          </div>
        </template>
      </n-data-table>
    </n-card>
    <n-card class="table-card" v-if="viewMode === 'card'">
      <n-spin :show="contactsLoading">
        <div v-if="contacts.length === 0" class="flex flex-col items-center justify-center py-12"><p class="text-gray-400">暂无联系方式</p></div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <n-card v-for="c in contacts" :key="c.id" size="small" class="hover:shadow-md transition-shadow">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs font-medium px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-700">{{ getContactTypeLabel(c.contactType) }}</span>
              <span class="text-xs text-gray-400 truncate">{{ c.contactValue }}</span>
            </div>
            <p class="text-xs text-gray-400" v-if="c.displayName">显示名称: {{ c.displayName }}</p>
            <div class="flex justify-end gap-3 mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
              <n-button text size="small" @click.stop="editContact(c)"><Edit class="w-4 h-4" /></n-button>
              <n-button text size="small" type="error" @click.stop="deleteContactItem(c)"><Trash2 class="w-4 h-4" /></n-button>
            </div>
          </n-card>
        </div>
      </n-spin>
    </n-card>
    <n-modal
      v-model:show="showContactModal"
      preset="card"
      :title="editingContact ? '编辑联系方式' : '添加联系方式'"
      :style="{ width: '500px' }"
    >
      <n-form :model="contactForm" label-placement="top">
        <n-form-item label="联系方式类型" path="contactType">
          <n-select v-model:value="contactForm.contactType" :options="contactTypeOptions" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="联系方式值" path="contactValue">
          <n-input v-model:value="contactForm.contactValue" placeholder="请输入联系方式值（邮箱、链接等）" />
        </n-form-item>
        <n-form-item label="显示名称" path="displayName">
          <n-input v-model:value="contactForm.displayName" placeholder="请输入显示名称（可选）" />
        </n-form-item>
        <n-form-item label="图标" path="icon">
          <n-input v-model:value="contactForm.icon" placeholder="请输入图标名称（可选）" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="contactForm.sortOrder" :min="0" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showContactModal = false">取消</n-button>
          <n-button type="primary" @click="saveContact">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="deleteContactModal"
      preset="card"
      title="确认删除"
      :style="{ width: '400px' }"
    >
      <p>确定要删除联系方式「{{ deletingContactName }}」吗？</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="deleteContactModal = false">取消</n-button>
          <n-button type="error" @click="confirmDeleteContact">确定删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { Plus, Refresh, Grid, Menu, Edit, Trash2 } from '@/icons'
import { NButton, NCard, NDataTable, NModal, NSpace, NForm, NFormItem, NInput, NSelect, NInputNumber, NSpin, useMessage } from 'naive-ui'
import { getContacts, createContact, updateContact, deleteContact, type UserContact } from '@/api/profile'
import { useViewMode } from '@/composables/useViewMode'

const message = useMessage()
const { viewMode, toggleViewMode } = useViewMode('admin-profile-contacts-view')

const getContactTypeLabel = (type: string) => {
  const found = contactTypeOptions.find(t => t.value === type)
  return found?.label || type
}

const contacts = ref<UserContact[]>([])
const contactsLoading = ref(false)
const showContactModal = ref(false)
const editingContact = ref<UserContact | null>(null)
const deleteContactModal = ref(false)
const deletingContactId = ref<number | null>(null)
const deletingContactName = ref('')

const contactForm = ref({
  contactType: 'email' as 'email' | 'phone' | 'github' | 'linkedin' | 'wechat' | 'website' | 'other',
  contactValue: '',
  displayName: '',
  icon: '',
  sortOrder: 0
})

const contactTypeOptions = [
  { label: '邮箱', value: 'email' },
  { label: 'GitHub', value: 'github' },
  { label: 'LinkedIn', value: 'linkedin' },
  { label: '微信', value: 'wechat' },
  { label: 'QQ', value: 'qq' },
  { label: '个人网站', value: 'website' },
  { label: '其他', value: 'other' }
]

const contactColumns = [
  {
    title: '类型',
    key: 'contactType',
    width: 100,
    render: (row: UserContact) => {
      const type = contactTypeOptions.find(t => t.value === row.contactType)
      return type?.label || row.contactType
    }
  },
  { title: '联系方式', key: 'contactValue', ellipsis: true, minWidth: 200 },
  { title: '显示名称', key: 'displayName', minWidth: 100 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right' as 'right',
    render: (row: UserContact) => {
      return h('div', { class: 'flex gap-1' }, [
        h(NButton, { text: true, size: 'tiny' as const, onClick: () => editContact(row) }, () => '编辑'),
        h(NButton, { text: true, size: 'tiny' as const, status: 'error' as const, onClick: () => deleteContactItem(row) }, () => '删除')
      ])
    }
  }
]

const openContactModal = () => {
  editingContact.value = null
  contactForm.value = { contactType: 'email', contactValue: '', displayName: '', icon: '', sortOrder: 0 }
  showContactModal.value = true
}

const editContact = (contact: UserContact) => {
  editingContact.value = contact
  contactForm.value = {
    contactType: contact.contactType,
    contactValue: contact.contactValue,
    displayName: contact.displayName || '',
    icon: contact.icon || '',
    sortOrder: contact.sortOrder
  }
  showContactModal.value = true
}

const deleteContactItem = (contact: UserContact) => {
  deletingContactId.value = contact.id
  deletingContactName.value = contact.displayName || contact.contactValue
  deleteContactModal.value = true
}

const confirmDeleteContact = async () => {
  if (deletingContactId.value) {
    await deleteContact(deletingContactId.value)
    contacts.value = contacts.value.filter(c => c.id !== deletingContactId.value)
    message.success('删除成功')
  }
  deleteContactModal.value = false
}

const saveContact = async () => {
  if (!contactForm.value.contactType || !contactForm.value.contactValue.trim()) {
    message.error('请填写联系方式类型和值')
    return
  }

  try {
    if (editingContact.value) {
      await updateContact({ id: editingContact.value.id, ...contactForm.value })
      const index = contacts.value.findIndex(c => c.id === editingContact.value!.id)
      if (index !== -1) {
        contacts.value[index] = { ...contacts.value[index], ...contactForm.value }
      }
      message.success('更新成功')
    } else {
      const response = await createContact(contactForm.value)
      contacts.value.push(response.data)
      message.success('创建成功')
    }
    showContactModal.value = false
  } catch (error) {
    console.error('保存联系方式失败:', error)
    message.error('保存失败')
  }
}

const fetchContacts = async () => {
  contactsLoading.value = true
  try {
    const response = await getContacts()
    if (response.data) {
      contacts.value = response.data
    }
  } catch (error) {
    console.error('获取联系方式失败:', error)
  } finally {
    contactsLoading.value = false
  }
}

onMounted(() => {
  fetchContacts()
})
</script>
