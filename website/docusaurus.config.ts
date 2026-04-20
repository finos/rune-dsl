import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import {GlossaryPresetOptions} from "docusaurus-plugin-glossary/preset";
import {PluginOptions} from "@docusaurus/plugin-svgr";

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'Rune DSL',
  tagline: 'Rune DSL is a domain‑specific language (DSL) designed to bring clarity and consistency to how financial markets and other sectors describe their processes. Use it with the Rosetta platform to create models that generate consistent, machine‑readable representations of financial products and workflows.',
  favicon: '/img/rune/icon/favicon.ico',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Set the production url of your site here
  url: 'https://rune-docs.netlify.app',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'rune-dsl', // Usually your GitHub org/user name.
  projectName: 'rune-dsl', // Usually your repo name.

  onBrokenLinks: 'throw',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'docusaurus-plugin-glossary/preset',
      {
        // Glossary configuration
        id: 'glossary',
        glossary: {
          glossaryPath: 'glossary/glossary.json',
          routePath: '/docs/resources/glossary',
        },
        // Standard Docusaurus preset-classic options
        docs: {
          sidebarPath: './sidebars.ts',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies GlossaryPresetOptions,
    ],
  ],

  plugins: [
    [
      '@docusaurus/plugin-svgr',
      {
        svgrConfig: {
          typescript: true,
        },
      } satisfies PluginOptions,
    ]
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/rune/icon/2024_Rune_Icon.svg',
    colorMode: {
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'Rune DSL',
      logo: {
        alt: 'Rune DSL Logo',
        src: 'img/rune/icon/2024_Rune_Icon.svg',
      },
      items: [
        {
          type: 'search',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      logo: {
        alt: 'Rune DSL Logo',
        src: 'img/rune/icon/2024_Rune_Icon.svg',
        width: 50,
        height: 50,
      },
      links: [
      ],
      copyright: `Copyright © ${new Date().getFullYear()} REGnosys and the Rune DSL community`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['haskell'],
    },
  } satisfies Preset.ThemeConfig,

  themes: [
    [
      require.resolve("@easyops-cn/docusaurus-search-local"),
      {
        hashed: true,
        indexDocs: true,
        indexBlog: false,
        docsRouteBasePath: "/docs",
      },
    ],
  ]
};

export default config;
