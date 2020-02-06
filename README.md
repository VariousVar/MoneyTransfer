Simple application for money transferring between accounts.

TODO:
1. Use more specific exceptions, to distinct API errors.
2. Long type used for balance field. Have to be BigDecimal / DECIMAL for real-life balance operating.
3. Behaviour on deleting accounts should be specified, on my opinion - restricted, due to transaction chain issues.
4. There probably should exist service account for account initial balance transaction.