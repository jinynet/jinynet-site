import { sm2 } from 'sm-crypto'

/**
 * SM2加密工具类
 */
export class Sm2Utils {
  /**
   * 使用公钥加密
   * @param publicKey 公钥（16进制字符串，带04前缀）
   * @param data 待加密的数据
   * @returns 加密后的16进制字符串
   */
  static encrypt(publicKey: string, data: string): string {
    // 确保公钥带04前缀
    let key = publicKey
    if (!key.startsWith('04')) {
      key = '04' + key
    }
    // 使用sm-crypto加密，返回hex格式字符串
    return sm2.doEncrypt(data, key, 1)
  }
}
