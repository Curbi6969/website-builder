---
name: owasp-security-check
description: Use for the standing audit of deployed sites, not for reviewing a diff. Checks security headers, cookie flags, TLS, exposed endpoints, form handling and known-vulnerable CMS plugins against a live URL. Use for a weekly or scheduled security audit, when asked to audit a live site, or before putting a site into production.
---

# Standing audit of a deployed site

This looks at what is actually serving on the internet, which is often not what
the repo says. Use it on a live URL. For a pending change, use `security-review`
instead.

## Where the real risk is in this portfolio

Not in the static sites and not in the Next.js code. **It is the WordPress
site.** Vulnerable WP plugins are the most routinely exploited thing in a
portfolio shaped like this one, automated scanners hit them within days of
disclosure, and that site is run by people who will never read an advisory. If
this audit only ever does one job well, make it that one.

## What to check

**Headers.** Fetch the live URL and read the response headers:

```sh
curl -sSI https://example.com
```

- `Strict-Transport-Security` present with a meaningful max-age
- `Content-Security-Policy` present, and check whether it actually constrains
  anything or is a permissive placeholder
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy` set
- `X-Frame-Options` or a CSP `frame-ancestors` directive
- Server and framework version banners that need not be public

**TLS.** Valid certificate, not near expiry, HTTP redirects to HTTPS rather than
serving both.

**Cookies.** Every cookie that matters should carry `Secure`, `HttpOnly` and a
sensible `SameSite`. A session cookie without `HttpOnly` is a finding.

**Exposed endpoints.** Look for what should not be reachable: `/.env`,
`/.git/config`, backup archives, admin panels open to the whole internet,
directory listings, and on WordPress specifically `/wp-json/wp/v2/users` (user
enumeration) and `xmlrpc.php` (credential stuffing amplifier).

**Forms.** Where does the form post, is it over HTTPS, is there any anti-spam or
rate limiting, and does an error response leak internals.

**CMS and plugin versions.** For WordPress, list the plugins and check each
against Patchstack or Wordfence advisories. Name the plugin, the installed
version, the CVE and the fixed version. A finding without a version number is
not actionable.

## Rules

- **Only audit sites the owner controls.** Never point this at third-party
  infrastructure.
- Read-only. Do not attempt exploitation, do not submit forms with payloads, and
  do not brute force anything. Observation only.
- Report each finding with the evidence: the header that was missing, the URL
  that responded, the version that is behind. A finding you cannot show is a
  guess.
- Rank by what is actually exploitable from the outside today. A missing
  `Referrer-Policy` and an outdated form plugin are not the same severity, and
  treating them alike is how a report gets ignored.
- Repeat findings across weeks are worse than new ones: say how long something
  has been open.
