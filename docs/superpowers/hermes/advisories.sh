#!/usr/bin/env bash
# Daily dependency advisories for aegis, lockfile matched.
#
# Relevance is structural, not a keyword blocklist: osv-scanner only reports a
# package that actually appears in a lockfile under ~/projects. Windows Server
# and Intune CVEs never match anything here, so they never appear.
#
# Silent on a clean run. Paired with `hermes cron --no-agent`, silence costs
# nothing and sends nothing.
#
# ponytail: --no-agent, so this reports rather than triages. When a finding
# actually lands, upgrade to agent mode so aegis can file it as a kanban task
# for forge. Not before: a daily LLM run to say "nothing found" is a daily bill.

set -uo pipefail

SCANNER="$HOME/bin/osv-scanner"
TARGET="${1:-$HOME/projects}"

if [ ! -x "$SCANNER" ]; then
  echo "advisories: osv-scanner missing at $SCANNER"
  exit 0
fi

out=$("$SCANNER" scan source -r "$TARGET" --format markdown 2>/dev/null)
rc=$?

# 0 = clean, 1 = vulnerabilities found, anything else = the scanner itself broke.
case "$rc" in
  0) : ;;
  1)
    echo "Dependency advisories matching lockfiles under $TARGET:"
    echo
    printf '%s\n' "$out"
    ;;
  *)
    echo "advisories: osv-scanner failed with exit $rc, feed is not trustworthy until fixed"
    ;;
esac
