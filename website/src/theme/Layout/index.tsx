import React, {type ReactNode} from 'react';
import Layout from '@theme-original/Layout';
import type LayoutType from '@theme/Layout';
import type {WrapperProps} from '@docusaurus/types';
import {useLocation} from '@docusaurus/router';
import Link from '@docusaurus/Link';
import Head from '@docusaurus/Head';

type Props = WrapperProps<typeof LayoutType> & {children?: ReactNode};

export default function LayoutWrapper(props: Props): ReactNode {
  const {pathname} = useLocation();
  const isGlossary = pathname.includes('/resources/glossary');

  // Create a class name from the pathname for CSS targeting
  const pageClass = pathname
    .replace(/\/$/, '')
    .split('/')
    .filter(Boolean)
    .join('-');

  return (
    <Layout {...props}>
      <Head>
        <body className={pageClass ? `page-${pageClass}` : ''} />
      </Head>
      {isGlossary && (
        <div className="container margin-top--lg">
          <div style={{marginBottom: '1rem'}}>
            <Link className="button button--outline button--primary button--sm" to="/docs/resources">
              « Return to Resources
            </Link>
          </div>
        </div>
      )}
      {props.children}
    </Layout>
  );
}
