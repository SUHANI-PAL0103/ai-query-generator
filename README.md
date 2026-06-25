# AI Query Generator

## How to Run (Must Follow This Order)

### Step 1: Start Backend
Open a terminal and run:
```bash
cd backend/ai-query-generator
mvn spring-boot:run
```
Wait until you see: `Started AiquerygeneratorApplication`

### Step 2: Start Frontend
Open a **new** terminal and run:
```bash
cd frontend
npm run dev
```

### Step 3: Open Browser
Go to: `http://localhost:5173`

## Important Notes
- Backend must be running on port `8081` before starting frontend
- Frontend runs on port `5173` and proxies API calls to backend
- If you see 502 errors, the backend is not running — start it first