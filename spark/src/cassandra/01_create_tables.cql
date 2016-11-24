DROP TABLE IF EXISTS fast2smart.member_monthly_balance;
CREATE TABLE fast2smart.member_monthly_balance (
    member bigint,
    year int,
    month int,
    amount bigint,
    maxdate timestamp,
    PRIMARY KEY (member, year, month));

DROP TABLE IF EXISTS fast2smart.member_delta_balance;
CREATE TABLE fast2smart.member_delta_balance (
    member bigint,
    year int,
    month int,
    amount bigint,
    maxdate timestamp,
    PRIMARY KEY (member, year, month));