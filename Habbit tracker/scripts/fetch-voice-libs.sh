#!/usr/bin/env bash
# Downloads the sherpa-onnx Android AAR (on-device STT/TTS native libs) into app/libs/.
# The AAR is gitignored (57 MB) — run this once after cloning to build the voice feature.
set -euo pipefail
VERSION="v1.13.3"
DEST="$(dirname "$0")/../app/libs"
AAR="sherpa-onnx-1.13.3.aar"
mkdir -p "$DEST"
if [ -f "$DEST/$AAR" ]; then
  echo "Already present: $DEST/$AAR"
else
  echo "Downloading $AAR ..."
  curl -sL -o "$DEST/$AAR" \
    "https://github.com/k2-fsa/sherpa-onnx/releases/download/$VERSION/$AAR"
  echo "Done -> $DEST/$AAR"
fi
