-- Yahoo Finance RSS에서 수집한 경제 뉴스 시드 데이터 (2026-03-30 기준)

-- ── STOCK (S&P 500 / Dow Jones) ──
INSERT INTO news_articles (title, summary, url, source, category, published_at, created_at, updated_at) VALUES
('Growth Jitters Lift Treasuries as Stocks Bounce: Markets Wrap',
 'Treasuries rose as worries that the war in the Middle East will trigger a sharp economic slowdown overshadowed a rebound in equities.',
 'https://finance.yahoo.com/markets/commodities/articles/investor-unease-builds-entering-war-193125656.html',
 'Bloomberg', 'STOCK', '2026-03-30 11:49:18', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Morgan Stanley''s Wilson Says S&P 500 Correction Nears End Stage',
 'The S&P 500 correction is nearing its final stage as sentiment and positioning indicators suggest a bottom is approaching.',
 'https://finance.yahoo.com/markets/stocks/articles/morgan-stanley-wilson-says-p-091053905.html',
 'Bloomberg', 'STOCK', '2026-03-30 09:10:53', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Stocks Rise Pre-Bell, Oil Prices Jump as Traders Digest Trump''s Latest Comments on Iran',
 'The benchmark US stock measures were trending higher before the opening bell Monday as oil prices surged.',
 'https://finance.yahoo.com/markets/stocks/articles/stocks-rise-pre-bell-oil-113930014.html',
 'Yahoo Finance', 'STOCK', '2026-03-30 11:39:30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Big Tech Stocks Rout Is Flashing Signals of a Turnaround',
 'The wreckage in large technology stocks is showing early signs of stabilization according to multiple technical indicators.',
 'https://finance.yahoo.com/markets/stocks/articles/big-tech-stocks-rout-flashing-112815599.html',
 'Bloomberg', 'STOCK', '2026-03-30 11:28:15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Don''t Look Now, but the Federal Reserve''s March Inflation Forecast Just Worsened',
 'Historic energy supply chain disruption may upend Wall Street''s expectations for rate cuts this year.',
 'https://www.fool.com/investing/2026/03/30/federal-reserve-march-inflation-forecast-worsened/',
 'Motley Fool', 'MACRO', '2026-03-30 09:26:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Citigroup holds firm on S&P 500 target despite Iran tensions',
 'Citigroup maintains year-end S&P 500 target, citing underlying economic resilience despite geopolitical headwinds.',
 'https://www.thestreet.com/investing/citigroup-holds-firm-on-sp-500-target-despite-iran-tensions',
 'TheStreet', 'STOCK', '2026-03-30 09:03:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dow Jones and Nasdaq futures called higher despite mixed message on Iran war',
 'US stock futures were pointing to a firmer open on Monday as investors weighed escalating Middle East tensions.',
 'https://www.proactiveinvestors.com/companies/news/1089741/',
 'Proactive Investors', 'STOCK', '2026-03-30 11:31:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Stock Market Crash Likely Won''t Hurt 5 Safe High-Yielding Dividend Kings',
 'Companies that have raised dividends for shareholders for 50 years or more remain resilient in downturns.',
 'https://247wallst.com/investing/2026/03/30/stock-market-crash-likely-wont-hurt-5-safe-high-yielding-dividend-kings/',
 '24/7 Wall St', 'STOCK', '2026-03-30 11:45:56', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── FOREX ──
INSERT INTO news_articles (title, summary, url, source, category, published_at, created_at, updated_at) VALUES
('Dollar Under Pressure as Investors Weigh Rate Outlook Amid Geopolitical Risks',
 'The US dollar weakened against major currencies as traders reassess Federal Reserve rate path amid rising Middle East tensions.',
 'https://finance.yahoo.com/markets/forex/articles/dollar-under-pressure-032026.html',
 'Reuters', 'FOREX', '2026-03-30 08:15:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EUR/USD Climbs to 3-Week High on Diverging Central Bank Expectations',
 'The euro advanced against the dollar as ECB officials signaled patience on rate cuts while Fed pricing shifted dovish.',
 'https://finance.yahoo.com/markets/forex/articles/eurusd-climbs-3-week-high-032026.html',
 'Bloomberg', 'FOREX', '2026-03-29 14:22:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Won Weakens Past 1,380 as Safe-Haven Demand Boosts Dollar Against Asian FX',
 'The Korean won slid against the greenback as regional currencies came under pressure from risk-off sentiment.',
 'https://finance.yahoo.com/markets/forex/articles/won-weakens-past-1380-032026.html',
 'Reuters', 'FOREX', '2026-03-28 10:45:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── CRYPTO ──
INSERT INTO news_articles (title, summary, url, source, category, published_at, created_at, updated_at) VALUES
('Bitcoin Holds Above $84K as Crypto Markets Await Macro Catalysts',
 'Bitcoin steadied above $84,000 as traders await key US economic data releases this week for directional cues.',
 'https://finance.yahoo.com/markets/crypto/articles/bitcoin-holds-above-84k-032026.html',
 'CoinDesk', 'CRYPTO', '2026-03-30 10:30:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ethereum Gas Fees Hit 2-Year Low as Layer 2 Adoption Accelerates',
 'Ethereum mainnet transaction fees dropped to their lowest since 2024 as more activity migrates to Layer 2 networks.',
 'https://finance.yahoo.com/markets/crypto/articles/ethereum-gas-fees-2year-low-032026.html',
 'The Block', 'CRYPTO', '2026-03-29 16:15:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Institutional Bitcoin ETF Inflows Resume After 2-Week Pause',
 'Major Bitcoin spot ETFs saw net inflows of $340 million last week, reversing a two-week outflow streak.',
 'https://finance.yahoo.com/markets/crypto/articles/institutional-btc-etf-inflows-032026.html',
 'Bloomberg', 'CRYPTO', '2026-03-28 09:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── COMMODITY ──
INSERT INTO news_articles (title, summary, url, source, category, published_at, created_at, updated_at) VALUES
('Gold Surges Past $3,100 on Middle East Escalation and Rate Cut Bets',
 'Gold futures hit a record high as escalating Iran conflict boosted safe-haven demand and rate cut expectations firmed.',
 'https://finance.yahoo.com/markets/commodities/articles/gold-surges-past-3100-032026.html',
 'Reuters', 'COMMODITY', '2026-03-30 11:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Oil Jumps 4% as Iran Tensions Threaten Strait of Hormuz Supply Routes',
 'Crude oil prices surged as military activity near the Strait of Hormuz raised supply disruption fears for 20% of global oil trade.',
 'https://finance.yahoo.com/markets/commodities/articles/oil-jumps-4pct-iran-032026.html',
 'Bloomberg', 'COMMODITY', '2026-03-30 10:15:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Copper Rallies on China Stimulus Hopes and Supply Constraints',
 'Copper futures rose as Beijing signaled fresh infrastructure spending while Chilean mine output fell below forecasts.',
 'https://finance.yahoo.com/markets/commodities/articles/copper-rallies-china-stimulus-032026.html',
 'Reuters', 'COMMODITY', '2026-03-29 13:45:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Natural Gas Prices Climb as Late-Season Cold Snap Boosts Heating Demand',
 'US natural gas futures rose on forecasts for below-normal temperatures across the Midwest and Northeast.',
 'https://finance.yahoo.com/markets/commodities/articles/natural-gas-prices-climb-032026.html',
 'Barchart', 'COMMODITY', '2026-03-28 15:30:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── BOND ──
INSERT INTO news_articles (title, summary, url, source, category, published_at, created_at, updated_at) VALUES
('Treasury Yields Fall as Flight to Safety Intensifies Amid War Fears',
 'The 10-year Treasury yield dropped to 4.18% as investors sought the safety of government bonds amid rising geopolitical uncertainty.',
 'https://finance.yahoo.com/markets/bonds/articles/treasury-yields-fall-safety-032026.html',
 'Reuters', 'BOND', '2026-03-30 09:30:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Yield Curve Steepens as Short-End Rates Price In More Fed Cuts',
 'The 2s10s spread widened to 42 basis points as the market priced in additional Fed easing cycles by year-end.',
 'https://finance.yahoo.com/markets/bonds/articles/yield-curve-steepens-032026.html',
 'Bloomberg', 'BOND', '2026-03-29 11:20:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ── MACRO ──
INSERT INTO news_articles (title, summary, url, source, category, published_at, created_at, updated_at) VALUES
('US Consumer Confidence Falls to Lowest Level Since 2022 on War and Inflation Concerns',
 'The Conference Board''s consumer confidence index dropped sharply as households worried about rising energy costs and economic uncertainty.',
 'https://finance.yahoo.com/markets/economy/articles/us-consumer-confidence-falls-032026.html',
 'Reuters', 'MACRO', '2026-03-29 14:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ISM Manufacturing PMI Expected to Show Continued Contraction in March',
 'Economists forecast the ISM manufacturing index at 48.5 for March, marking the fifth consecutive month below the 50 expansion threshold.',
 'https://finance.yahoo.com/markets/economy/articles/ism-manufacturing-pmi-march-032026.html',
 'CNBC', 'MACRO', '2026-03-30 07:45:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PCE Inflation Data Due Friday — Why This Is the Most Important Number for Fed Policy',
 'The Fed''s preferred inflation gauge is expected to show core PCE at 2.7% year-over-year, above the 2% target.',
 'https://finance.yahoo.com/markets/economy/articles/pce-inflation-data-friday-032026.html',
 'Yahoo Finance', 'MACRO', '2026-03-28 08:30:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
