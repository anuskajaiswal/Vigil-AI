# Vigil

**Vigil watches what AI agents actually do — and explains why something looks off, in plain English.**

🔗 **Try it live:** https://vigil-ai-uuhq.onrender.com
*(free hosting — give it ~30 seconds to wake up on first load)*

---

## Why I built this

AI agents are starting to take real actions on people's behalf — reading files, sending emails, moving data, calling other systems. Most of the tooling around this focuses on making agents *more capable*. Almost none of it focuses on a simpler question: **once an agent is out there acting, who's actually watching what it does?**

Vigil is my attempt at answering that. It's a small, focused security tool that sits behind any AI agent, logs every action it takes, and flags the moment something doesn't match how that agent has behaved before — with a plain-English reason, not a cryptic error code.

## What it actually does

1. **Logs every action** — agent name, what it did, what it touched, and when.
2. **Checks it against history** — has *this* agent done *this* action, on *this* resource, before?
3. **Flags what's new** — if not, it raises a flag and explains exactly why, referencing the agent's past behavior.
4. **Shows it all on a live dashboard** — a real-time feed of every action, with anomalies highlighted.

This isn't about deciding good vs. bad — it's about surfacing the *unfamiliar*, so a human (or a smarter system downstream) can decide what to do about it.

## See it in action

Here's a real example, pulled directly from the live system:

**Normal behavior — nothing flagged:**
```json
{
  "agentName": "customer-support-bot",
  "action": "read_file",
  "resource": "customer_records.csv",
  "flagged": false,
  "explanation": null
}
```

**Anomaly — flagged with a plain-English reason:**
```json
{
  "agentName": "customer-support-bot",
  "action": "export_data",
  "resource": "customer_records.csv",
  "flagged": true,
  "explanation": "This agent has never performed 'export_data' before. Past actions: read_file, send_email."
}
```

More sample scenarios live in [`/examples`](./examples), including an agent that suddenly touches a file it's never accessed before.

**Want to try it with your own scenario?** Go to the [live dashboard](https://vigil-ai-uuhq.onrender.com) and use the "Try it yourself" box — type in an agent name, an action, and a resource, and watch Vigil evaluate it in real time.

## How it's built

- **Backend:** Java, Spring Boot, REST API
- **Database:** PostgreSQL
- **Anomaly detection:** custom logic that checks each new action/resource pair against that agent's full history
- **Frontend:** a dark-themed live dashboard (no framework — plain HTML/CSS/JS), polling the API in real time
- **Deployment:** Render (backend + database), auto-deployed from this repo via Docker

## Running it locally

```bash
git clone https://github.com/anuskajaiswal/Vigil-AI.git
cd Vigil-AI
./mvnw spring-boot:run
```

Then open `http://localhost:8080` — you'll need a local PostgreSQL instance running and configured in `src/main/resources/application.properties`.

## License

The source code here is shared for viewing and portfolio purposes — see [`LICENSE`](./LICENSE) for details. The live hosted dashboard is free for anyone to use as an end-user tool.

## About

Built by Anuska Jaiswal, a cybersecurity engineering student, as part of an ongoing exploration into what real, practical AI safety tooling can look like — not just theory, something you can actually click on and try.
