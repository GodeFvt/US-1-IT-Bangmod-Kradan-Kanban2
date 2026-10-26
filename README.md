# US-1-IT-Bangmod-Kradan-Kanban

## สมาชิก 
**[jirananyenlab](https://www.github.com/jirananyenlab)  65130500010 จิรนันท์ เย็นลับ**

- Front-End

**[FocusXis](https://www.github.com/FocusXis)  65130500042 ปรมะ จิราศุภดิลก**

- Front-End
- Database

**[GodeFvt](https://www.github.com/GodeFvt)  65130500054 พุทธินันท์ ภาคแสวง**

- Back-End

## Diagram V1.0.0
### Front-End
![Front-End](https://github.com/user-attachments/assets/ac9f4bba-8f25-45a7-b1f5-37887d00654d)

### Database
![Database](https://github.com/user-attachments/assets/e4d043a7-5c74-493f-8cc1-59750b26a4fe)

## PostgreSQL setup

The backend now uses one PostgreSQL datasource. Copy `.env.example` to your
private deployment environment and provide `DATABASE_URL`, `DATABASE_USER`, and
`DATABASE_PASSWORD`. `DATABASE_URL` must be a JDBC URL such as
`jdbc:postgresql://host:5432/taskboard`. Do not commit the password.

- New database: run `database/postgresql.sql`.
- Existing PostgreSQL database with the seven taskboard tables: run
  `database/002_add_authentication.sql`.

Local passwords live only in `user_credentials.password_hash` and must be a
Spring-compatible Argon2 encoded value (normally beginning with `$argon2id$`).
OAuth identities live in `oauth_accounts`; the schema is ready for Google but
the Google client settings remain disabled until credentials are provisioned.

