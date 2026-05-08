#!/usr/bin/env bash
# Deploy one use-case module to Fly.io using the same command as the
# .github/workflows/fly-deploy.yml workflow.
#
# Usage: ./deploy.sh <module>
# Example: ./deploy.sh clipboard

set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Usage: $0 <module>" >&2
    exit 64
fi

module=$1
repo_root=$(cd "$(dirname "$0")" && pwd)

if [ ! -f "$repo_root/$module/fly.toml" ] || [ ! -f "$repo_root/$module/Dockerfile" ]; then
    echo "Module '$module' is missing fly.toml or Dockerfile under $repo_root/$module" >&2
    exit 66
fi

if [ -z "${VAADIN_PRO_KEY:-}" ]; then
    pro_key_file=$HOME/.vaadin/proKey
    if [ ! -f "$pro_key_file" ]; then
        echo "VAADIN_PRO_KEY is not set and $pro_key_file does not exist" >&2
        exit 78
    fi
    if ! command -v jq >/dev/null 2>&1; then
        echo "jq is required to read $pro_key_file" >&2
        exit 78
    fi
    VAADIN_PRO_KEY=$(jq -r '.proKey // empty' "$pro_key_file")
    if [ -z "$VAADIN_PRO_KEY" ]; then
        echo "Could not read .proKey from $pro_key_file" >&2
        exit 78
    fi
fi

cd "$repo_root"
exec flyctl deploy . --remote-only \
    --config "$module/fly.toml" \
    --dockerfile "$module/Dockerfile" \
    --build-secret proKey="$VAADIN_PRO_KEY"
