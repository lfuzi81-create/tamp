import { request } from '@tamp/core'
import type { Result, PageResult, PaginateParams } from '@tamp/core'

/** 产品 VO */
export interface ProductVO {
  id: number | string
  title: string
  categoryId: number | string
  categoryName?: string
  coverImage?: string
  summary?: string
  detail?: string
  status?: number | string
  isTop?: boolean
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
  [key: string]: any
}

/** 产品分类 */
export interface ProductCategory {
  id: number | string
  name: string
  sortOrder?: number
  [key: string]: any
}

/** 产品查询参数 */
export interface ProductQueryParams extends PaginateParams {
  categoryId?: number | string
  status?: number | string
}

/** 产品上架店铺映射 */
export type ProductShelfShops = Record<string, any>

/** 分页查询产品 */
export function getProducts(params: ProductQueryParams) {
  return request.get<any, Result<PageResult<ProductVO>>>('/products', { params })
}

/** 获取产品详情 */
export function getProductDetail(id: number | string) {
  return request.get<any, Result<ProductVO>>(`/products/${id}`)
}

/** 创建产品 */
export function createProduct(data: Partial<ProductVO>) {
  return request.post<any, Result<ProductVO>>('/products', data)
}

/** 更新产品 */
export function updateProduct(id: number | string, data: Partial<ProductVO>) {
  return request.put<any, Result<ProductVO>>(`/products/${id}`, data)
}

/** 删除产品 */
export function deleteProduct(id: number | string) {
  return request.delete<any, Result<void>>(`/products/${id}`)
}

/** 切换产品上下架状态 */
export function updateProductStatus(id: number | string, status?: number | string) {
  return request.put<any, Result<void>>(`/products/${id}/status`, status !== undefined ? { status } : undefined)
}

/** 获取产品分类列表 */
export function getProductCategories() {
  return request.get<any, Result<ProductCategory[]>>('/products/categories')
}

/** 创建产品分类 */
export function createProductCategory(data: Partial<ProductCategory>) {
  return request.post<any, Result<ProductCategory>>('/products/categories', data)
}

/** 更新产品分类 */
export function updateProductCategory(id: number | string, data: Partial<ProductCategory>) {
  return request.put<any, Result<ProductCategory>>(`/products/categories/${id}`, data)
}

/** 删除产品分类 */
export function deleteProductCategory(id: number | string) {
  return request.delete<any, Result<void>>(`/products/categories/${id}`)
}

/** 获取产品已上架店铺列表 */
export function getProductShelfShops(id: number | string) {
  return request.get<any, Result<ProductShelfShops>>(`/products/${id}/shelf-shops`)
}

/** 获取推荐产品列表 */
export function getRecommendProducts() {
  return request.get<any, Result<ProductVO[]>>('/products/recommend')
}
