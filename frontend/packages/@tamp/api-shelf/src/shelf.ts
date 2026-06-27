import { request } from '@tamp/core'
import type { Result } from '@tamp/core'

/** 货架产品 VO */
export interface ShelfProductVO {
  id: number | string
  shopId: number | string
  productId: number | string
  productTitle?: string
  productCoverImage?: string
  tags?: string[]
  isRecommend?: boolean
  sortOrder?: number
  [key: string]: any
}

/** 货架内容 VO */
export interface ShelfContentVO {
  id: number | string
  shopId: number | string
  contentId: number | string
  contentTitle?: string
  contentCoverImage?: string
  tags?: string[]
  isRecommend?: boolean
  sortOrder?: number
  [key: string]: any
}

/** 加入货架产品请求体 */
export interface ShelfProductBody {
  shopId: number | string
  productId: number | string
  tags?: string[]
}

/** 加入货架内容请求体 */
export interface ShelfContentBody {
  shopId: number | string
  contentId: number | string
  tags?: string[]
}

/** 货架排序请求体 */
export interface ShelfOrderBody {
  shopId: number | string
  productIds?: (number | string)[]
  contentIds?: (number | string)[]
}

/** 货架标签更新请求体 */
export interface ShelfTagsBody {
  tags: string[]
}

/** 获取店铺货架产品列表 */
export function getShelfProducts(shopId: number | string) {
  return request.get<any, Result<ShelfProductVO[]>>('/shelf/products', { params: { shopId } })
}

/** 加入产品到货架 */
export function addProductToShelf(data: ShelfProductBody) {
  return request.post<any, Result<void>>('/shelf/products', data)
}

/** 更新货架产品排序 */
export function updateShelfProductOrder(data: ShelfOrderBody) {
  return request.put<any, Result<void>>('/shelf/products/order', data)
}

/** 切换货架产品推荐 */
export function updateShelfProductRecommend(id: number | string, shopId: number | string) {
  return request.put<any, Result<void>>(`/shelf/products/${id}/recommend`, null, { params: { shopId } })
}

/** 更新货架产品标签 */
export function updateShelfProductTags(id: number | string, shopId: number | string, data: ShelfTagsBody) {
  return request.put<any, Result<void>>(`/shelf/products/${id}/tags`, data, { params: { shopId } })
}

/** 从货架移除产品 */
export function removeProductFromShelf(id: number | string, shopId: number | string) {
  return request.delete<any, Result<void>>(`/shelf/products/${id}`, { params: { shopId } })
}

/** 获取店铺货架内容列表 */
export function getShelfContents(shopId: number | string) {
  return request.get<any, Result<ShelfContentVO[]>>('/shelf/contents', { params: { shopId } })
}

/** 加入内容到货架 */
export function addContentToShelf(data: ShelfContentBody) {
  return request.post<any, Result<void>>('/shelf/contents', data)
}

/** 更新货架内容排序 */
export function updateShelfContentOrder(data: ShelfOrderBody) {
  return request.put<any, Result<void>>('/shelf/contents/order', data)
}

/** 切换货架内容推荐 */
export function updateShelfContentRecommend(id: number | string, shopId: number | string) {
  return request.put<any, Result<void>>(`/shelf/contents/${id}/recommend`, null, { params: { shopId } })
}

/** 更新货架内容标签 */
export function updateShelfContentTags(id: number | string, shopId: number | string, data: ShelfTagsBody) {
  return request.put<any, Result<void>>(`/shelf/contents/${id}/tags`, data, { params: { shopId } })
}

/** 从货架移除内容 */
export function removeContentFromShelf(id: number | string, shopId: number | string) {
  return request.delete<any, Result<void>>(`/shelf/contents/${id}`, { params: { shopId } })
}
