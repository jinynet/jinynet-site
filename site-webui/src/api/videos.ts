import axios from '@/api/axios'

export interface VideoDetail {
  id: number; title: string; description?: string; coverUrl?: string
  videoUrl?: string; hlsUrl?: string; sourceUrl?: string
  duration?: number; views?: number; viewCount?: number; likeCount?: number
  categoryName?: string; category?: { name: string }; status?: string; tags?: string[]
  publishedAt?: string; createdAt?: string; updatedAt?: string
}

export interface VideoList {
  id: number; title: string; description?: string; coverUrl?: string
  videoUrl?: string; duration?: number; views?: number; viewCount?: number
  categoryName?: string; status?: string
  publishedAt?: string; createdAt?: string; updatedAt?: string
}

export interface VideoCategory {
  id: number; name: string; code?: string; sortOrder?: number
}

export interface VideoQuery {
  pageIndex?: number; pageSize?: number
  categoryId?: number; title?: string
}

export const getPublicVideos = (params: VideoQuery) =>
  axios.get('/videos/public', { params })

export const getVideoCategories = () =>
  axios.get('/videos/categories/public')

export const getPublicVideoById = (id: number | string) =>
  axios.get(`/videos/public/${id}`)

export const getHotVideos = (limit = 6) =>
  axios.get('/videos/public/hot', { params: { limit } })
