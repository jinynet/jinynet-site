<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import type { VideoDetail } from '@/api/videos'
import { useTheme } from '@/composables/useTheme'

const props = defineProps<{
  video: VideoDetail
}>()

const { themeConfig } = useTheme()

const videoRef = ref<HTMLVideoElement | null>(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(80)
const isMuted = ref(false)
const playbackRate = ref(1)

const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const togglePlay = () => {
  if (videoRef.value) {
    if (isPlaying.value) {
      videoRef.value.pause()
    } else {
      videoRef.value.play()
    }
    isPlaying.value = !isPlaying.value
  }
}

const handleTimeUpdate = () => {
  if (videoRef.value) {
    currentTime.value = videoRef.value.currentTime
  }
}

const handleLoadedMetadata = () => {
  if (videoRef.value) {
    duration.value = videoRef.value.duration
  }
}

const handleEnded = () => {
  isPlaying.value = false
}

const seekTo = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (videoRef.value) {
    videoRef.value.currentTime = parseFloat(target.value)
    currentTime.value = videoRef.value.currentTime
  }
}

const toggleMute = () => {
  if (videoRef.value) {
    videoRef.value.muted = !videoRef.value.muted
    isMuted.value = videoRef.value.muted
  }
}

const changeVolume = (e: Event) => {
  const target = e.target as HTMLInputElement
  volume.value = parseFloat(target.value)
  if (videoRef.value) {
    videoRef.value.volume = volume.value / 100
  }
}

const changePlaybackRate = (rate: number) => {
  playbackRate.value = rate
  if (videoRef.value) {
    videoRef.value.playbackRate = rate
  }
}

const skip = (seconds: number) => {
  if (videoRef.value) {
    videoRef.value.currentTime = Math.max(0, Math.min(duration.value, videoRef.value.currentTime + seconds))
    currentTime.value = videoRef.value.currentTime
  }
}

watch(() => props.video, () => {
  if (videoRef.value) {
    videoRef.value.load()
  }
})

onMounted(() => {
  if (videoRef.value) {
    videoRef.value.volume = volume.value / 100
  }
})

onUnmounted(() => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
})
</script>

<template>
  <div class="video-player w-full bg-black rounded-lg overflow-hidden">
    <div class="relative aspect-video">
      <video
        ref="videoRef"
        class="w-full h-full"
        @timeupdate="handleTimeUpdate"
        @loadedmetadata="handleLoadedMetadata"
        @ended="handleEnded"
        @play="isPlaying = true"
        @pause="isPlaying = false"
        :poster="video.coverUrl || undefined"
      >
        <source v-if="video.hlsUrl" :src="video.hlsUrl" type="application/x-mpegURL" />
        <source v-else-if="video.sourceUrl" :src="video.sourceUrl" type="video/mp4" />
      </video>
      
      <!-- 播放遮罩 -->
      <div
        v-if="!isPlaying"
        class="absolute inset-0 bg-black/30 flex items-center justify-center cursor-pointer"
        @click="togglePlay"
      >
        <div class="w-20 h-20 rounded-full bg-white/90 flex items-center justify-center shadow-lg">
          <svg class="w-10 h-10 ml-1" fill="currentColor" viewBox="0 0 24 24" :style="{ color: themeConfig.primaryColor }">
            <path d="M8 5v14l11-7z"/>
          </svg>
        </div>
      </div>
      
      <!-- 进度条 -->
      <div class="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/80 to-transparent">
        <div class="mb-2">
          <input
            type="range"
            :value="currentTime"
            :max="duration"
            @input="seekTo"
            class="w-full h-1 bg-white/30 rounded-full appearance-none cursor-pointer"
            style="accent-color: #3b82f6;"
          />
        </div>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <button
              @click="togglePlay"
              class="w-10 h-10 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors"
            >
              <svg v-if="isPlaying" class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
                <path d="M6 4h4v16H6zm8 0h4v16h-4z"/>
              </svg>
              <svg v-else class="w-5 h-5 text-white ml-0.5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M8 5v14l11-7z"/>
              </svg>
            </button>
            <button @click="skip(-10)" class="text-white hover:text-gray-300">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 8V4l-8 4 8 4zm0 12v-4l8-4-8-4v4z"/>
              </svg>
            </button>
            <button @click="skip(10)" class="text-white hover:text-gray-300">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M4 4v4l8-4-8-4zm0 16v4l8-4-8-4z"/>
              </svg>
            </button>
            <span class="text-white text-sm ml-2">
              {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
            </span>
          </div>
          
          <div class="flex items-center gap-3">
            <div class="flex items-center gap-2">
              <button @click="toggleMute" class="text-white hover:text-gray-300">
                <svg v-if="isMuted" class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.42-3.58-8-8-8-4.42 0-8 3.58-8 8 0 1.5.41 2.91 1.15 4.15l1.51-1.51A6.996 6.996 0 015 12c0-3.87 3.13-7 7-7s7 3.13 7 7z"/>
                </svg>
                <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89 1.19 5 4.06 5 7.71s-2.11 6.52-5 7.71v2.06c4.01-1.23 7-4.91 7-9.77s-2.99-8.54-7-9.77z"/>
                </svg>
              </button>
              <input
                type="range"
                v-if="!isMuted"
                :value="volume"
                max="100"
                @input="changeVolume"
                class="w-20 h-1 bg-white/30 rounded-full appearance-none cursor-pointer"
                style="accent-color: white;"
              />
            </div>
            
            <select
              :value="playbackRate"
              @change="(e) => changePlaybackRate(parseFloat((e.target as HTMLSelectElement).value))"
              class="bg-white/20 text-white text-xs px-2 py-1 rounded outline-none cursor-pointer"
            >
              <option value="0.5">0.5x</option>
              <option value="0.75">0.75x</option>
              <option value="1">1x</option>
              <option value="1.25">1.25x</option>
              <option value="1.5">1.5x</option>
              <option value="2">2x</option>
            </select>
            
            <button class="text-white hover:text-gray-300">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M4 4h16v16H4z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
