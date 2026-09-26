# Seminar Suite: Render + Supabase

This package adds cloud deployment support while preserving the existing pages and login flow.
It is not deployed yet. Your accounts and database connection details are required.

## 1. Create the free database

Visit https://supabase.com/dashboard and create a project under a Free organization.
Save the DATABASE password privately (this is not your Supabase account password).
Open SQL Editor and run:

```sql
CREATE SCHEMA IF NOT EXISTS seminar;
REVOKE ALL ON SCHEMA seminar FROM PUBLIC, anon, authenticated;
```

The app uses this separate schema, not the public Data API schema. Do not add `seminar`
to Supabase's exposed schemas. Supabase Auth is not used by this application.

In Connect, choose Session pooler, port 5432. Record host, username and database name.
Use these to build the Render variables below. Do not use a Supabase API URL or API key.

## 2. Upload the prepared code to your GitHub repository

Extract this ZIP. Upload the CONTENTS of the folder containing pom.xml and Dockerfile
into a new repository under your account. Keep the original project's attribution.
The Dockerfile, pom.xml, render.yaml and src folder should be at repository root.
Do not upload passwords, .env files or local logs. Existing local MySQL records are
not copied: the new PostgreSQL database starts empty.

## 3. Deploy to Render

Visit https://dashboard.render.com and choose New > Web Service.
Connect the GitHub repository. Select Docker runtime and the Free instance type.
Leave Root Directory blank if Dockerfile is at repository root; otherwise set it to
that folder. Use Dockerfile path ./Dockerfile. No separate build/start command is needed.

Set these environment variables:

| Name | Value |
|---|---|
| SPRING_PROFILES_ACTIVE | cloud |
| DB_URL | jdbc:postgresql://YOUR-SESSION-POOLER-HOST:5432/postgres?sslmode=require |
| DB_USERNAME | Exact Session pooler username, usually postgres.PROJECT_REFERENCE |
| DB_PASSWORD | Your Supabase DATABASE password |

Replace the host placeholder; keep the jdbc:postgresql:// prefix. Password is a separate
variable, not part of the URL. Port is provided by Render. Alternatively, Render's
Blueprint deployment can read render.yaml; it selects the Free plan and prompts for
these three DB variables.

Create the service. Wait for `Started SeminarBookingWebsiteApplication` and Render Live.
Open the assigned https://YOUR-SERVICE.onrender.com address. Its assigned name may differ.

## 4. Create a demo login

This project has no signup route. AFTER the first successful start creates the tables,
run the following in Supabase SQL Editor. Replace the example password with a demo-only
password before running. Do not use your database password or any personal password.

```sql
INSERT INTO seminar.users (email, password)
VALUES ('demo@example.com', 'REPLACE_WITH_DEMO_PASSWORD')
ON CONFLICT (email) DO NOTHING;
```

Log in at your Render URL followed by /login. Test a future booking, My Bookings,
then deletion. Test again on your phone using mobile data. Share only the demo account
credentials with the reviewer. The existing code stores login passwords as plain text;
this package does not redesign authentication. Use it only for a demo with sample data.

## Free-plan behavior

Select Free on BOTH services. Do not select trials, paid upgrades, disks or custom domains.
The assigned onrender.com address works independently of your laptop. Render sleeps after
15 minutes idle and a first visit can be slow. Supabase Free pauses after a week of
inactivity; resume it in its dashboard if paused. Quotas/provider policies still apply;
this is a fixed address, not guaranteed perpetual uptime.

## Local MySQL use remains available

In PowerShell inside the extracted project, with Java 17 configured:

```powershell
$env:DB_PASSWORD = Read-Host 'Your local MySQL password'
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
.\mvnw.cmd spring-boot:run
```

If DB_URL or DB_USERNAME were previously set to cloud values, remove those variables
for local use. Local defaults remain localhost:3306/login and root. No database
password is embedded in the files.

## Troubleshooting

- Password authentication failed: use the database password and exact pooler username.
- Network unreachable: use Session pooler on 5432, not the IPv6-only direct hostname.
- Schema seminar does not exist: run the schema SQL in step 1 and redeploy.
- Email not found: complete step 4 after startup; local accounts were not migrated.
- Build failure: copy the first ERROR from Render logs, hiding any secrets.

References checked September 26, 2026:
https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot
https://supabase.com/pricing
https://render.com/docs/docker
https://render.com/docs/free

Validation: POM XML and Render YAML parsed successfully. A local Maven build was
attempted, but required dependencies could not be downloaded because Maven Central DNS
resolution failed in the preparation environment.
Compilation and a live PostgreSQL connection have not been verified here.
Render will perform the build; complete the login/booking checks after deployment.
