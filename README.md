Simple application for money transferring between accounts.

### API:

| METHOD | PATH                      |           BODY          | Response                | Description                                     |
|--------|---------------------------|:-----------------------:|-------------------------|-------------------------------------------------|
| GET    | /account/                 |            x            | AccountModel            | Get all registered accounts                     |
| GET    | /account/{id}             |            x            | Array<AccountModel>     | Get one account by id                           |
| POST   | /account/                 |    CreateAccountModel   | AccountModel            | Create new account                              |
| PATCH  | /account/{id}             |    ChangeAccountModel   | AccountModel            | Change account                                  |
| DELETE | /account/{id}             |            x            |                         | Delete account                                  |
| GET    | /account/{id}/transaction |            x            | Array<TransactionModel> | Get all transactions that correspond to account |
| POST   | /transaction              | ExecuteTransactionModel | TransactionModel        | Execute transaction between accounts            |

##### AccountModel

| Field   | Type   |
|---------|--------|
| id      | Long   |
| name    | String |
| balance | Long   |

##### CreateAccountModel

| Field   | Type   |
|---------|--------|
| name    | String |
| balance | Long   |

##### ChangeAccountModel

| Field   | Type   |
|---------|--------|
| name    | String |

##### TransactionModel

| Field       | Type   |
|-------------|--------|
| id          | Long   |
| sender      | Long   |
| receiver    | Long   |
| amount      | Long   |
| description | String |
| created     | Date   |

##### ExecuteTransactionModel

| Field       | Type   |
|-------------|--------|
| sender      | Long   |
| receiver    | Long   |
| amount      | Long   |
| description | String |






TODO:
1. Use more specific exceptions, to distinct API errors.
2. Long type used for balance field. Have to be BigDecimal / DECIMAL for real-life balance operating.
3. Behaviour on deleting accounts should be specified, on my opinion - restricted, due to transaction chain issues.
4. There probably should exist service account for account initial balance transaction.
5. Use SWAGGER for API presenting.