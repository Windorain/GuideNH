# Localization

GuideNH supports localized guide pages and localized guide assets.

## Folder Layout

Runtime localization is folder-based:

```text
wiki/resourcepack/assets/<modid>/guidenh/
|-- _en_us/
|   `-- index.md
`-- _zh_cn/
    `-- index.md
```

Language folders are recognized only in the underscored form. Plain folders such as `en_us/` and `zh_cn/` are no longer treated as localization roots.

## Page Lookup Order

For each requested page id, GuideNH tries:

1. `_<current language>/<page>`
2. `_<default language>/<page>` if the current language page is missing
3. `<page>` without a language folder

Guide pages only fall back to the guide's `defaultLanguage`. Auto-discovered resource-pack guides still default that value to `en_us`, so another language is not promoted into a fallback language just because it exists.

## Asset Lookup Order

Guide assets use a slightly richer fallback order:

1. `_<current language>/<path>`
2. if the current language is not the guide default language, `_<default language>/<path>`
3. `<path>`

This makes it possible to localize images or texture-like assets when needed.

## Search And Language

Search documents store both the raw Minecraft language and the analyzer language used for Lucene. If the current Minecraft language is not mapped to a known analyzer, search falls back to English tokenization.

## Ignore Translation Config

GuideNH does not expose a global "ignore translations" switch. If you want a guide to fall back to a non-English language, set that guide's `defaultLanguage` explicitly in code.

## Authoring Advice

- set `defaultLanguage` deliberately when you want a non-English fallback language for a guide
- add a shared language-neutral page only when cross-language fallback is actually intended
- translate pages first, then translate assets only when text is embedded in the asset
- avoid language-specific asset filenames when a rooted shared asset would do

## Example

```text
wiki/resourcepack/assets/guidenh/guidenh/_en_us/index.md
wiki/resourcepack/assets/guidenh/guidenh/_zh_cn/index.md
wiki/resourcepack/assets/guidenh/guidenh/_en_us/test1.png
wiki/resourcepack/assets/guidenh/guidenh/_zh_cn/test1.png
```

## Related Pages

- [Guide Page Format](Guide-Page-Format)
- [Images And Assets](Images-And-Assets)
