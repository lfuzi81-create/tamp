import { request } from '@tamp/core'
import type { Result, PageResult, PaginateParams } from '@tamp/core'

/** 知识库文章 */
export interface KnowledgeArticle {
  id: number | string
  title: string
  categoryId: number | string
  categoryName?: string
  coverImage?: string
  summary?: string
  content?: string
  status?: number | string
  author?: string
  viewCount?: number
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 知识库分类 */
export interface KnowledgeCategory {
  id: number | string
  name: string
  sortOrder?: number
  [key: string]: any
}

/** 文章查询参数 */
export interface KnowledgeArticleQueryParams extends PaginateParams {
  categoryId?: number | string
}

/** 文章上架店铺映射 */
export type KnowledgeArticleShelfShops = Record<string, any>

/** 分页查询知识库文章 */
export function getArticles(params: KnowledgeArticleQueryParams) {
  return request.get<any, Result<PageResult<KnowledgeArticle>>>('/knowledge/articles', { params })
}

/** 获取文章详情 */
export function getArticleDetail(id: number | string) {
  return request.get<any, Result<KnowledgeArticle>>(`/knowledge/articles/${id}`)
}

/** 创建文章 */
export function createArticle(data: Partial<KnowledgeArticle>) {
  return request.post<any, Result<KnowledgeArticle>>('/knowledge/articles', data)
}

/** 更新文章 */
export function updateArticle(id: number | string, data: Partial<KnowledgeArticle>) {
  return request.put<any, Result<KnowledgeArticle>>(`/knowledge/articles/${id}`, data)
}

/** 删除文章 */
export function deleteArticle(id: number | string) {
  return request.delete<any, Result<void>>(`/knowledge/articles/${id}`)
}

/** 切换文章状态 */
export function updateArticleStatus(id: number | string, status: number | string) {
  return request.patch<any, Result<void>>(`/knowledge/articles/${id}/status`, { status })
}

/** 获取知识库分类列表 */
export function getKnowledgeCategories() {
  return request.get<any, Result<KnowledgeCategory[]>>('/knowledge/categories')
}

/** 创建知识库分类 */
export function createKnowledgeCategory(data: Partial<KnowledgeCategory>) {
  return request.post<any, Result<KnowledgeCategory>>('/knowledge/categories', data)
}

/** 更新知识库分类 */
export function updateKnowledgeCategory(id: number | string, data: Partial<KnowledgeCategory>) {
  return request.put<any, Result<KnowledgeCategory>>(`/knowledge/categories/${id}`, data)
}

/** 删除知识库分类 */
export function deleteKnowledgeCategory(id: number | string) {
  return request.delete<any, Result<void>>(`/knowledge/categories/${id}`)
}

/** 获取文章已上架店铺列表 */
export function getArticleShelfShops(articleId: number | string) {
  return request.get<any, Result<KnowledgeArticleShelfShops>>(`/knowledge/articles/${articleId}/shelf-shops`)
}
