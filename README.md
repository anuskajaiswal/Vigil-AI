# Agent Behavior & Trust Ledger

AI agents are increasingly being given real autonomy — reading files, calling APIs, deleting records, sending emails — often with nobody actually watching what they're doing. There's no security camera for AI agent behavior the way there is for human employees.
This project is my attempt at building that missing piece: a backend tool that logs every action an AI agent takes, learns what "normal" looks like for that specific agent over time, and automatically flags anything unusual — with a plain-English explanation of why, not just a red flag with no context.

## What it actually does

- Logs every agent action: which agent, what it did, what it touched, whether it succeeded, and when
- Builds a behavior baseline for each agent individually, based on its own history
- Flags an action the moment it's something that agent has never done before (new action, or new resource it's never touched)
- Explains every flag in plain English — e.g. *"This agent has never performed 'export_data' before. Past actions: read_file, send_email, delete_record."*
- Comes with a simple live dashboard so you can actually see flagged events highlighted, instead of reading raw JSON

## Why I built it this way

I went with a simple rule-based approach for anomaly detection ("has this agent ever done this before?") instead of something like frequency scoring or ML from the start. With limited historical data, a percentage-based approach doesn't really mean much yet — and honestly, for a security tool, being able to clearly explain *why* something got flagged matters more than sounding sophisticated. A black-box anomaly score isn't very useful if nobody trusts it.

## How it works

Agent performs an action, which triggers POST /api/events. That runs two checks: has this agent done this action before, and has this agent touched this resource before. Based on those checks, the event gets flagged with an explanation or logged as normal. Either way, it's saved in PostgreSQL and shown on the dashboard.

## Built with

Java + Spring Boot for the backend
PostgreSQL for storage
Spring Data JPA / Hibernate so I'm not writing raw SQL by hand
Plain HTML/CSS/JS for the dashboard — no frameworks, kept it simple on purpose
Tested manually with curl before building the frontend, to make sure the logic actually worked before worrying about how it looked

## API

POST /api/events — Logs a new event and runs the anomaly check automatically
GET /api/events — Returns everything logged so far
GET /api/events/agent/{agentName} — Returns events for one specific agent
GET /api/events/flagged — Returns only the suspicious ones
