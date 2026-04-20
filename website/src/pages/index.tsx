import { type ReactNode, useEffect } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

// Hide search bar on homepage
function useHideSearchBar() {
  useEffect(() => {
    const searchBar = document.querySelector('.navbar__search') as HTMLElement;
    if (searchBar) {
      searchBar.style.display = 'none';
    }
    return () => {
      if (searchBar) {
        searchBar.style.display = '';
      }
    };
  }, []);
}

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx('hero', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className={styles.heroTitle}>
          Rune DSL Docs
        </Heading>
        <p className={styles.heroSubtitle}>{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link
            className="button button--primary button--lg"
            to="/docs/get-started/introducing-rune">
            Rune Docs
          </Link>
          <Link
            className="button button--secondary button--lg"
            href="https://github.com/finos/rune-dsl">
            GitHub Repo
          </Link>
        </div>
        <div className={styles.logoContainer}>
          <img
            src="/img/rune/icon/2024_Rune_Icon.svg"
            alt="Rune DSL Logo"
            className={styles.heroLogo}
          />
        </div>
      </div>
    </header>
  );
}

export default function Home(): ReactNode {
  const { siteConfig } = useDocusaurusContext();
  useHideSearchBar();
  useEffect(() => {
    document.body.classList.add('homepage');
    return () => {
      document.body.classList.remove('homepage');
    };
  }, []);

  return (
    <Layout
      title="Rune DSL Docs"
      description={siteConfig.tagline}>
      <HomepageHeader />
    </Layout>
  );
}
