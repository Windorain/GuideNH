[English](Localization)

# 本地化

GuideNH 支持本地化的指南页面与本地化的指南资源。

## 目录结构

运行时本地化基于目录：

```text
wiki/resourcepack/assets/<modid>/guidenh/
|-- _en_us/
|   `-- index.md
`-- _zh_cn/
    `-- index.md
```

语言目录只认以下划线开头的形式。像 `en_us/` 和 `zh_cn/` 这样的普通目录不再被当作本地化根目录。

## 页面查找顺序

对于每个请求的页面 id，GuideNH 会依次尝试：

1. `_<current language>/<page>`
2. 若当前语言页面缺失，则尝试 `_<default language>/<page>`
3. 不带语言目录的 `<page>`

指南页面只会回退到该 guide 的 `defaultLanguage`。自动发现的资源包 guide 仍然默认把这个值设为 `en_us`，所以不会因为某个别的语言存在，就把它自动提升成兜底语言。

## 资源查找顺序

指南资源使用稍微更丰富的回退顺序：

1. `_<current language>/<path>`
2. 若当前语言不是指南默认语言，则尝试 `_<default language>/<path>`
3. `<path>`

这样在需要时，就可以对图片或类似纹理的资源进行本地化。

## 搜索与语言

搜索文档会同时记录原始 Minecraft 语言和 Lucene 实际使用的 analyzer 语言。若当前 Minecraft 语言未映射到已知 analyzer，搜索会回退到英文分词。

## 忽略翻译配置

GuideNH 目前没有提供全局“忽略翻译”开关。如果你希望某个 guide 回退到非英文语言，请在代码里显式设置该 guide 的 `defaultLanguage`。

## 编写建议

- 如果你希望某个 guide 使用非英文作为回退语言，请显式设置 `defaultLanguage`
- 只有在确实希望跨语言共享时，才添加无语言的共享页面
- 先翻译页面，再在资源中确实嵌入了文本时才翻译资源
- 若共享资源足够通用，就不要额外引入语言特定的资源文件名

## 示例

```text
wiki/resourcepack/assets/guidenh/guidenh/_en_us/index.md
wiki/resourcepack/assets/guidenh/guidenh/_zh_cn/index.md
wiki/resourcepack/assets/guidenh/guidenh/_en_us/test1.png
wiki/resourcepack/assets/guidenh/guidenh/_zh_cn/test1.png
```

## 相关页面

- [指南页面格式](Guide-Page-Format-zh-CN)
- [图片与资源](Images-And-Assets-zh-CN)
