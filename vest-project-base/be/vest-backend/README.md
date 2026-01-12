# vest-backend (Spring Boot)

## What’s included
- Full **JPA entity mapping** generated from your SQL Server script (`ERD_AoVestNam_SQLServer.sql`).
- Base layered structure: `domain/entity`, `repository`, `service`, `web/controller`, `web/dto`, `exception`, `config`.
- Sample endpoints (CRUD skeleton): `GET/POST /api/san-pham`.
- Swagger UI: `/swagger`.

## Run (local)
1) Create DB & tables by running your SQL script on SQL Server.
2) Update `src/main/resources/application.yml` with your DB credentials.
3) Run:
```bash
mvn spring-boot:run
```

> If you want I can scaffold thêm CRUD cho `hoa_don`, `san_pham_chi_tiet`, `phieu_giam_gia` theo đúng flow online/offline.
