# TradeFlux Frontend

React + Vite frontend for the Stock Brokerage and Portfolio Management System. It talks
exclusively to the Spring Boot REST API — it never connects to MySQL directly.

```
React frontend (this app, :8081) → Spring Boot backend (:8080) → MySQL
```

## Running locally

1. Start MySQL and the backend first (see `../backend/README.md` / `../README.md`).
   The backend must be running on `http://localhost:8080`.
2. Install dependencies and start the dev server:

   ```bash
   npm install
   npm run dev
   ```

3. Open http://localhost:8081. In development, Vite proxies any `/api/*` request to
   `http://localhost:8080` (see `vite.config.js`), so no CORS setup is needed locally.

## Login

- Bootstrap admin: `admin@stockbroker.local` / `Admin@12345` (seeded by `DataInitializer`
  on first backend startup — change this password in any non-local environment).
- New clients self-register via the Register page. Dealer / Research Analyst / Compliance
  Officer / Risk Manager accounts are created by an Admin from the in-app Admin Panel
  (`Admin Panel → Provision Staff Account`).
- A freshly registered client must complete KYC (Profile & Settings → KYC & Verification)
  before they can place trades.

## Production builds

```bash
npm run build
```

Set `VITE_API_BASE_URL` (see `.env.example`) if the built app will be served from a
different origin than the backend, and make sure that origin is added to
`security.cors.allowed-origins` in the backend's configuration.

## Project structure

- `src/api/` — one module per backend controller, thin wrappers over a shared axios client.
- `src/context/` — `AuthContext` (JWT session, current user) and `ToastContext` (notifications).
- `src/components/layout/` — sidebar/topbar app shell, role-based navigation.
- `src/pages/` — one file per route.
