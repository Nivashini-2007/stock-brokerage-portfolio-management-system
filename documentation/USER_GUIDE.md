# TradeFlux User Guide

A working guide to every screen and every role in the Stock Brokerage and Portfolio
Management System — from a client placing a first order to an administrator
provisioning a compliance officer. Written against the app as it actually behaves.

```
React frontend (:8081) → Spring Boot REST API (:8080) → MySQL
```

## Quick start

1. Start MySQL, then the backend (`mvn spring-boot:run` in `backend/`), then the
   frontend (`npm run dev` in `frontend/`).
2. Open `http://localhost:8081`.
3. Sign in as the seeded admin, or register a new client account.
4. New clients: finish KYC before the Terminal will accept an order.

### Seeded login

| | |
|---|---|
| Email | `admin@stockbroker.local` |
| Password | `Admin@12345` |

This is the only account that exists on a fresh database. Every other user — client
or staff — is created from inside the app (see [Getting started](#getting-started)).
Rotate this password before running the app anywhere beyond a laptop.

---

## Table of contents

- [Getting started](#getting-started)
- [Common to every role](#common-to-every-role)
- [Permission matrix](#permission-matrix)
- [Client guide](#client-guide)
- [Dealer guide](#dealer-guide)
- [Research Analyst guide](#research-analyst-guide)
- [Compliance Officer guide](#compliance-officer-guide)
- [Risk Manager guide](#risk-manager-guide)
- [Administrator guide](#administrator-guide)
- [Reference: order types](#reference-order-types)
- [Reference: how margin works](#reference-how-margin-works)
- [Reference: tax report logic](#reference-tax-report-logic)
- [Reference: known limitations](#reference-known-limitations)

---

## Getting started

The system has one door in — the Login page — and two ways through it:
self-registration for clients, and admin-provisioning for everyone who works at the
brokerage.

### Becoming a client

1. **Register.** Open the Register page and fill in name, email, mobile number and a
   password (min. 8 characters, mixed case + a digit + a symbol).
   Names accept letters and spaces only; mobile numbers must be exactly 10 digits —
   the form validates this before it ever calls the API.
2. **Sign in.** Use the email and password you just registered with. Email is the
   only accepted login identifier.
3. **Complete KYC.** Go to *Profile & Settings → KYC & Verification* and submit your
   PAN, DEMAT ID, bank account number and IFSC code.
   PAN must match `AAAAA9999A`, IFSC must match `AAAA0999999`. Until this is
   verified, the Terminal will refuse every order with *"Complete KYC submission
   before trading."*
4. **Add funds.** Go to *Funds* and deposit cash before placing your first order —
   margin starts at zero for every new account.

### Becoming staff (Dealer, Research Analyst, Compliance Officer, Risk Manager, Admin)

Staff accounts are never self-registered. An existing Administrator creates them
from *Admin Panel → Provision Staff Account*, choosing the role from a live list and
setting a temporary password. There is no public sign-up path for these roles — if
you need one, ask whoever holds the admin account.

> **Sessions.** A JWT is issued on login and stored for the browser session. Client
> sessions last 8 hours, staff sessions 12 hours, and the Administrator's session
> lasts 24 hours before you're asked to sign in again.

---

## Common to every role

Three screens behave identically no matter which badge you're wearing.

**Notifications** — A running feed of everything that happened to your own account:
order fills, margin calls, KYC decisions, published research. New items carry a
blue dot and also raise the counter on the bell icon in the top bar, which checks
for updates roughly every 20 seconds.

**Profile & Settings** — Your name, email, phone and role are read-only here —
they're set at account creation. The second tab, *KYC & Verification*, is where
PAN/DEMAT/bank details are submitted and where your current verification status is
shown.

**Watchlist** — A personal list of ticker symbols with live quotes, refreshed every
10 seconds. It lives in your browser's local storage, not on the server — it won't
follow you to another device or browser, and clearing site data clears it.

**Research** — Every role can read the *Published* tab: analyst recommendations
that have cleared compliance review, each with a target price and a one-line
thesis.

---

## Permission matrix

What each role can reach in the sidebar. **L** means a scoped or read-limited
version of the feature, not the full staff view.

| Capability | Client | Dealer | Analyst | Compliance | Risk Mgr | Admin |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| View market data & research | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Place / cancel orders | ✓ | ✓ | – | – | – | ✓ |
| View own portfolio | ✓ | ✓ | L | L | L | ✓ |
| View all client portfolios | – | ✓ | – | ✓ | ✓ | ✓ |
| Publish research reports | – | – | ✓ | – | – | ✓ |
| Approve / reject research | – | – | – | ✓ | – | ✓ |
| Monitor client margins | – | L | – | – | ✓ | ✓ |
| Halt / resume a symbol | – | ✓ | – | – | ✓ | ✓ |
| KYC review | – | – | – | ✓ | – | ✓ |
| Regulatory CSV reports | – | – | – | ✓ | – | ✓ |
| Provision staff accounts | – | – | – | – | – | ✓ |

---

## Client guide

The retail investor. Trades their own money, tracks their own portfolio, and files
their own tax paperwork at year end. Onboards by self-registration.

**Screens:** Dashboard · Terminal · Portfolio · Holdings · Orders · Positions ·
Funds · Watchlist · Research

### Placing a trade

1. **Open the Terminal** and pick a symbol from the left rail, or jump to any symbol
   with the search box in the top bar.
2. **Choose BUY or SELL**, then an order type — see
   [Order types](#reference-order-types) for what each one needs.
3. **Set quantity** (and price / trigger / target, if the order type asks for them).
   The panel shows the estimated order value against the live quote as you type.
4. **Submit.** A `MARKET` order fills immediately if the symbol isn't
   circuit-halted and you hold enough available margin; other types sit as
   `PENDING` until triggered or cancelled.

### Tracking what you own

**Portfolio** gives the headline numbers — current value, total gain/loss, cost
basis — plus a year-by-year capital gains report. **Holdings** is the same
positions in a searchable, sortable, exportable table. **Positions** is the same
list again, framed for action: a *Square Off* button next to each row places a
market order to close it in one click.

### Funds

Deposit or withdraw cash from *Funds*; every movement, and every
brokerage/GST/STT/stamp-duty charge on a trade, appears underneath in the transfer
history. The three stat tiles at the top are *margin* figures, not a separate
wallet — see [How margin works](#reference-how-margin-works) for exactly how
they're derived from that ledger.

> **Immutable once filled.** An `EXECUTED` order cannot be cancelled or edited —
> only a `PENDING` order can be. To exit a filled position, place an opposite-side
> order (or use *Square Off* on the Positions page).

---

## Dealer guide

Trades on behalf of clients and keeps an eye on the full order flow. Created by an
Administrator.

**Screens:** Dashboard · Terminal · Portfolio (all) · Orders (book) · Positions ·
Risk Alerts · Market Admin

The Terminal works the same as for a client, with one addition: a **Client ID**
field appears above the order form so an order can be placed for any client
account, not just your own.

**Orders** defaults to the full *Order Book* — every order from every client — with
a toggle to switch to a single client's history by ID. **Portfolio** has the same
toggle, via *View All Clients*, for checking anyone's holdings at a glance.

From **Market Admin** a Dealer can list a new tradable symbol, update a stock's
reference price (there is no live NSE/BSE feed wired up — prices only move when a
Dealer or Admin sets them here), and halt or resume trading on a symbol from the
same table.

---

## Research Analyst guide

Writes the investment calls clients read on the Research page. Created by an
Administrator.

**Screens:** Dashboard · Research → New Report · Research → My Reports

1. **Open Research → New Report** and fill in the company, symbol, a Buy/Hold/Sell
   call, target price and a short thesis.
2. **Submit for review.** The report enters `PENDING_REVIEW` — it is not visible to
   clients yet.
3. **Track it under My Reports**, which shows every report you've written
   regardless of status.
4. Once a Compliance Officer approves it, its status flips to `PUBLISHED` and it
   appears on the public Research page for every client.

---

## Compliance Officer guide

The gate between an analyst's draft and a client seeing it, and between an
applicant and an active trading account. Created by an Administrator.

**Screens:** Dashboard · Research → Pending Review · Compliance · Portfolio (all,
read-only)

### Reviewing research

Open **Research → Pending Review** to see every report awaiting a decision, with
the full thesis and target price on each card. *Approve* publishes it immediately
to every client; *Reject* returns it to the analyst without publishing.

### Reviewing KYC

On the **Compliance** page, enter a client's numeric ID under *KYC Review* to pull
up their PAN (masked), DEMAT and current status. Add an optional remark and choose
*Approve KYC* or *Reject KYC* — approval is what flips a client's trading status to
`ACTIVE`.

There is no searchable list of pending applicants yet — you'll need the client's ID
from another source (a support ticket, the order book, the client themselves)
before you can look them up.

### Regulatory exports

The same **Compliance** page generates two exchange-format CSV files: a *Daily
Activity Report* for a chosen date (every order executed that day), and the *UCC
file* — one row per client with their trading and KYC status. Both download
straight to your machine.

---

## Risk Manager guide

Watches margin exposure across every client and holds the circuit-breaker switch.
Created by an Administrator.

**Screens:** Dashboard · Risk Alerts · Portfolio (all, read-only)

The **Risk Alerts** page lists every active alert — margin calls and forced
square-offs, raised automatically once a client's margin utilisation crosses 80%
and 90% respectively (see [How margin works](#reference-how-margin-works)). Two
tools sit above the table:

- **Margin lookup** — enter a client ID to see their available margin, used margin
  and utilisation percentage on demand, without waiting for an alert to fire.
- **Circuit breaker** — pick a symbol and halt or resume trading on it instantly. A
  halted symbol rejects every new order platform-wide until it's resumed.

---

## Administrator guide

Holds every permission above, plus the one thing nobody else can do: create new
staff logins. There is exactly one Admin on a fresh install — the seeded bootstrap
account.

**Screens:** Everything · Admin Panel

### Provisioning a staff account

1. Open **Admin Panel**.
2. Fill in the new hire's name, email, phone and a temporary password.
3. Choose their role — Dealer, Research Analyst, Compliance Officer, Risk Manager,
   or another Admin — from the live role list.
4. Submit. They can sign in immediately with the password you set; there is no
   separate activation step.

> **Change the seeded password.** `admin@stockbroker.local` / `Admin@12345` is a
> development convenience baked in by the backend's first-run seed data. Rotate it
> before this ever runs anywhere other than a laptop.

---

## Reference: order types

What the Terminal asks for changes with the order type you pick.

| Type | Needs | Behaviour |
|---|---|---|
| **Market** | Quantity | Fills instantly at the current quoted price. |
| **Limit** | Quantity, Price | Rests as `PENDING` until the market reaches your price. |
| **Stop-Loss** | Quantity, Trigger Price | Dormant until the trigger price is touched, then fires as a market order. |
| **Bracket** | Quantity, Price, Trigger Price, Target Price | An entry with a built-in stop-loss and take-profit exit. |
| **Cover** | Quantity, Trigger Price | An entry order paired with a mandatory protective stop. |

---

## Reference: how margin works

There's no leverage in this system — every number on the Funds and Risk Alerts
pages reduces to your cash ledger:

```
Total margin     = ledger cash balance
Used margin      = value of your own PENDING buy orders
Available margin = Total − Used
```

This recalculates whenever an order is placed. Utilisation is `Used ÷ Total`: at
**80%** a margin-call alert and notification fire; at **90%** your own oldest
pending buy orders are automatically cancelled, oldest first, until utilisation
drops back below the threshold. Settled holdings are never force-sold by this
mechanism — only unfilled pending orders are released.

---

## Reference: tax report logic

The annual report on the Portfolio page applies FIFO cost basis to every closed
position in the chosen financial year, then splits the result by how long each lot
was held:

- **STCG** — held 365 days or less.
- **LTCG** — held more than 365 days.

Alongside the gain/loss table it estimates tax owed on each bucket after the
configured LTCG exemption, purely as a planning figure — treat it as a worksheet,
not a filing.

---

## Reference: known limitations

| Area | What to expect |
|---|---|
| Market prices | No live NSE/BSE feed. Prices move only when a Dealer or Admin updates them from Market Admin. |
| Notifications | No mark-as-read control — the feed is read-only history. |
| Watchlist | Stored per-browser (local storage), not synced to your account or across devices. |
| Staff client lookups | Margin, KYC, portfolio and order lookups by client ID require already knowing that ID — there's no client directory search yet. |
| Live updates | Quotes and notification counts poll every 8–20 seconds; nothing pushes to the browser instantly. |
