#!/usr/bin/env bash
# Deploy Firestore & Firebase Storage Security Rules to Firebase project
set -euo pipefail

PROJECT_ID="${1:-funny-vote-2e6be}"

echo "🚀 Deploying Security Rules to Firebase Project: ${PROJECT_ID}..."

npx firebase-tools deploy \
    --only firestore:rules,storage:rules \
    --project "${PROJECT_ID}"

echo "✅ Security Rules deployment completed successfully!"
