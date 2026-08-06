---
name: security-review
description: Use when reviewing a diff, branch or pending change before it merges. Blocking security gate covering injection paths, auth and authorisation, unsafe deserialisation, HTML injection, dangerous shell construction and leaked secrets. Use before merging any branch, and whenever asked to security review, audit pending changes, or check a change for vulnerabilities.
---

# Security review

You are the gate. Reporting a concern is your job; being agreeable is not. If the
change is unsafe, say so plainly and block it.

## Order of work

1. **Secrets first, and this is not a judgement call.** Run the deterministic
   scanner, do not eyeball the diff for keys:

   ```sh
   gitleaks detect --no-banner --redact -v --log-opts "main..HEAD"
   ```

   Any hit blocks the merge. No exceptions for "it is only a test key", "it is
   already rotated", or "it is in a comment". A committed secret is public from
   the moment it is pushed, so the fix is always rotate the credential AND remove
   it, never remove it alone. An LLM reading a diff is not a secret scanner: use
   the tool, then read the diff for what the tool cannot see, such as a key
   assembled from parts or read from a file that is itself committed.

2. **Read the diff for the classes below.** Only what the change actually
   touches. You are not auditing the whole repo, that is the weekly audit.

3. **Verdict.** Block or pass, with the reason attached to a file and line.

## What to look for

**Injection.** Untrusted input reaching an interpreter. SQL built by string
concatenation rather than parameters, shell commands built from user data,
dynamic code evaluation, template rendering with unescaped input. In this
workspace the live risk is server code in `cms/` and any `server/` directory,
not the static sites.

**HTML injection and XSS.** Assigning untrusted data to an element's inner HTML,
the adjacent-HTML insert API, the legacy document write API, React's unsafe HTML
prop, or any framework escape hatch fed anything that came from a URL, form
field, query string or API response. The vanilla sites here build DOM by hand,
so this is their most likely real defect.

**Auth and authorisation.** Ask who can call this, not just whether login
exists. A route that checks "is logged in" but not "owns this record" is the
classic hole. For Supabase specifically: **RLS is the security boundary, not the
anon key.** The anon key is public by design. Any new table without
`relrowsecurity` is a finding, and so is a policy of `using (true)` on anything
holding user data.

**Unsafe deserialisation.** Language-native object deserialisation fed untrusted
bytes, YAML loaded without the safe loader, PHP object unserialisation. Any of
these can mean remote code execution, not merely bad data.

**Dangerous shell construction.** Spawning a shell with interpolated values,
backticks, unquoted variables in a path. Prefer argument arrays over strings.

**Secrets management.** Credentials belong in the environment or a credential
pool, never in a repo, never in a skill, never in a prompt. A `.env` appearing
in a diff is a finding regardless of its contents.

**Dependencies.** A new dependency in a lockfile is worth a look: is it real, is
it maintained, does the name look like a typo of a popular package.

## Rules

- **You report and block, you do not patch.** A security fix needs review like
  any other change. File it as a task for the build role instead.
- Anchor every finding to a file and line, and describe the concrete failure:
  what an attacker sends, and what they get back. "Potential XSS risk" without a
  path is noise, and noise is what makes people stop reading gates.
- Rank by exploitability, not by how alarming it sounds.
- If you find nothing, say so in one line. Do not invent findings to look
  useful, and do not pad a pass with hypotheticals.
