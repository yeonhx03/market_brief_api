CREATE TABLE article_tickers (
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    ticker VARCHAR(16) NOT NULL,
    PRIMARY KEY (article_id, ticker)
);

CREATE INDEX ix_article_tickers_ticker_article
    ON article_tickers (ticker, article_id);
