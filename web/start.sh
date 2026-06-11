#!/bin/bash
# NewsVisualizer - News Intelligence Platform
# Starts the Python API backend (port 8081) and the Next.js frontend (port 3000).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$SCRIPT_DIR/server"
CLIENT_DIR="$SCRIPT_DIR/client"
DATA_DIR="/tmp/newsvisualizer_data"

echo "╔══════════════════════════════════════════════════╗"
echo "║   NewsVisualizer - News Intelligence Platform    ║"
echo "║   Version 3.0.0                                  ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is required but not installed."
    exit 1
fi
if ! command -v npm &> /dev/null; then
    echo "Error: Node.js / npm is required but not installed."
    exit 1
fi

echo "  API:      http://localhost:8081"
echo "  Frontend: http://localhost:3000"
echo "  Database: $DATA_DIR/newsvisualizer.db"
echo ""
echo "Press Ctrl+C to stop."
echo ""

cleanup() { kill 0; }
trap cleanup EXIT

(cd "$SERVER_DIR" && PORT=8081 python3 server.py) &
(cd "$CLIENT_DIR" && npm run dev) &
wait
