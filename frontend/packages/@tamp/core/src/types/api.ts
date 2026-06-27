/** 统一 API 响应 */
export interface Result<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}

/** 分页响应 */
export interface PageResult<T = any> {
  list: T[]
  pageNum: number
  pageSize: number
  total: number
  pages: number
}

/** 分页请求参数 */
export interface PaginateParams {
  page?: number
  size?: number
  keyword?: string
  [key: string]: any
}
