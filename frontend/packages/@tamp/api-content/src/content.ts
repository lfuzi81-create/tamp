import { request } from '@tamp/core'
import type { Result, PageResult, PaginateParams } from '@tamp/core'

/** 内容 VO */
export interface ContentVO {
  id: number | string
  title: string
  categoryId: number | string
  categoryName?: string
  coverImage?: string
  summary?: string
  detail?: string
  status?: number | string
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 内容分类 */
export interface ContentCategory {
  id: number | string
  name: string
  sortOrder?: number
  [key: string]: any
}

/** 内容查询参数 */
export interface ContentQueryParams extends PaginateParams {
  categoryId?: number | string
}

/** 内容上架店铺映射 */
export type ContentShelfShops = Record<string, any>

/** 分页查询内容 */
export function getContents(params: ContentQueryParams) {
  return request.get<any, Result<PageResult<ContentVO>>>('/contents', { params })
}

/** 获取内容详情 */
export function getContentDetail(id: number | string) {
  return request.get<any, Result<ContentVO>>(`/contents/${id}`)
}

/** 创建内容 */
export function createContent(data: Partial<ContentVO>) {
  return request.post<any, Result<ContentVO>>('/contents', data)
}

/** 更新内容 */
export function updateContent(id: number | string, data: Partial<ContentVO>) {
  return request.put<any, Result<ContentVO>>(`/contents/${id}`, data)
}

/** 删除内容 */
export function deleteContent(id: number | string) {
  return request.delete<any, Result<void>>(`/contents/${id}`)
}

/** 切换内容状态 */
export function updateContentStatus(id: number | string, status: number | string) {
  return request.patch<any, Result<void>>(`/contents/${id}/status`, { status })
}

/** 获取内容分类列表 */
export function getContentCategories() {
  return request.get<any, Result<ContentCategory[]>>('/contents/categories')
}

/** 创建内容分类 */
export function createContentCategory(data: Partial<ContentCategory>) {
  return request.post<any, Result<ContentCategory>>('/contents/categories', data)
}

/** 更新内容分类 */
export function updateContentCategory(id: number | string, data: Partial<ContentCategory>) {
  return request.put<any, Result<ContentCategory>>(`/contents/categories/${id}`, data)
}

/** 删除内容分类 */
export function deleteContentCategory(id: number | string) {
  return request.delete<any, Result<void>>(`/contents/categories/${id}`)
}

/** 获取推荐内容列表 */
export function getRecommendContents() {
  return request.get<any, Result<ContentVO[]>>('/contents/recommend')
}

/** 获取内容已上架店铺列表 */
export function getContentShelfShops(contentId: number | string) {
  return request.get<any, Result<ContentShelfShops>>(`/contents/${contentId}/shelf-shops`)
}
