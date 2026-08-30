CREATE UNIQUE INDEX ux_articles_source_article_id
    ON articles (source, source_article_id);

CREATE UNIQUE INDEX ux_articles_canonical_url
    ON articles (canonical_url);

CREATE UNIQUE INDEX ux_articles_content_hash
    ON articles (content_hash);
