import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
import { fileURLToPath, URL } from 'node:url'
import viteCompression from 'vite-plugin-compression'
import { visualizer } from 'rollup-plugin-visualizer'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    UnoCSS(),
    // 自动导入 Vue 相关 API 和 Naive UI 组件
    AutoImport({
      imports: [
        'vue',
        'vue-router',
        {
          'naive-ui': ['useDialog', 'useMessage', 'useNotification', 'useLoadingBar']
        }
      ],
      dts: 'src/auto-imports.d.ts'
    }),
    // 自动注册组件
    Components({
      resolvers: [NaiveUiResolver()],
      dts: 'src/components.d.ts'
    }),
    // Gzip 压缩
    viteCompression({
      algorithm: 'gzip',
      threshold: 10240,
      verbose: true
    }),
    // Brotli 压缩（更高压缩率）
    viteCompression({
      algorithm: 'brotliCompress',
      threshold: 10240,
      verbose: true
    }),
    // 构建分析工具（可用于分析打包大小）
    visualizer({
      filename: './dist/stats.html',
      open: false
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  build: {
    assetsDir: 'assets',
    chunkSizeWarningLimit: 500,
    // 启用 CSS 代码分割
    cssCodeSplit: true,
    // 生产环境压缩
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    // 代码分割优化
    rollupOptions: {
      output: {
        // 函数式分包策略：更细粒度地拆分 node_modules
        manualChunks(id: string) {
          if (id.includes('node_modules')) {
            // naive-ui 单独拆出（体积最大的 UI 库）
            if (id.includes('naive-ui')) return 'naive-ui'
            // Vue 生态核心
            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router')) return 'vue-ecosystem'
            // 图标库独立拆分（便于缓存）
            if (id.includes('@vicons')) return 'vicons'
            // vditor / markdown 编辑器相关（体积大，按需加载）
            if (id.includes('vditor')) return 'vditor'
            // 其他第三方依赖
            return 'vendor'
          }
        },
        // 生成 hash 文件名用于缓存
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    },
    // 开启 Tree Shaking
    target: 'es2020'
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'pinia',
      'vue-router',
      'naive-ui',
      'sm-crypto'
    ],
    exclude: []
  }
})
