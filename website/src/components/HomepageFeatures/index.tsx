import type { ReactNode } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import ThemedImage from '@theme/ThemedImage';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg?: React.ComponentType<React.ComponentProps<'svg'>>;
  imgSrc?: string; // fallback single image
  imgSrcLight?: string;
  imgSrcDark?: string;
  description: ReactNode;
  link?: string;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Rune keywords',
    imgSrc: require('@site/static/img/rune/icon/Rune-icon-Keyword-search.png').default,
    description: (
      <>
        Search for a keyword to find the modelling component info you want, fast.
      </>
    ),
    link: '/docs/get-started/rune-keywords',
  },
  {
    title: 'Components',
    imgSrcLight: require('@site/static/img/rune/icon/Rune-icon-components-pink.png').default,
    imgSrcDark: require('@site/static/img/rune/icon/Rune-icon-components-green.png').default,
    description: (
      <>
        Our modelling components and how they work, with examples to show each feature.
      </>
    ),
    link: '/docs/modelling-components',
  },
  {
    title: 'Get started',
    imgSrcLight: require('@site/static/img/rune/icon/Rune-icon-get-started-pink.png').default,
    imgSrcDark: require('@site/static/img/rune/icon/Rune-icon-get-started-green.png').default,
    description: (
      <>
        New to Rune? Interested in data modelling? Start here to find out all you need to know.
      </>
    ),
    link: '/docs/get-started',
  },
];

function Feature({ title, Svg, imgSrc, imgSrcLight, imgSrcDark, description, link }: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        {Svg ? (
          <Svg className={styles.featureSvg} role="img" />
        ) : imgSrcLight && imgSrcDark ? (
          <ThemedImage
            className={styles.featureSvg}
            sources={{ light: imgSrcLight, dark: imgSrcDark }}
            alt={title}
          />
        ) : imgSrc ? (
          <img src={imgSrc} className={styles.featureSvg} alt={title} />
        ) : null}
      </div>
      <div className="text--center padding-horiz--md">
        {link ? (
          <Link to={link}>
            <h3>{title}</h3>
          </Link>
        ) : (
          <Heading as="h3">{title}</Heading>
        )}
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
