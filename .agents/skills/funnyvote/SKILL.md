---
name: funnyvote-security-audit
description: Security audit and vulnerability assessment for FunnyVote Android App (MVP Kotlin + Firebase Firestore / Auth / Storage).
---

# FunnyVote Security Profile

## Overview
FunnyVote is a community voting Android application built with Kotlin, Room, Firebase Firestore, Firebase Authentication, and Firebase Storage.

## Security Architecture
1. **Authentication**: Anonymous authentication and Google Sign-In via Firebase Auth.
2. **Database**: Firestore with least-privilege security rules.
3. **Storage**: User image uploads compressed and stored under user-scoped paths in Firebase Storage.
4. **Data Privacy**: Passwords for protected polls hashed or verified securely.
