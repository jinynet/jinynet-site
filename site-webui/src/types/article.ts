/**
 * 文章相关类型定义
 */

export type ArticleStatus = 'draft' | 'published' | 'private'

/**
 * 文章分类
 */
export interface ArticleCategory {
  id: number
  name: string
  slug: string
  description: string | null
  sortOrder: number
}

/**
 * 文章标签
 */
export interface ArticleTag {
  id: number
  name: string
  slug: string
  color: string | null
  description: string | null
  sortOrder: number
}

/**
 * 文章列表项 - 管理端视图 (后端 ArticleList DTO)
 */
export interface ArticleListItem {
  id: number
  title: string
  slug: string
  excerpt: string | null
  coverImage: string | null
  status: ArticleStatus
  viewCount: number
  likeCount: number
  publishedAt: string | null
  updatedAt: string
}

/**
 * 已发布文章列表项 - 前端视图
 * 后端返回扁平的 categoryName / tags 字符串数组
 */
export interface PostedArticleListItem extends ArticleListItem {
  categoryName?: string
  tags?: string[]
}

/**
 * 文章详情 - 管理端视图 (后端 ArticleDetail DTO)
 */
export interface ArticleDetail extends ArticleListItem {
  content: string
  category?: ArticleCategory | null
  tags?: ArticleTag[]
}

/**
 * 文章详情 - 前端视图
 * 继承 ArticleDetail（拥有嵌套 category/tags），额外补充扁平的 categoryName
 */
export interface PostedArticleDetail extends ArticleDetail {
  categoryName?: string
}

/**
 * 前端列表/卡片展示用的文章对象
 * 兼容嵌套 category 对象与扁平 categoryName 两种格式
 */
export interface ArticleCardItem {
  id: number
  title: string
  slug?: string
  excerpt: string | null
  coverImage: string | null
  status?: ArticleStatus
  viewCount: number
  likeCount: number
  publishedAt: string | null
  updatedAt?: string
  categoryName?: string
  category?: { id?: number; name: string } | null
  tags?: string[] | ArticleTag[]
}

/**
 * 分类输入
 */
export interface CategoryInput {
  id?: number
  name: string
  slug: string
  description: string | null
  sortOrder: number | 0
}

/**
 * 标签输入
 */
export interface TagInput {
  id?: number
  name: string
  slug: string
  description: string | null
  sortOrder: number | 0
}

/**
 * 文章表单 (后端 ArticleForm DTO)
 */
export interface ArticleForm {
  title: string
  slug: string
  content: string
  excerpt: string | null
  coverImage: string | null
  status: ArticleStatus
  category?: CategoryInput | null
  tags?: TagInput[]
}

/**
 * 文章查询条件 (后端 ArticleSpecification DTO)
 */
export interface ArticleQuery {
  pageIndex: number
  pageSize: number
  title?: string
  excerpt?: string
  content?: string
  status?: string
  publishedAtStart?: string
  publishedAtEnd?: string
  orderBy?: string
}

/**
 * 已发布文章查询参数
 */
export interface PostedArticleQuery {
  pageIndex: number
  pageSize: number
  title?: string
  categoryId?: number
  tagId?: number
}
